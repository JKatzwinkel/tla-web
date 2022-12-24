package tla.web.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import tla.domain.model.Passport;
import tla.web.model.parts.Glyphs;
import tla.web.model.parts.Token;

import static org.junit.jupiter.api.Assertions.*;

public class LemmaTest {

    @Test
    void glyphConstructionTest() {
        Glyphs g1 = Glyphs.of(null);
        assertAll("creator should accept null value",
            () -> assertNotNull(g1, "should return object"),
            () -> assertTrue(g1.isEmpty(), "should be considered empty tho"),
            () -> assertEquals(g1, Glyphs.of(null), "empty hieros should always be equal")
        );
        Glyphs g2 = Glyphs.of(" ");
        assertAll("creator should accept empty value",
            () -> assertNotNull(g2, "should return object"),
            () -> assertTrue(g2.isEmpty(), "should be considered empty tho")
        );
    }

    @Test
    void getHieroglyphs() throws Exception {
        List<String> wordGlyphs = List.of("N35", "", "G43");
        Lemma l = Lemma.builder()
            .id("1")
            .words(
                wordGlyphs.stream().map(
                    mdc -> {
                        var token = new Token();
                        token.setGlyphs(Glyphs.of(mdc));
                        return token;
                    }
                ).toList()
            ).build();
        List<Glyphs> glyphs = l.getHieroglyphs();
        assertAll("test hieroglyphs from lemma extraction",
            () -> assertEquals(3, glyphs.size(), "item count"),
            () -> assertTrue(glyphs.get(1).isEmpty(), "second item empty"),
            () -> assertEquals(Glyphs.of("N35"), glyphs.get(0), "first item"),
            () -> assertEquals(Glyphs.of("N35").hashCode(), glyphs.get(0).hashCode(), "first item hashcode")
        );
    }

    @Test
    void getBibliography() throws Exception {
        Passport p = new Passport();
        Passport b = new Passport();
        b.add(
            "bibliographical_text_field",
            new Passport(
                "Wb 1, 130.1-5; Germer, Flora, 125; LÄ II, 1265"
            )
        );
        p.add("bibliography", b);
        assertAll("test passport",
            () -> assertTrue(!b.isEmpty(), "subnode not empty"),
            () -> assertTrue(b.containsKey("bibliographical_text_field"), "subnode key"),
            () -> assertEquals(1, p.size(), "root size"),
            () -> assertEquals(List.of("bibliography"), p.getFields(), "root keys"),
            () -> assertTrue(p.containsKey("bibliography"), "root key"),
            () -> assertTrue(!p.getProperties().get("bibliography").isEmpty(), "subnodes exist"),
            () -> assertTrue(p.getProperties().get("bibliography").get(0).containsKey("bibliographical_text_field"))
        );
        Lemma l = Lemma.builder().passport(p).build();
        assertAll("see if bibliography can be extracted",
            () -> assertNotNull(l.getBibliography(), "bibliography not null"),
            () -> assertTrue(!l.getBibliography().isEmpty(), "bibl not empty"),
            () -> assertEquals(3, l.getBibliography().size(), "3 bibl entries"),
            () -> assertEquals("Germer, Flora, 125", l.getBibliography().get(1), "value trimmed")
        );
    }
}