package tla.web.model.mappings;

import java.awt.geom.Rectangle2D;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang3.RegExUtils;
import org.qenherkhopeshef.graphics.svg.SVGGraphics2D;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.utils.DoubleDimensions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Util
 */
@Slf4j
public class Util {

    private Util() {
        //
    }

    public static final String SERIF_FONT_MARKUP_REGEX = "\\$([^$]+)\\$";
    public static final String SERIF_FONT_MARKUP_REPLACEMENT = "<span class=\"bbaw-libertine\">$1</span>";

    public static final String XML_HEAD = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String SVG_ATTR_REGEX = "width=.([0-9.]+). height=.([0-9.]+).";
    public static final String SVG_ATTR_REPLACEMENT = "viewBox=\"0 0 $1 $2\"";
    private static MDCDrawingFacade facade = new MDCDrawingFacade();

    public static String patchSVG(Writer writer) {
        var xml = writer.toString();
        return RegExUtils.replacePattern(
            xml.subSequence(XML_HEAD.length(), xml.length()),
            SVG_ATTR_REGEX, SVG_ATTR_REPLACEMENT
        );
    }

    private static TopItemList topItems(String mdc, boolean rubrum) throws MDCSyntaxError {
        TopItemList topItems;
        if (rubrum) {
            List<TopItem> parsed = new MDCParserModelGenerator().parse(mdc).asList();
            parsed.forEach(item -> item.setRed(rubrum));
            topItems = new TopItemList();
            topItems.addAll(parsed);
        } else {
            topItems = new MDCParserModelGenerator().parse(mdc);
        }
        return topItems;
    }

    /**
     * Tries to use JSesh in order to render an MdC hieroglyph encoding
     * into an SVG vector graphic.
     * @param mdc hieroglyph sequence in Manuel de Codage (MdC)
     * @param rubrum whether to render entire MdC sequence in red
     * @return textual serialization of SVG vector graphic or null
     */
    @SneakyThrows
    public static String jseshRender(String mdc, boolean rubrum) {
        if (mdc == null || mdc.isBlank()) {
            return null;
        }
        StringWriter writer = new StringWriter();
        Rectangle2D boundingBox = facade.getBounds(
            mdc, 0, 0
        );
        var svg = new SVGGraphics2D(
            writer,
            new DoubleDimensions(
                boundingBox.getWidth(),
                boundingBox.getHeight()
            )
        );
        facade.draw(
            topItems(mdc, rubrum), svg, 0, 0
        );
        svg.dispose();
        return patchSVG(writer);
    }

    /**
     * Render Manuel de Codage hieroglyph encoding to SVG.
     * @param mdc
     * @return SVG serialization
     * @see #jseshRender(String, boolean)
     */
    public static String jseshRender(String mdc) {
        return jseshRender(mdc, false);
    }

    /**
     * Parses the input and replaces <code>$nfr$</code> markup
     * with HTML tags.
     */
    public static String escapeMarkup(String text) {
        if (text == null) {
            return text;
        }
        String escaped = RegExUtils.replacePattern(
            text.subSequence(0, text.length()),
            SERIF_FONT_MARKUP_REGEX,
            SERIF_FONT_MARKUP_REPLACEMENT
        );
        return escaped.replace("\\n", "<br/>");
    }

}
