package tla.web.mvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tla.web.repo.TlaClient;


@SpringBootTest
@AutoConfigureMockMvc
class ViewTest {  // NOSONAR

    public enum Language {
        EN;
    }

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected TlaClient backend;

    @Autowired
    protected MessageSource messages;

    void testLocalization(ResultActions testResponse, Language lang) throws Exception {
        testLocalization(testResponse, lang.toString());
    }

    void testLocalization(ResultActions testResponse, String lang) throws Exception {
        testResponse.andExpect(
            content().string(
                not(containsString(
                    String.format("_%s??", lang)
                ))
            )
        );
    }

    void testLocalization(ResultActions testResponse) throws Exception {
        testLocalization(testResponse, Language.EN);
    }

}
