package tla.web.mvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import tla.web.config.EditorialConfig.EditorialRegistry;

@TestInstance(Lifecycle.PER_CLASS)
public class EditorialPagesTest extends ViewTest {

    static final String NO_SUPPORT_LANG = "es";

    @Value("${tla.editorials.lang-default}")
    private String fallback;

    @Autowired
    private EditorialRegistry pages;

    @Test
    void registryAvailable() {
        assertAll("editorial registry properly injected",
            () -> assertNotNull(pages),
            () -> assertTrue(pages.getLangSupport().keySet().size() > 2, "more than 2 pages registered")
        );
    }

    @Test
    void registryProperlyInitialized() {
        assertAll("registry populated with contents of editorials folder",
            () -> assertThat(
                pages.getSupportedLanguages("/legal/imprint"),
                hasItems("de", "en")
            ),
            () -> assertEquals(
                fallback, pages.getLangDefault(), "editorial page fallback language"
            )
        );
    }

    /**
     * makes sure that both localization language (language in which i18n labels are being displayed)
     * and payload language (translation of actual editorial page) match the expectation
     */
    void testLocalization(ResultActions testResponse, String path, String lang) throws Exception {
        this.testLocalization(testResponse, lang);
        testResponse.andExpect(
            model().attribute("contentLang", lang)
        ).andExpect(
            xpath("/html/@lang").string(lang)
        ).andExpect(
            xpath("//div[@id='breadcrumbs']/div/nav/ol/li[last()]/span/text()").string(
                messages.getMessage(
                    EditorialContentController.getPageTitleMsgKey(path, lang),
                    null, new Locale(lang)
                )
            )
        );
    }

    private Stream<Arguments> editorialPageLanguageSupport() {
        return pages.getLangSupport().entrySet().stream().map(
            e -> Arguments.of(e.getKey(), e.getValue())
        );
    }

    @ParameterizedTest(
        name = "{index}: editorial {0} should reply to each ?lang URL param value with corresponding translation: {1}"
    )
    @MethodSource("editorialPageLanguageSupport")
    void testEditorials(String editorial, Set<String> languages) throws Exception {
        openEditorial(
            editorial, new LinkedList<>(languages)
        );
    }

    private void openEditorial(String path, List<String> languages) throws Exception {
        assertTrue(languages.size() > 0, "at least one language supported");
        for (String lang : languages) {
            ResultActions test = mockMvc.perform(
                get(
                    String.format("/%s", path)
                ).header(
                    HttpHeaders.ACCEPT_LANGUAGE, lang
                )
            ).andDo(
                print()
            ).andExpect(
                model().attributeExists("env")
            );
            testLocalization(test, String.format("/%s", path), lang);
        }
    }

    @Test
    void acceptedLanguageUnsupported() throws Exception {
        ResultActions test = mockMvc.perform(
            get(
                "/legal/imprint"
            ).header(
                HttpHeaders.ACCEPT_LANGUAGE, NO_SUPPORT_LANG
            )
        ).andDo(
            print()
        );
        testLocalization(test, "/legal/imprint", pages.getLangDefault());
    }

    @Test
    @DisplayName(
        "request for editorial page with weighted HTTP header attributes should return that page in preferred language"
    )
    void someAcceptedLanguagesSupported() throws Exception {
        ResultActions test = mockMvc.perform(
            get(
                "/legal/imprint"
            ).header(
                HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9,de;q=0.8"
            )
        );
        testLocalization(test, "/legal/imprint", "en");
    }

    @Test
    @DisplayName(
        "request for editorial page with weighted HTTP header language attributes should return page in preferred language"
    )
    void weightedAcceptedLanguagesSupported() throws Exception {
        ResultActions test = mockMvc.perform(
            get(
                "/legal/imprint"
            ).header(
                HttpHeaders.ACCEPT_LANGUAGE, "en-US;q=0.8,en;q=0.8,de;q=0.9"
            )
        );
        testLocalization(test, "/legal/imprint", "de");
    }

    @ParameterizedTest(name = "{index}: requesting imprint with ?lang URL param should yield reponse in expected language: {0}")
    @ValueSource(strings = {"de->de", ";->en", "no->en", "->en"})
    void weightedAcceptedLanguages_langParam(String spec) throws Exception {
        String param = spec.split("->")[0];
        String negotiated = spec.split("->")[1];
        ResultActions test = mockMvc.perform(
            get(
                "/legal/imprint"
            ).header(
                HttpHeaders.ACCEPT_LANGUAGE, "en,en-US;q=1.0,de;q=0.9"
            ).param(
                "lang", param
            )
        );
        testLocalization(test, "/legal/imprint", negotiated);
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "en"})
    void testLandingPage(String lang) throws Exception {
        ResultActions test = mockMvc.perform(
            get("/home").param(
                "lang", lang
            )
        );
        testLocalization(test, lang);
        test.andExpect(
            xpath("//div[@id='breadcrumbs']/div/nav/ol/li[last()]/span/text()").string(
                messages.getMessage(
                    EditorialContentController.getPageTitleMsgKey("/home", lang),
                    null, Locale.forLanguageTag(lang)
                )
            )
        );
    }

}
