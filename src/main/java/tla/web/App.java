package tla.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// exclusion only necessary until spring boot 3.2.0-M4
// (cf. https://github.com/spring-projects/spring-boot/issues/37660) ⬇:
@SpringBootApplication(exclude = WebSocketServletAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        SpringProperties.setFlag("spring.xml.ignore");
        ApplicationContext context = SpringApplication.run(
            App.class,
            args
        );
        log.info(
            "bean definitions: {}",
            context.getBeanDefinitionNames().length
        );
    }

}
