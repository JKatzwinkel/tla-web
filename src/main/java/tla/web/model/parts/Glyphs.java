package tla.web.model.parts;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tla.web.model.mappings.Util;
import lombok.AllArgsConstructor;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Glyphs {

    public static final Glyphs EMPTY = new Glyphs();

    private String unicode;

    private String mdc;

    private String svg;

    /**
     * Creates UI hieroglyphs representation of Manuel de Codage encoding.
     */
    public static Glyphs of(String mdc) {
        return of(mdc, false);
    }

    /**
     * Creates UI hieroglyphs representation of Manuel de Codage encoding.
     * @param rubrum whether or not to render the entire thing in red
     */
    public static Glyphs of(String mdc, boolean rubrum) {
        var glyphs = new Glyphs();
        glyphs.setMdc(mdc);
        glyphs.setSvg(Util.jseshRender(mdc, rubrum));
        return glyphs;
    }

    /**
     * create UI hieroglyphs representation (including JSesh SVG) out of
     * Manuel de Codage / Gardiner numbers.
     */
    public static Glyphs of(String mdc, String unicode, boolean rubrum) {
        var glyphs = Glyphs.of(mdc, rubrum);
        glyphs.setUnicode(unicode);
        return glyphs;
    }

    /**
     * Creates UI hieroglyphs representation for DTO glyphs object, i.e. renders SVG graphics
     * using JSesh with optional rubrum characteristics.
     */
    public static Glyphs of(tla.domain.model.SentenceToken.Glyphs dto, boolean rubrum) {
        if (dto == null) {
            return Glyphs.EMPTY;
        }
        var glyphs = Glyphs.of(dto.getMdc(), dto.getUnicode(), rubrum);
        glyphs.setUnicode(dto.getUnicode());
        return glyphs;
    }

    /**
     * Returns true if all attributes are actually empty.
     */
    public boolean isEmpty() {
        return (this.unicode == null || this.unicode.isBlank())
            && (this.mdc == null || this.mdc.isBlank());
    }

}

