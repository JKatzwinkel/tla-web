package tla.web.repo;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepoConfig {

    @Bean
    public TlaClient tlaClient(Environment env) {
        return new TlaClient(env.getProperty("tla.backend-url"));
    }

}
