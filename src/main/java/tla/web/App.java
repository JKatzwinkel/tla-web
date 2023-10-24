package tla.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringProperties.setFlag("spring.xml.ignore");
        ApplicationContext context = SpringApplication.run(App.class, args);
        log.info(
            "bean definitions: {}", context.getBeanDefinitionNames().length
        );
    }

}
