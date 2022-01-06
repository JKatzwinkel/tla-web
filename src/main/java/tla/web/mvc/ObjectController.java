package tla.web.mvc;

import static tla.web.mvc.GlobalControllerAdvisor.BREADCRUMB_HOME;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import lombok.extern.slf4j.Slf4j;
import tla.domain.command.SearchCommand;
import tla.domain.model.meta.Resolvable;
import tla.error.ObjectNotFoundException;
import tla.web.model.meta.ObjectDetails;
import tla.web.model.meta.SearchResults;
import tla.web.model.meta.TLAObject;
import tla.web.model.meta.TemplateModelName;
import tla.web.model.ui.BreadCrumb;
import tla.web.model.ui.CorpusPathSegment;
import tla.web.model.ui.Pagination;
import tla.web.service.ObjectService;

/**
 * Generic view controller with standard request handlers.
 *
 * Controllers implementing this class must be annotated with {@link Controller},
 * {@link TemplateModelName}, and {@link RequestMapping}. They must also be able to
 * provide a service class annotated with <code>@Service</code> and
 * <code>@ModelClass</code> annotations.
 */
@Slf4j
public abstract class ObjectController<T extends TLAObject, S extends SearchCommand<?>> {

    @Autowired
    TemplateUtils templateUtils;

    /**
     * map eclasses to request mapping/route prefixes.
     */
    protected static Map<String, String> eClassRequestMappings = new HashMap<>();

    /**
     * controller registry
     */
    protected static List<ObjectController<? extends TLAObject, ? extends SearchCommand<?>>> controllers = new LinkedList<>();

    private String templatePath = null;

    public ObjectController() {
        controllers.add(this);
    }

    /**
     * Return value of controller's {@link RequestMapping} annotation.
     *
     * @return URL path prefix
     */
    public String getRequestMapping() {
        for (Annotation a : this.getClass().getAnnotationsByType(RequestMapping.class)) {
            return ((RequestMapping) a).value()[0];
        }
        return null;
    }

    /**
     * Find view controller responsible for domain model class with given BTS <code>eClass</code> identifier
     * and return the URL path prefix to which it responds.
     */
    private static String findRequestMapping(String eclass) {
        for (ObjectController<?, ?> controller : controllers) {
            var service = controller.getService();
            if (service.getModelEClass().equals(eclass)) {
                return controller.getRequestMapping();
            }
        }
        return null;
    }

    /**
     * Return request mapping path prefix of the controller responsible for domain model objects
     * with a given eclass.
     */
    public static String getRequestMapping(String eclass) {
        return eClassRequestMappings.computeIfAbsent(
            eclass,
            k -> findRequestMapping(k)
        );
    }

    /**
     * Creates the URL path at which the details page for an object with a given ID and eclass can be
     * visited.
     */
    public static String getDetailsViewPath(String eclass, String id) {
        return String.format("%s/%s", getRequestMapping(eclass), id);
    }

    /**
     * Create a breadcrumb-like link container from an object reference.
     * @param ref
     * @return
     */
    public static BreadCrumb createLink(Resolvable ref) {
        return CorpusPathSegment.of(
            getDetailsViewPath(ref.getEclass(), ref.getId()),
            ref.getName(),
            ref.getEclass(),
            ref.getType()
        );
    }

    /**
     * Extract template path from {@link TemplateModelName} annotation. Template path
     * is being used in order to locate the HTML template for the single object details
     * view, and for message properties for i18n.
     */
    @ModelAttribute("objectType") // TODO: objecttype und template path trennen
    public String getTemplatePath() {
        if (this.templatePath == null) {
            for (Annotation a : getClass().getAnnotations()) {
                if (a instanceof TemplateModelName) {
                    this.templatePath = ((TemplateModelName) a).value();
                }
            }
        }
        return this.templatePath;
    }

    /**
     * Replaces URL path, but leaves parameters, so that the resulting URL can be used
     * to link back to search form with fields filled.
     */
    @ModelAttribute("modifySearchUrl")
    public String modifySearchUrl() {
        return templateUtils.replacePath("search").build().toString();
    }

    /**
     * Translate object passport to UI representation.
     */
    public LinkedHashMap<String, List<CorpusPathSegment>> getPassportPropertyValues(ObjectDetails<T> container) {
        final var res = new LinkedHashMap<String, List<CorpusPathSegment>>();
        getService().getDetailsPassportPropertyValues(
            container.getObject()
        ).forEach(
            (passportField, values) -> res.put(
                passportField.replace(".", "_"),
                values.stream().map(
                    passportValue -> new CorpusPathSegment(passportValue.getLeafNodeValue())
                ).collect(
                    Collectors.toList()
                )
            )
        );
        return res;
    }

