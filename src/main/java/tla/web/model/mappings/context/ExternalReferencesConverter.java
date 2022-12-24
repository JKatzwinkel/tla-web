package tla.web.model.mappings.context;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.List;

import tla.web.config.ApplicationProperties;
import tla.web.config.LinkFormatter;
import org.modelmapper.AbstractConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * Converts external references data structures used in the TLA commons domain model into
 * external references representations by TLA frontend templates model, in which each reference
 * gets expanded into a fully functional hyperlink.
 * 
 * External references ID and TYPE expansion rules are defined under the <code>tla.link-formatters</code>
 * node in the application properties. Please confer to {@link LinkFormatter} documentation for more detail.
 * 
 * @see LinkFormatter
 */
@Slf4j
public class ExternalReferencesConverter extends AbstractConverter<
    SortedMap<String, SortedSet<tla.domain.model.ExternalReference>>,
    SortedMap<String, List<tla.web.model.parts.ExternalReference>>
> {

    private Map<String, LinkFormatter> linkFormatters;

    public ExternalReferencesConverter(Map<String, LinkFormatter> linkFormatters) {
        this.linkFormatters = linkFormatters;
        log.info("instantiate external refs converter");
        log.info(linkFormatters.toString());
    }

    public ExternalReferencesConverter(ApplicationProperties properties) {
        this(properties.getLinkFormatters());
    }

    private String formatLink(String provider, String id, String type) {
        assert linkFormatters != null;
        if (linkFormatters.containsKey(provider)) {
            return linkFormatters.get(provider).format(id, type);
        }
        log.debug("no link format configuration for provider {}", provider);
        return null;
    }

    @Override
    protected SortedMap<String, List<tla.web.model.parts.ExternalReference>> convert(
        SortedMap<String, SortedSet<tla.domain.model.ExternalReference>> source
    ) {
        TreeMap<String, List<tla.web.model.parts.ExternalReference>> res = new TreeMap<>();
        if (source == null) {
            return res;
        }
        source.forEach(
            (provider, refs) -> {
                res.put(
                    provider,
                    refs.stream().map(
                        dto -> tla.web.model.parts.ExternalReference.builder()
                            .href(
                                formatLink(
                                    provider,
                                    dto.getId(),
                                    dto.getType()
                                )
                            )
                            .value(dto.getId())
                            .type(dto.getType())
                            .build()
                    ).toList()
                );
            }
        );
        return res;
    }
}
