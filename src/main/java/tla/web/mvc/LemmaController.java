package tla.web.mvc;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tla.domain.command.LemmaSearch;
import tla.domain.command.SearchCommand;
import tla.domain.model.Language;
import tla.domain.model.Script;
import tla.web.config.LemmaSearchProperties;
import tla.web.model.Lemma;
import tla.web.model.meta.ObjectDetails;
import tla.web.model.meta.SearchResults;
import tla.web.model.meta.TemplateModelName;
import tla.web.model.ui.AttestationTimeline;
import tla.web.service.LemmaService;
import tla.web.service.ObjectService;

@Controller
@RequestMapping("/lemma")
@TemplateModelName("lemma")
public class LemmaController extends ObjectController<Lemma, LemmaSearch> {

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private LemmaSearchProperties searchConfig;

    public static final Script[] SEARCHABLE_SCRIPTS = {
        Script.HIERATIC,
        Script.DEMOTIC
    };

    public static final Language[] SEARCHABLE_TRANSLATION_LANGUAGES = {
        Language.DE,
        Language.EN,
        Language.FR
    };

    @Override
    public ObjectService<Lemma> getService() {
        return lemmaService;
    }

    @Override
    protected Model extendSingleObjectDetailsModel(Model model, ObjectDetails<Lemma> container) {
        model.addAttribute("annotations", lemmaService.extractAnnotations(container));
        model.addAttribute(
            "timeline",
            AttestationTimeline.from(
                container.getObject().getAttestations()
            )
        );
        return model;
    }

    @ModelAttribute("sortOrders")
    public List<String> getSortOrders() {
        return searchConfig.getSortOrders();
    }

    @Override
    @RequestMapping(value="/search", method=RequestMethod.GET)
    public String getSearchResultsPage(
        @ModelAttribute("lemmaSearchForm") LemmaSearch form,
        @RequestParam(defaultValue = "1") String page,
        @RequestParam MultiValueMap<String, String> params,
        Model model
    ) {
        model.addAttribute("wordClasses", searchConfig.getWordClasses());
        model.addAttribute("lemmaAnnotationTypes", searchConfig.getAnnotationTypes());
        return super.getSearchResultsPage(form, page, params, model);
    }

    @Override
    protected Model extendSearchResultsPageModel(Model model, SearchResults results, SearchCommand<?> searchForm) {
        if (searchForm instanceof LemmaSearch) {
            LemmaSearch form = (LemmaSearch) searchForm;
            model.addAttribute(
                "allTranslationLanguages",
                (form.getTranscription() != null) ? form.getTranslation().getLang() : Collections.EMPTY_LIST
            );
            model.addAttribute("allScripts", form.getScript());
        }
        return model;
    }

}
