package tla.web.mvc;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tla.domain.command.SentenceSearch;
import tla.domain.model.meta.Hierarchic;
import tla.web.model.Sentence;
import tla.web.model.meta.TemplateModelName;
import tla.web.model.ui.BreadCrumb;
import tla.web.model.ui.CorpusPathSegment;
import tla.web.service.ObjectService;
import tla.web.service.SentenceService;

@Controller
@RequestMapping("/sentence")
@TemplateModelName("sentence")
public class SentenceController extends HierarchicObjectController<Sentence, SentenceSearch> {

    @Autowired
    private SentenceService service;

    @Override
    public ObjectService<Sentence> getService() {
        return service;
    }

    /**
     * creates a list of breadcrumb-like link objects beginning with a sentence's
     * text object and followed by optional paragraph and finally line count information.
     */
    private List<BreadCrumb> createSentenceContextLinks(Sentence sentence) {
        return Stream.of(
            ObjectController.createLink(
                sentence.getText().toObjectReference()
            ),
            (
                sentence.getContext().getParagraph() != null
            ) ? new CorpusPathSegment(
                null, sentence.getContext().getParagraph(),
                null, "paragraph"
            ) : null,
            new CorpusPathSegment(
                null, sentence.getContext().getLine(),
                null, "line"
            )
        ).filter(
            Objects::nonNull
        ).toList();
    }

    @Override
    public List<List<BreadCrumb>> createObjectPathLinks(Hierarchic object) {
        var sentence = (Sentence) object;
        var sentenceContextLinks = createSentenceContextLinks(sentence);
        return super.createObjectPathLinks(sentence).stream().map(
            parentObjectPathLinks -> Stream.of(
                parentObjectPathLinks,
                sentenceContextLinks
            ).flatMap(
                links -> links.stream()
            ).toList()
        ).toList();
    }

    /*
     * this needs to be here for the sake of the procedural redundant route generation
     * in {@link SearchController#onApplicationReady}
     */
    @Override
    @RequestMapping(value="/search", method=RequestMethod.GET)
    public String getSearchResultsPage(
        @ModelAttribute("sentenceSearchForm") SentenceSearch form,
        @RequestParam(defaultValue = "1") String page,
        @RequestParam MultiValueMap<String, String> params,
        Model model
    ) {
        return super.getSearchResultsPage(form, page, params, model);
    }

}