package tla.web.mvc;

import static tla.web.mvc.GlobalControllerAdvisor.BREADCRUMB_HOME;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;
import tla.domain.command.LemmaSearch;
import tla.domain.command.SearchCommand;
import tla.domain.command.SentenceSearch;
import tla.domain.command.SentenceSearch.TokenSpec;
import tla.domain.model.Language;
import tla.domain.model.Script;
import tla.web.config.LemmaSearchProperties;
import tla.web.model.Lemma;
import tla.web.model.ui.BreadCrumb;
import tla.web.model.ui.SearchFormExpansionState;

@Slf4j
@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private LemmaSearchProperties lemmaSearchConf;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Value("${search.config.default}")
    private String defaultForm;

    public static final List<String> SEARCH_FORMS = List.of("lemma-quick", "lemma", "sentence");

    @ModelAttribute("allScripts")
    public Script[] getAllScripts() {
        return LemmaController.SEARCHABLE_SCRIPTS; // TODO
    }

    @ModelAttribute("allTranslationLanguages")
    public Language[] getAllTranslationLanguages() {
        return LemmaController.SEARCHABLE_TRANSLATION_LANGUAGES; // TODO
    }

    @ModelAttribute("wordClasses")
    public Map<String, List<String>> getWordClasses() {
        return lemmaSearchConf.getWordClasses();
    }

    @ModelAttribute("lemmaAnnotationTypes")
    public Map<String, List<String>> getLemmaAnnotationTypes() {
        return lemmaSearchConf.getAnnotationTypes();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String mainSearchPage(
        @ModelAttribute("lemma") Lemma lemma,
        @ModelAttribute("lemmaSearchForm") LemmaSearch lemmaForm,
        @ModelAttribute("sentenceSearchForm") SentenceSearch sentenceForm,
        @RequestParam MultiValueMap<String, String> params,
        Model model
    ) {
        model.addAttribute(
            "breadcrumbs",
            List.of(
                BREADCRUMB_HOME,
                BreadCrumb.of("/search", "menu_global_search")
            )
        );
        if (sentenceForm.getTokens() == null || sentenceForm.getTokens().isEmpty()) {
            sentenceForm.setTokens(
                List.of(new TokenSpec(), new TokenSpec())
            );
        }
        model.addAttribute(
            "forms", initFormExpansionStates(params)
        );
        return "search";
    }

    /**
     * Determine how to determine whether a search form should initially be expanded.
     */
    private Function<String, SearchFormExpansionState> expandFormExpression(MultiValueMap<String, String> params) {
        if (SEARCH_FORMS.stream().anyMatch(
            key -> params.containsKey(key)
        )) {
            return key -> new SearchFormExpansionState(
                key, params.containsKey(key)
            );
        } else {
            return key -> new SearchFormExpansionState(
                key, key.equals(defaultForm)
            );
        }
    }

    /**
     * Determine the initial expansion states of collapsible search forms based on URL parameters
     * or the default search mode selected via the application setting <code>search.config.default</code>.
     */
    protected List<SearchFormExpansionState> initFormExpansionStates(MultiValueMap<String, String> params) {
        return SEARCH_FORMS.stream().map(
            this.expandFormExpression(params)
        ).collect(
            Collectors.toList()
        );
    }

    /**
     * returns the URL path leading to the search result page provided by given object controller
     */
    public String getSearchResultsPageRoute(ObjectController<?,?> controller) {
        return String.format("/search%s", controller.getRequestMapping());
    }

    /**
     * Creates additional route to search handlers for all registered domain model object view controllers
     * supporting search (i.e. those which implement
     * {@link ObjectController#getSearchResultsPage(SearchCommand, String, MultiValueMap, Model)}).
     *
     * Currently, domain model object view controllers individually map these handler methods to
     * the route <code> /{objectType}/search </code>, and in this here event handler, a redundant
     * route is being added for those handlers, which results in two routes for each of those
     * search handlers:
     *
     * <ul>
     *  <li><code> /{objectType}/search </code></li>
     *  <li><code> /search/{objectType} </code></li>
     * </ul>
     *
     * Of these two, the latter is preferable and the former should be considered for removal,
     * as it technically creates an ambiguous mapping of the universal object details page route
     * path <code> /{objectType}/{id} </code>. While it is unlikely for an object to be identifiable
     * by the ID <code> "search" </code>, it is not impossible, nor is it even necessary for this
     * situation to be considered somewhat messy. Note that some types of object (i.e. text, object,
     * thesaurus entry) can also be looked up under their <i>short unique id</i> (SUID), which is a
     * 6-character (!) base36 rehash of their some-26-character ID, introduced due to demand from
     * Leipzig for short and easier-to-remember identifiers. As the number of objects in the text
     * corpus and the thesaurus grows, one of them might very well be assigned the 6-character base36
     * SUID <code>"search"</code> at some point sooner or later.
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        for (ObjectController<?,?> controller : ObjectController.controllers) {
            try {
                Method method = controller.getClass().getDeclaredMethod(
                    "getSearchResultsPage",
                    SearchCommand.class,
                    String.class,
                    MultiValueMap.class,
                    Model.class
                );
                // remove potential pre-existing mapping (necessary for test suite)
                handlerMapping.getHandlerMethods().keySet().forEach(
                    mapping -> {
                        if (mapping.getPatternValues().contains(
                            this.getSearchResultsPageRoute(controller)
                        )) {
                            handlerMapping.unregisterMapping(mapping);
                        }
                    }
                );
                // create new handler mapping for object search route
                handlerMapping.registerMapping(
                    RequestMappingInfo.paths(
                        this.getSearchResultsPageRoute(controller)
                    ).methods(RequestMethod.GET).options(
                        EditorialContentController.handlerMappingBuilderConfig(handlerMapping)
                    ).build(),
                    controller,
                    method
                );
            } catch (NoSuchMethodException e) {
                log.info("couldn't create search route for controller {}: no handler method.", controller);
            }
        }
    }

}
