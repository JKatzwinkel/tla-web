package tla.web.mvc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import tla.error.ObjectNotFoundException;
import tla.web.config.ApplicationProperties;
import tla.web.model.ui.BreadCrumb;

@Controller
@ControllerAdvice
public class GlobalControllerAdvisor extends DefaultHandlerExceptionResolver {

    private ApplicationProperties applicationProperties;

    public GlobalControllerAdvisor(ApplicationProperties properties) {
        this.applicationProperties = properties;
    }

    @ModelAttribute("env")
    public Map<String, Object> appVars() {
        return Map.of(
            "baseUrl", applicationProperties.getBaseUrl(),
            "appName", applicationProperties.getName(),
            "l10n", applicationProperties.getL10n(),
            "lang", LocaleContextHolder.getLocale().getLanguage()
        );
    }

    @ModelAttribute("breadcrumbs")
    public List<BreadCrumb> basicBreadcrumb() {
        return List.of(
            BreadCrumb.of("/", "menu_global_home")
        );
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ModelAndView handleObjectNotFound(ObjectNotFoundException e, HttpServletRequest request, Model model) {
        model.addAttribute("env", appVars());
        model.addAttribute("name", e.getClass().getSimpleName());
        model.addAttribute("code", 404);
        model.addAttribute("msg", e.getMessage());
        model.addAttribute("url", request.getRequestURI());
        model.addAttribute(
            "breadcrumbs",
            List.of(
                BreadCrumb.of("/", "menu_global_home"),
                BreadCrumb.of("/search", "menu_global_search")
            )
        );
        return new ModelAndView("error/404", model.asMap(), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnything(Exception e, HttpServletRequest request, Model model) {
        model.addAttribute("env", appVars());
        model.addAttribute("code", 500);
        model.addAttribute("name", e.getClass().getCanonicalName());
        model.addAttribute("url", request.getRequestURI());
        model.addAttribute("msg", "Coulnd't process your request.");
        return new ModelAndView("error/404", model.asMap(), HttpStatus.OK);
    }

}
