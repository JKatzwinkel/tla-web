package tla.web.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.util.UriTemplate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Instances of this class are supposed to be created automatically via {@link ApplicationProperties}
 * when reading application configuration identified by the prefix <code>tla.link-formatters</code>.
 * Link templates have to be serialized as {@link UriTemplate}.
 * 
 * Link format configuration should look like this:
 * <pre>
 * tla.link-formatters.cfeetk.default-format=http://sith.huma-num.fr/vocable/{id}
 * tla.link-formatters.aaew_wcn.type-formats.hieratic=http://aaew.bbaw.de/tla/servlet/GetWcnDetails?u=guest&f=0&l=0&wn={id}&db=0
 * tla.link-formatters.aaew_wcn.type-formats.demotic=http://aaew.bbaw.de/tla/servlet/GetWcnDetails?u=guest&f=0&l=0&wn={id}&db=1
 * tla.link-formatters.aaew_wcn.type-formats.default=http://aaew.bbaw.de/tla/servlet/GetWcnDetails?u=guest&f=0&l=0&wn={id}&db=0
 * </pre>
 * 
 * Either <code>default-format</code> or <code>type-formats.default</code> should be defined.
 * 
 */
@Slf4j
@Getter
@Setter
@Validated
@NoArgsConstructor
public class LinkFormatter {

    private Map<String, UriTemplate> typeFormats = new HashMap<>();
    private UriTemplate defaultFormat;
    private Pattern idPattern;
    private String typePattern;

    public LinkFormatter(String defaultFormat) {
        this.defaultFormat = new UriTemplate(defaultFormat);
    }

    public boolean canFormat(String id) {
        return this.idPattern == null || this.idPattern.matcher(id).find();
    }

    /**
     * Generates a URL based on a <code>type-format</code> template definition. If no template
     * has been defined for the given <code>type</code> parameter, the <code>default-format</code>
     * template is attempted next. If no default format has been defined either, the link provider's
     * configuration is checked for a <code>type-format</code> definition for type <code>default</code>.
     * If all of this fails, <code>null</code> is returned.
     * 
     * @param id value for the <code>{id}</code> placeholder in the template
     * @param type value for the <code>{type}</code> placeholder in the template
     * @return string representation of an expanded {@link UriTemplate}, or <code>null</code>
     */
    public String format(String id, String type) {
        if (type != null) {
            if (this.typeFormats.containsKey(type)) {
                return this.typeFormats.get(type).expand(Map.of("id", id, "type", type)).toString();
            }
            if (this.defaultFormat != null) {
                log.info("formatter undefined for type '{}' -> using untyped formatter!", type);
                return this.defaultFormat.expand(Map.of("id", id, "type", type)).toString();
            }
        }
        return format(id);
    }

    /**
     * Tries to format a URL for the given ID based on the <code>default-format</code> template defined for the link
     * provider configuration via {@link ApplicationProperties}. Default template can be obtained with
     * {@link LinkFormatter#getDefaultFormat()}.
     * 
     * If no default format has been defined for a link provider, this method will attempt to use the typed format template
     * for type <code>default</code> defined under <code>type-formats</code>. Returns <code>null</code> if no matching
     * template could be found at all.
     * 
     */
    public String format(String id) {
        if (!this.canFormat(id)) {
            log.warn("can not format URL template for ID {} because it is invalid", id);
            return null;
        }
        try {
            if (this.defaultFormat != null) {
                return this.defaultFormat.expand(id).toString();
            }
            if (this.typeFormats.containsKey("default")) {
                log.info("didn't get a type for ID {}, but default-format is undefined; trying 'default' type format", id);
                return this.typeFormats.get("default").expand(id).toString();
            } else {
                log.warn("didn't get a type for ID {}, but neither default-format nor 'default' type-format is undefined", id);
                return null;
            }
        } catch (NullPointerException e) {
            if (this.typeFormats.size() > 0) {
                log.warn("could not format link for ID {}: type attribute required!", id);
            } else {
                log.warn("could not format link for ID {}: default-format property undefinded!", id);
            }
            return null;
        }
    }
}