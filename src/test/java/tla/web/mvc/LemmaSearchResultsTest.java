package tla.web.mvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import tla.domain.command.LemmaSearch;
import tla.domain.dto.extern.SearchResultsWrapper;

public class LemmaSearchResultsTest extends ViewTest {

    /**
     * load search results transfer object from file.
     */
    @SuppressWarnings("rawtypes")
    SearchResultsWrapper loadDto(String filename) throws Exception {
        return tla.domain.util.IO.loadFromFile(
            String.format(
                "src/test/resources/sample/data/lemma/search/%s",
                filename
            ),
            SearchResultsWrapper.class
        );
    }

    @SuppressWarnings("unchecked")
    SearchResultsWrapper<?> mockSearch(String filename) throws Exception {
        var dto = loadDto(filename);
        assertNotNull(dto);
        when(
            backend.searchObjects(any(), any(), anyInt())
        ).thenReturn(
            dto
        );
        return dto;
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void testSearchResults(Language lang) throws Exception {
        var dto = mockSearch("demotic_translation_de.json");
        ResultActions testResponse = mockMvc.perform(
            get("/search/lemma").header(HttpHeaders.ACCEPT_LANGUAGE, lang)
        ).andDo(print()).andExpect(
            status().isOk()
        );
        testLocalization(testResponse, lang);
        testResponse.andExpect(
            xpath("//div[contains(@class,'result-list-item')]").nodeCount(dto.getResults().size())
        ).andExpect(
            xpath("//div[contains(@class,'result-page-desc')]").exists()
        ).andExpect(
            xpath("//div[contains(@class,'result-page-desc')]/b[1]/text()").string("21 - 38")
        ).andExpect(
            xpath("//div[@id='lemma-dm2434']//span[contains(@class,'subcorpus')]").exists()
        ).andExpect(
            xpath("//div[@id='lemma-dm2434']//span[contains(@class,'hieroglyphs')]").doesNotExist()
        ).andExpect(
            xpath("//div[@id='lemma-dm529']/a[contains(@class,'review-state')]//span[contains(@class,'notok')]").exists()
        );
    }

    @Test
    @DisplayName("search parameters should be preserved inside hidden search form")
    void testHiddenSearchForm() throws Exception {
        mockSearch("demotic_translation_de.json");
        ResultActions testResponse = mockMvc.perform(
            get(
                "/search/lemma?wordClass.type=adjective&wordClass.subtype=nisbe_adjective_preposition"
            ).header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        ).andDo(print()).andExpect(
            status().isOk()
        );
        testResponse.andExpect(
            xpath(
                "//select[@name='wordClass.type']/option[@value='adjective']"
            ).exists()
        ).andExpect(
            xpath(
                "//select[@name='anno.type']/option[@value='lemma']"
            ).exists()
        );
    }

    @Test
    void testFulltypeLabels() throws Exception {
        mockSearch("demotic_translation_de.json");
        ResultActions testResponse = mockMvc.perform(
            get("/search/lemma").header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        ).andDo(print()).andExpect(
            status().isOk()
        );
        testResponse.andExpect(
            xpath(
                "//div[@id='lemma-dm2254']//span[contains(@class,'type-subtype')]/span/text()"
            ).string(
                messages.getMessage(
                    "lemma_fulltype_entity_name_person_name",
                    null, Locale.ENGLISH
                )
            )
        );
    }

    @Test
    void testSideBar() throws Exception {
        mockSearch("demotic_translation_de.json");
        ResultActions testResponse = mockMvc.perform(
            get("/search/lemma")
        ).andDo(print());

        testResponse.andExpect(
            xpath("//div[@class='hide-properties-buttons']/button[@id='hide-property-button-bibliography']").exists()
        ).andExpect(
            xpath("//div[@class='hide-properties-buttons']/button[@id='hide-property-button-translations']").exists()
        );
    }

    @Test
    @DisplayName("check lemma search form conversion")
    void testSearchFormConversion() throws Exception {
        when(
            backend.searchObjects(any(), any(), anyInt())
        ).thenAnswer(
            i -> {
                LemmaSearch form = i.getArgument(1, LemmaSearch.class);
                assertAll("check URLdecode conversion of form field values",
                    () -> assertEquals("säk", form.getBibliography(), "bibliography"),
                    () -> assertEquals("könig", form.getTranslation().getText(), "translation"),
                    () -> assertEquals("zḫi̯", form.getTranscription(), "transcription")
                );
                return loadDto("demotic_translation_de.json");
            }
        );
        mockMvc.perform(
            get("/lemma/search?bibliography=säk&translation.text=k%C3%B6nig&transcription=zḫi̯&root=")
        ).andDo(print());
        verify(backend).searchObjects(any(), any(), anyInt());
    }

}
