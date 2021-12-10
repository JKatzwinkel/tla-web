package tla.web.mvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.XpathResultMatchers;

import tla.domain.dto.extern.SingleDocumentWrapper;
import tla.domain.dto.meta.AbstractDto;
import tla.web.model.Lemma;

public class LemmaDetailsTest extends ViewTest {

    private static final String[] EXPECT_TOP_LEVEL_ELEM_IDS = {
        "lemma-property-dict",
        "lemma-property-type-subtype",
        "external-references",
        "translations",
        "bibliography"
    };

    @SuppressWarnings("unchecked")
    private SingleDocumentWrapper<AbstractDto> lemmaDetails(String id) throws Exception {
        return tla.domain.util.IO.loadFromFile(
            String.format(
                "src/test/resources/sample/data/lemma/details/%s.json",
                id
            ),
            SingleDocumentWrapper.class
        );
    }

    private void respondToDetailsRequestWithLemma(String id) throws Exception {
        when(backend.retrieveObject(Lemma.class, id)).thenReturn(lemmaDetails(id));
    }

    private ResultActions makeDetailsRequest(String id, Language lang) throws Exception {
        return mockMvc.perform(
            get("/lemma/" + id).header(HttpHeaders.ACCEPT_LANGUAGE, lang)
        ).andDo(print()).andExpect(status().isOk());
    }

    private void testBasicStructure(ResultActions testResponse, Language lang) throws Exception {
        for (String id : EXPECT_TOP_LEVEL_ELEM_IDS) {
            testResponse.andExpect(
                xpath("//div[@id='details-content']/div[@id='" + id + "']").exists()
            );
        }
        testLocalization(testResponse, lang);
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void testLemmaDetails_containingExternalReferenceWithNoProviderConfigured(Language lang) throws Exception {
        final String id = "44130";
        respondToDetailsRequestWithLemma(id);
        // There is no 'vega' provider in the application properties but vega external reference should be displayed regardlesz
        ResultActions testResponse = makeDetailsRequest(id, lang);
        testBasicStructure(testResponse, lang);
        XpathResultMatchers vega = xpath("//div[@id='external-references-vega']/span[contains(@class,'external-reference-provider')]/text()");
        testResponse.andExpect(
            vega.exists()
        ).andExpect(
            vega.nodeCount(1)
        ).andExpect(
            vega.string("VégA")
        );
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    @DisplayName("details page for hieratic lemma entry should render appropriately")
    void testLemmaDetails_hieratic(Language lang) throws Exception {
        final String id = "31610";
        respondToDetailsRequestWithLemma(id);
        ResultActions testResponse = makeDetailsRequest(id, lang);
        // check if POS fragments get compiled correctly
        testBasicStructure(testResponse, lang);
        testResponse.andExpect(
            xpath("//div[@id='lemma-property-type-subtype']").exists()
        ).andExpect(
            xpath("//div[@id='lemma-property-type-subtype']//span[@id='type-subtype']").exists()
        ).andExpect(
            xpath("//div[@id='details-content']/div[@id='lemma-property-attestations']").exists()
        ).andExpect(
            xpath("//div[@id='passport-properties']").doesNotExist()
        ).andExpect(
            xpath("//div[contains(@class,'bibliography')]/p/span/span[contains(@class,'bibliographic-reference')]").nodeCount(3)
        ).andExpect(
            xpath("//div[contains(@class,'bibliography')]/p/span/span[contains(@class,'bibliographic-reference')]/text()").string(
                "Wb 1, 130.1-5"
            )
        ).andExpect(
            xpath("//div[@id='lemma-property-attestations']/p/a/span").string("1")
        );
    }

    @Test
    @DisplayName("test lemma details generic passport properties")
    void testLemmaDetails_passport() throws Exception {
        final String id = "151410";
        respondToDetailsRequestWithLemma(id);
        makeDetailsRequest(id, Language.en).andExpect(
            xpath("//div[@id='passport-properties']").exists()
        ).andExpect(
            xpath("//div[@id='passport-properties']/p/span[@class='lemma_main_group_nominal_schenkel']").nodeCount(1)
        );
    }

    @Test
    @DisplayName("lemma without passport should be rendered regardless")
    void testLemmaDetails_noPassport() throws Exception {
        respondToDetailsRequestWithLemma("875255");
        makeDetailsRequest("875255", Language.en).andExpect(
            xpath("//div[@id='lemma-property-type-subtype']//span[@id='type-subtype']/span").string(
                messages.getMessage("lemma_type_root", null, Locale.ENGLISH)
            )
        );
    }

    @Test
    @DisplayName("lemma details page should show attested timespan")
    void testAttestedTimespan() throws Exception {
        respondToDetailsRequestWithLemma("100090");
        makeDetailsRequest("100090", Language.en).andExpect(
            xpath("//div[@id='lemma-property-attestations']/p/span/span[@id='attestation-timespan-from']/span[1]").string("2375")
        ).andExpect(
            xpath("//div[@id='lemma-property-attestations']/p/span/span[@id='attestation-timespan-from']/span[2]").string(
                messages.getMessage("object_property_aux_attestation_time_bce", null, Locale.ENGLISH)
            )
        ).andExpect(
            xpath("//div[@id='lemma-property-attestations']/p/a/@href").string(
                "/search/sentence?tokens[0].lemma.id=100090"
            )
        );
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void testLemmaDetails_demotic(Language lang) throws Exception {
        final String id = "d1315";
        respondToDetailsRequestWithLemma(id);
        ResultActions testResponse = makeDetailsRequest(id, lang);
        testBasicStructure(testResponse, lang);
        testResponse.andExpect(
            xpath("//div[@id='lemma-property-hieroglyphs']").doesNotExist()
        ).andExpect(
            xpath("//div[@id='lemma-property-attestations']/p/a/span").string("5")
        );
    }

    @Test
    void testModel() throws Exception {
        respondToDetailsRequestWithLemma("31610");
        ResultActions testResponse = makeDetailsRequest("31610", Language.en);
        testResponse.andExpect(
            model().attributeExists("annotations")
        ).andExpect(
            model().attributeExists("relations")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"100090"})
    void testResponseOk(String id) throws Exception {
        respondToDetailsRequestWithLemma(id);
        makeDetailsRequest(id, Language.en).andExpect(
            xpath("//div[@id='lemma-property-dict']").exists()
        );
    }

    @Test
    @DisplayName("/lemma/lookup?id=ID should redirect to /lemma/ID")
    void testLookup() throws Exception {
        mockMvc.perform(
            get("/lemma/lookup?id=31610")
        ).andExpect(
            status().is3xxRedirection()
        ).andExpect(
            header().string(
                "Location", "/lemma/31610"
            )
        );
    }

}
