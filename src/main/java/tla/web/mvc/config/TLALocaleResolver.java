package tla.web.mvc.config;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class TLALocaleResolver extends AcceptHeaderLocaleResolver {

    private SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();

    public static class TLALocaleChangeInterceptor extends LocaleChangeInterceptor {
        public TLALocaleChangeInterceptor(String name) {
            this.setParamName(name);
        }
        @Override
        protected Locale parseLocaleValue(String localeValue) {
            if (isValidContentLanguage(localeValue)) {
                return super.parseLocaleValue(localeValue);
            } else {
                return null;
            }
        }
    }

    public static LocaleChangeInterceptor localeChangeInterceptor(String paramName) {
        return new TLALocaleChangeInterceptor(paramName);
    }

    /**
     * Determines whether a language specifier is even worth considering
     * during content negotiation (i.e. <em>approximate</em> <code>ISO 639-1</code> conformity).
     */
    public static boolean isValidContentLanguage(String lang) {
        return lang != null && lang.matches("[A-Za-z]{2}");
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        sessionLocaleResolver.setLocale(request, response, locale);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if (isValidContentLanguage(request.getParameter("lang"))) {
            return new Locale(
                request.getParameter("lang")
            );
        } else {
            Locale locale = sessionLocaleResolver.resolveLocale(request);
            return (locale != null) ? locale : super.resolveLocale(request);
        }
    }

}
