package tla.web.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Testcontainers
@Tag("selenium")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = SearchPageTest.Initializer.class)
public class SearchPageTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MessageSource messages;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            applicationContext.addApplicationListener(
                (ApplicationListener<WebServerInitializedEvent>) event -> {
                    log.warn("= INIT APP CONTEXT =");
                    log.warn("WEBSERVER PORT: {}", event.getWebServer().getPort());
                    org.testcontainers.Testcontainers.exposeHostPorts(
                        event.getWebServer().getPort()
                    );
                }
            );
        }
    }

    @Container
    private BrowserWebDriverContainer<?> container = new BrowserWebDriverContainer<>().withCapabilities(
        new ChromeOptions()
    );

    void open(RemoteWebDriver driver, String urlPath) {
        var ip = "host.testcontainers.internal";
        driver.get(
            String.format(
                "http://%s:%d%s",
                ip, port, urlPath
            )
        );
    }

    RemoteWebDriver getBrowser() {
        var browser = this.container.getWebDriver();
        browser.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(10)
        );
        return browser;
    }

    @Test
    @SuppressWarnings("deprecation")
    public void lemmaSearch() throws Exception {
        var chrome = getBrowser();
        open(chrome, "/search?lang=de");

        new WebDriverWait(chrome, Duration.ofSeconds(5)).until(
            ExpectedConditions.elementToBeClickable(
                By.id("toggle-sentence-search-form-button")
            )
        );

        assertEquals("TLA", chrome.getTitle(), "check page title");

        assertEquals(
            "false",
            chrome.findElement(
                By.id("toggle-sentence-search-form-button")
            ).getAttribute("aria-expanded"),
            "toggle sentence search form button in collapsed state"
        );

        chrome.executeScript(
            "arguments[0].click()",
            chrome.findElement(
                By.id("toggle-sentence-search-form-button")
            )
        );

        TimeUnit.MILLISECONDS.sleep(800);

        assertEquals(
            "true",
            chrome.findElement(
                By.id("toggle-sentence-search-form-button")
            ).getAttribute("aria-expanded"),
            "toggle sentence search form button in expanded state"
        );

        chrome.executeScript(
            "arguments[0].click()",
            chrome.findElement(
                By.id("toggle-dict-search-form-button")
            )
        );

        TimeUnit.SECONDS.sleep(2);

        assertEquals(
            "true",
            chrome.findElement(
                By.id("toggle-dict-search-form-button")
            ).getAttribute("aria-expanded"),
            "toggle lemma search form button in expanded state"
        );
        assertEquals(
            "false",
            chrome.findElement(
                By.id("toggle-sentence-search-form-button")
            ).getAttribute("aria-expanded"),
            "toggle sentence search form button in collapsed state"
        );

        chrome.findElement(
            By.cssSelector(".lemma-script > input#script1")
        ).click();

        TimeUnit.MILLISECONDS.sleep(800);

        chrome.findElement(
            By.cssSelector("input#transcription")
        ).sendKeys(
            "nfr"
        );

        TimeUnit.MILLISECONDS.sleep(800);

        chrome.findElement(
            By.cssSelector("input#transcription")
        ).submit();

        TimeUnit.MILLISECONDS.sleep(2000);

        assertTrue(
            chrome.getCurrentUrl().contains("/lemma/search"),
            "went to lemma search results page"
        );

        chrome.findElement(
            By.cssSelector("nav.search-results-pagination #page-link-5")
        ).click();

        new WebDriverWait(chrome, Duration.ofSeconds(2));

        chrome.findElement(
            By.cssSelector("button#hide-property-button-hieroglyphs")
        ).click();
        assertEquals(
            messages.getMessage("result_page_description_left", null, Locale.GERMAN),
            chrome.findElement(
                By.cssSelector("div.result-page-desc > span:nth-child(1)")
            ).getText(),
            "german locale is applied despite ?lang parameter not set"
        );

        new WebDriverWait(chrome, Duration.ofSeconds(1));

        chrome.findElement(
            By.cssSelector("#lemma-853340 > a:nth-child(2)")
        ).click();

        TimeUnit.SECONDS.sleep(4);

        chrome.getKeyboard().pressKey(Keys.PAGE_DOWN);
        chrome.getKeyboard().releaseKey(Keys.PAGE_DOWN);

        assertTrue(
            chrome.getCurrentUrl().endsWith("/lemma/853340"),
            "details page for lemma 853340"
        );
    }

}
