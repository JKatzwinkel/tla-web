package tla.web.model.mappings;

import org.modelmapper.AbstractConverter;

import tla.domain.model.SentenceToken;

public class TokenGlyphsToSVGConverter extends AbstractConverter<SentenceToken, String> {

    @Override
    protected String convert(SentenceToken source) {
        if (source.getGlyphs() == null) {
            return null;
        }
        return Util.jseshRender(
            source.getGlyphs().getMdc(),
            source.getAnnoTypes() != null && source.getAnnoTypes().contains("rubrum")
        );
    }

}
