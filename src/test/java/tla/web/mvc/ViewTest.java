package tla.web.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tla.web.repo.TlaClient;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@SpringBootTest
public class ViewTest {

    public static enum Language {
        en;
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
        testLocalization(testResponse, Language.en);
    }

}
