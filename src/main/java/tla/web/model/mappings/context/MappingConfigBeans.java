package tla.web.model.mappings.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tla.web.config.ApplicationProperties;

@Configuration
public class MappingConfigBeans {

    @Bean
    public ExternalReferencesConverter externalReferencesConverter(
        ApplicationProperties properties
    ) {
        return new ExternalReferencesConverter(properties);
    }

}
