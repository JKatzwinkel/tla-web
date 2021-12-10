package tla.web.mvc;

import static tla.web.mvc.GlobalControllerAdvisor.BREADCRUMB_HOME;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.jooq.lambda.Seq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;
import tla.web.config.EditorialConfig.EditorialRegistry;
import tla.web.model.ui.BreadCrumb;

/**
 * Uses dynamic mappings to handle requests for quote unquote "<em>static</em>" pages.
 */
@Slf4j
@Controller
@RequestMapping("/")
public class EditorialContentController {

    @Autowired
    private MessageSource l10n;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private EditorialRegistry editorialRegistry;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Value("${tla.editorials.path}")
    private String editorialsDir;

    @ModelAttribute("build")
    public Map<String, String> buildInfo() {
        return Map.of();
    }

    /**
     * Select one of the languages in which a requested editorial page is available
     * under consideration of the <code>Accept-Language</code> values passed.
     */
    private String negotiateContentLanguage(String path, HttpHeaders header) {
        List<Locale.LanguageRange> requested = header.getAcceptLanguage();
        requested.sort(
            Comparator.comparing(
                Locale.LanguageRange::getWeight
            ).reversed()
        );
        var supported = getSupportedLanguages(path);
        for (Locale.LanguageRange lang : requested) {
            if (supported.contains(lang.getRange())) {
                return lang.getRange();
            }
        }
        return editorialRegistry.getLangDefault();
    }

    private Set<String> getSupportedLanguages(String path) {
        return  editorialRegistry.getSupportedLanguages(
            path
        );
    }

    /**
     * Add specified language with a weight of 1.0 to the language specifier list
     * in the accepted languages header field.
     */
    private HttpHeaders preferContentLanguage(String lang, HttpHeaders header) {
        if (lang != null && TLALocaleResolver.isValidContentLanguage(lang)) {
            List<Locale.LanguageRange> requested = new ArrayList<>(
                header.getAcceptLanguage()
            );
            requested.add(
                0,
                new Locale.LanguageRange(lang, 1.0f)
            );
            header.setAcceptLanguage(requested);
        }
        return header;
    }

    /**
     * build i18n message key for an editorial page's title.
     */
    public static String getPageTitleMsgKey(String path, String lang) {
        return "editorial_title_" + Seq.toString(
            Stream.of(
                path.split("/")
            ).filter(
                segm -> !segm.isBlank()
            ),
            "_"
        );
    }

    /**
     * Looks up the page title as defined in <code>messages.properties</code>.
     *
     * Example: Title for <code>legal/imprint</code> is defined via message key
     * <code>editorial_title_legal_imprint</code>.
     */
    public String getPageTitle(String path, String lang) {
        return l10n.getMessage(
            getPageTitleMsgKey(path, lang),
            null,
            path,
            new Locale(lang)
        );
    }

    /**
     * Puts together the file system path where to find the negotiated template.
     *
     * TODO get subdir from ${tla.editorials.path}
     * TODO account for path not being prefixed by slash
     */
    private String templatePath(String lang, String path) {
        return Path.of(
            "..",
            editorialsDir,
            lang,
            path
        ).toString();
    }

    /**
     * Generic handler for semi-static editorial HTML page requests.
     */
    public String renderEditorial(
        HttpServletRequest request,
        @RequestParam(required = false) String lang,
        @RequestHeader HttpHeaders header,
        Model model
    ) throws Exception {
        String path = request.getRequestURI();
        preferContentLanguage(
            localeResolver.resolveLocale(request).getLanguage(),
            header
        );
        preferContentLanguage(lang, header);
        String contentLang = negotiateContentLanguage(path, header);
        log.info(
            "serving request for static page {} with accepted languages {} with negotiated language {}.",
            path, header.getAcceptLanguage(), contentLang
        );
        var availableLanguages = getSupportedLanguages(path);
        model.addAttribute("templatePath", templatePath(contentLang, path));
        model.addAttribute("contentLang", contentLang);
        model.addAttribute("env.l10n", availableLanguages);
        model.addAttribute(
            "pageTitle",
            getPageTitle(path, contentLang)
        );
        model.addAttribute(
            "breadcrumbs",
            List.of(
                BREADCRUMB_HOME,
                BreadCrumb.of(
                    getPageTitleMsgKey(path, lang)
                )
            )
        );
        return "editorial";
    }


    // TODO: this is a workaround snatched from
    // TODO: https://github.com/qaware/openapi-generator-for-spring/blob/5b95aefe436fc5e4c9e36a5df829e0f43c2ca5aa/openapi-generator-for-spring-webmvc/src/main/java/de/qaware/openapigeneratorforspring/autoconfigure/OpenApiGeneratorWebMvcAutoConfiguration.java#L61-L76
    // TODO: once spring 5.3.14 is out, replace with RequestMappingHandlerMapping#getRequestMappingInfoBuilderConfiguration from
    // TODO: https://github.com/spring-projects/spring-framework/pull/27723/files
    public static BuilderConfiguration handlerMappingBuilderConfig(RequestMappingHandlerMapping handlerMapping) {
        BuilderConfiguration config = new BuilderConfiguration();
        config.setTrailingSlashMatch(handlerMapping.useTrailingSlashMatch());
        config.setContentNegotiationManager(handlerMapping.getContentNegotiationManager());
        config.setPatternParser(handlerMapping.getPatternParser());
        return config;
    }


    /**
     * Creates request handler mappings for editorial pages previously registered in the
     * {@link EditorialRegistry}.
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) throws NoSuchMethodException {
        log.info("ready to create request handler mappings for editorial templates");
        Method handlerMethod = EditorialContentController.class.getDeclaredMethod(
            "renderEditorial",
            HttpServletRequest.class,
            String.class,
            HttpHeaders.class,
            Model.class
        );
        try {
            this.editorialRegistry.getLangSupport().forEach(
                (path, supportedLanguages) -> {
                    handlerMapping.registerMapping(
                        RequestMappingInfo.paths(path).methods(RequestMethod.GET).options(
                            handlerMappingBuilderConfig(handlerMapping)
                        ).build(),
                        this,
                        handlerMethod
                    );
                }
            );
        } catch (Exception e) {}
    }

}
