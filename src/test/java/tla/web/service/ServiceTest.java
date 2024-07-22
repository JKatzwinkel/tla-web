package tla.web.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import tla.domain.command.LemmaSearch;
import tla.domain.command.SentenceSearch;
import tla.domain.dto.extern.SearchResultsWrapper;
import tla.domain.dto.extern.SingleDocumentWrapper;
import tla.domain.dto.meta.DocumentDto;
import tla.domain.model.Passport;
import tla.web.model.Annotation;
import tla.web.model.Lemma;
import tla.web.model.Sentence;
import tla.web.model.ThsEntry;
import tla.web.model.meta.ObjectDetails;
import tla.web.model.meta.SearchResults;
import tla.web.model.meta.TLAObject;
import tla.web.repo.TlaClient;

@SpringBootTest
class ServiceTest {

    @MockitoBean
    private TlaClient backend;

    @Autowired
    private LemmaService lemmaService;

    @Autowired
    private ThsService thsService;

    @Autowired
    private SentenceService sentenceService;

    @Test
    void searchPropertiesRegistry() {
        assertAll("check search properties registry for entries",
            () -> assertNotNull(lemmaService.getSearchProperties(), "lemma search properties registered"),
            () -> assertNotNull(lemmaService.getSearchProperties().getHideableProperties(), "hide/show property list"),
            () -> assertFalse(lemmaService.getSearchProperties().getHideableProperties().isEmpty(), "hide/show property list not empty")
        );
    }