    /**
     * Must return an appropriate {@link ObjectService} instance for a particular controller
     * to be able to invoke operations targeting the entity model class it has been typed for.
     * @return An {@link ObjectService} instance providing access to entities of the specific type
     * required by this controller.
     */
    public abstract ObjectService<T> getService();

    /**
     * Passes the <code>?term</code> URL parameter value through to an <code>/complete</code>
     * autocomplete endpoint of the backend application and return back with its JSON response.
     */
    @RequestMapping(value = "/autocomplete", method = RequestMethod.GET)
    public ResponseEntity<?> autoComplete(
        @RequestParam(required = false) Optional<String> term,
        @RequestParam(required = false) Optional<String> type
    ) {
        return getService().autoComplete(
            term.orElse(""), type.orElse("")
        );
    }

    /**
     * Redirects to object details page, but identifies object by <code>&id</code> URL parameter
     * instead of path variable.
     */
    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    public RedirectView lookup(@RequestParam String id) {
        return new RedirectView(
            String.format(
                "%s/%s", this.getRequestMapping(), id
            ),
            true
        );
    }

    /**
     * Retrieves the requested plus relevant related entites, and renders the results into the
     * single object details template defined for the entity type supported. To this end, it is
     * necessary that a template file <code>details.html</code> exists within a templates folder
     * subdirectory whose name matches the value given in the controller's {@link TemplateModelName}
     * annotation. So the template to be used for rendering the lemma details view must be in the file
     * <code>src/main/resources/templates/lemma/details.html</code> so that the controller
     * <code>@TemplateModelName("lemma") class LemmaController extends ObjectController<Lemma></code>
     * can use it.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getSingleObjectDetailsPage(@PathVariable String id, Model model) {
        log.debug("Compile lemma detail view data for {} {}", getTemplatePath(), id);
        ObjectDetails<T> container = getService().getDetails(id).orElseThrow(
            () -> new ObjectNotFoundException(id, getTemplatePath())
        );
        model.addAttribute(
            "breadcrumbs",
            List.of(
                BREADCRUMB_HOME,
                BreadCrumb.of("/search", "menu_global_search"),
                BreadCrumb.of(
                    String.format("caption_details_%s", getTemplatePath())
                )
            )
        );
        model.addAttribute("obj", container.getObject());
        model.addAttribute("passport", getPassportPropertyValues(container));
        model.addAttribute("caption", getService().getLabel(container.getObject()));
        model.addAttribute("related", container.getRelated());
        model.addAttribute("relations", container.extractRelatedObjects());
        model = extendSingleObjectDetailsModel(model, container);
        return String.format("%s/details", getTemplatePath());
    }

    /**
     * Subclasses can extend the view model by overriding this method.
     */
    protected Model extendSingleObjectDetailsModel(Model model, ObjectDetails<T> container) {
        return model;
    }

    /**
     * Delegate submitted search form to TLA backend and render the results retrieved.
     *
     * @see SearchController#onApplicationReady(org.springframework.boot.context.event.ApplicationReadyEvent)
     */
    public String getSearchResultsPage(
        S form,
        @RequestParam(defaultValue = "1") String page,
        @RequestParam MultiValueMap<String, String> params,
        Model model
    ) {
        log.info("Submitted search form: {}", tla.domain.util.IO.json(form));
        log.info("URL params: {}", params);
        SearchResults results = this.getService().search(form, Integer.parseInt(page)); // TODO validate page
        model.addAttribute("breadcrumbs",
            List.of(
                BREADCRUMB_HOME,
                BreadCrumb.of(
                    modifySearchUrl(), "menu_global_search"
                ),
                BreadCrumb.of(
                    templateUtils.setQueryParam("page", "1"),
                    String.format("menu_global_search_%s", this.getTemplatePath())
                )
            )
        );
        this.addHideableProperties(model);
        model.addAttribute("objectType", getTemplatePath());
        model.addAttribute("searchResults", results.getObjects());
        model.addAttribute("searchQuery", results.getQuery());
        model.addAttribute("facets", results.getFacets());
        model.addAttribute("page", results.getPage());
        model.addAttribute("pagination", new Pagination(results.getPage()));
        model = extendSearchResultsPageModel(model, results, form);
        return String.format("%s/search", getTemplatePath());
    }

    /**
     * Subclasses may override in order to extend view model based on search input and results.
     */
    protected Model extendSearchResultsPageModel(
        Model model, SearchResults results, SearchCommand<?> searchForm
    ) {
        return model;
    }

    protected Model addHideableProperties(Model model) {
        var searchProperties = this.getService().getSearchProperties();
        if (searchProperties != null) {
            model.addAttribute(
                "hideableProperties",
                searchProperties.getHideableProperties()
            );
        }
        return model;
    }

}