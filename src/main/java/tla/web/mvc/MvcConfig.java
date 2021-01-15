package tla.web.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import tla.web.config.ApplicationProperties;
import tla.web.model.mappings.BTSMarkupConverter;
import tla.web.model.mappings.LanguageFromStringConverter;
import tla.web.model.mappings.ScriptFromStringConverter;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    @Autowired
    public GlobalControllerAdvisor globalAdvisoryController(ApplicationProperties applicationProperties) {
        return new GlobalControllerAdvisor(applicationProperties);
    }

    /**
     * Locale resolver is being called multiple times per request, mysteriously...
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new TLALocaleResolver();
    }

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ScriptFromStringConverter());
        registry.addConverter(new LanguageFromStringConverter());
        registry.addConverter(new BTSMarkupConverter());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/resources/**")
            .addResourceLocations("/resources/");
    }

}