    @Test
    void detailsPropertiesPassportValues() throws Exception {
        Passport p = tla.domain.util.IO.getMapper().readValue(
            """
                {
                    "lemma": [
                        {
                            "main_group": [
                                {"lsort": ["QZe"], "nominal_osing": ["I 2.13"]}
                            ]
                        }
                    ]
                }
            """,
            Passport.class
        );
        Lemma l = new Lemma();
        l.setPassport(p);
        Map<String, List<Passport>> values = lemmaService.getDetailsPassportPropertyValues(l);
        assertAll("check passport value extraction",
            () -> assertEquals(1, values.size()),
            () -> assertTrue(
                values.values().stream().anyMatch(
                    value -> !value.isEmpty()
                ),
                "at least one passport value extracted"
            )
        );
        ThsEntry t = new ThsEntry(); t.setPassport(p);
        assertTrue(
            thsService.getDetailsProperties().getPassportProperties().isEmpty(), "no thesaurus passport properties extracted"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void lemmaService() throws Exception {
        SingleDocumentWrapper<DocumentDto> dto = tla.domain.util.IO.loadFromFile(
            "src/test/resources/sample/data/lemma/details/31610.json",
            SingleDocumentWrapper.class
        );
        ObjectDetails<?> objectDetails = ObjectDetails.from(dto);
        assertTrue(objectDetails.getObject() instanceof Lemma);
        ObjectDetails<Lemma> lemmaDetails = new ObjectDetails<Lemma>(
            (Lemma) objectDetails.getObject(),
            objectDetails.getRelated()
        );
        assertNotNull(lemmaDetails);
        Map<String, List<TLAObject>> relatedObjects = lemmaDetails.extractRelatedObjects();
        assertAll("test related objects extraction",
            () -> assertEquals(dto.getDoc().getRelations().size(), relatedObjects.size(), "predicate count"),
            () -> assertEquals(2, relatedObjects.get("contains").size(), "relations of predicate 'contains'"),
            () -> assertTrue(relatedObjects.get("contains").get(0) instanceof Annotation, "reference reified to Annotation instance")
        );
        List<Annotation> annotations = lemmaService.extractAnnotations(lemmaDetails);
        assertAll("test lemma annotations extraction",
            () -> assertNotNull(annotations),
            () -> assertEquals(1, annotations.size()),
            () -> assertNotNull(annotations.get(0).getBody())
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void thsService() throws Exception {
        when(
            backend.retrieveObject(ThsEntry.class, "KQY2F5SJVBBN7GRO5WUXKG5M6M")
        ).thenReturn(
            tla.domain.util.IO.loadFromFile(
                "src/test/resources/sample/data/ths/details/KQY2F5SJVBBN7GRO5WUXKG5M6M.json",
                SingleDocumentWrapper.class
            )
        );
        assertTrue(
            backend.retrieveObject(ThsEntry.class, "KQY2F5SJVBBN7GRO5WUXKG5M6M") instanceof SingleDocumentWrapper,
            "check if mockup backend client works"
        );
        ObjectDetails<ThsEntry> details = thsService.getDetails("KQY2F5SJVBBN7GRO5WUXKG5M6M").get();
        assertAll("test service method for thesaurus entry details retrieval",
            () -> assertNotNull(details.getObject())
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void lemmaSearchResultsMapping() throws Exception {
        var wrap = tla.domain.util.IO.loadFromFile(
            "src/test/resources/sample/data/lemma/search/demotic_translation_de.json",
            SearchResultsWrapper.class
        );
        when(
            backend.searchObjects(any(), any(), anyInt())
        ).thenReturn(
            wrap
        );
        assertNotNull(wrap);
        SearchResultsWrapper<?> dto = backend.searchObjects(Lemma.class, new LemmaSearch(), 1);
        assertAll("assert that deserialization from file works",
            () -> assertNotNull(dto),
            () -> assertNotNull(dto.getResults())
        );
        SearchResults result = lemmaService.search(new LemmaSearch(), 1);
        assertAll("test mapping from DTO to search result page frontend model",
            () -> assertNotNull(result.getObjects(), "search hits not null"),
            () -> assertNotNull(result.getQuery(), "query not null"),
            () -> assertNotNull(result.getPage(),"page not null")
        );
        result.getObjects().stream().forEach(
            o -> assertTrue(o instanceof Lemma, o.getId() + " should be of class lemma")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void sentenceServiceTextInjection() throws Exception {
        when(
            backend.retrieveObject(Sentence.class, "IBcCBpKz4FWJo0yOhxfTNEhx5J0")
        ).thenReturn(
            tla.domain.util.IO.loadFromFile(
                "src/test/resources/sample/data/sentence/details/IBcCBpKz4FWJo0yOhxfTNEhx5J0.json",
                SingleDocumentWrapper.class
            )
        );
        var container = sentenceService.getDetails("IBcCBpKz4FWJo0yOhxfTNEhx5J0").orElseThrow();
        var sent = container.getObject();
        assertAll("test sentence details retrieval and processing",
            () -> assertNotNull(sent, "sentence received"),
            () -> assertNotNull(sent.getText(), "text injected"),
            () -> assertEquals(sent.getContext().getTextId(), sent.getText().getId(), "correct text injected"),
            () -> assertNotNull(sent.getEdited(), "editing information available")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void sentenceServiceTextInjectionSearchResults() throws Exception {
        when(
            backend.searchObjects(eq(Sentence.class), any(), anyInt())
        ).thenReturn(
            tla.domain.util.IO.loadFromFile(
                "src/test/resources/sample/data/sentence/search/occurrences-145700.json",
                SearchResultsWrapper.class
            )
        );
        var resultsContainer = sentenceService.search(new SentenceSearch(), 1);
        assertAll("test sentence search results container conversion from DTO container",
            () -> assertNotNull(resultsContainer, "search results retrieved"),
            () -> assertNotNull(resultsContainer.getRelated(), "has related objects"),
            () -> assertTrue(resultsContainer.getRelated().size() > 0, "contains related objects")
        );
        var s = (Sentence) resultsContainer.getObjects().get(0);
        assertAll("test sentence search results objects initialization",
            () -> assertNotNull(s.getText(), "sentence result has text information"),
            () -> assertNotNull(s.getText().getPassport(), "got passport"),
            () -> assertNotNull(s.getPaths(), "sentence result has path information")
        );
    }

}
