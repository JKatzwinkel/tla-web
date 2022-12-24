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
        var l = new Lemma();
        l.setId("1");
        l.setWords(
            wordGlyphs.stream().map(
                mdc -> {
                    var token = new Token();
                    token.setGlyphs(Glyphs.of(mdc));
                    return token;
                }
            ).toList()
        );
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
        Passport passport = new Passport();
        Passport bibliography = new Passport();
        bibliography.add(
            "bibliographical_text_field",
            new Passport(
                "Wb 1, 130.1-5; Germer, Flora, 125; LÄ II, 1265"
            )
        );
        passport.add("bibliography", bibliography);
        assertAll("test passport",
            () -> assertTrue(!bibliography.isEmpty(), "subnode not empty"),
            () -> assertTrue(bibliography.containsKey("bibliographical_text_field"), "subnode key"),
            () -> assertEquals(1, passport.size(), "root size"),
            () -> assertEquals(List.of("bibliography"), passport.getFields(), "root keys"),
            () -> assertTrue(passport.containsKey("bibliography"), "root key"),
            () -> assertTrue(!passport.getProperties().get("bibliography").isEmpty(), "subnodes exist"),
            () -> assertTrue(passport.getProperties().get("bibliography").get(0).containsKey("bibliographical_text_field"))
        );
        Lemma lemma = new Lemma();
        lemma.setPassport(passport);
        assertAll("see if bibliography can be extracted",
            () -> assertNotNull(lemma.getBibliography(), "bibliography not null"),
            () -> assertTrue(!lemma.getBibliography().isEmpty(), "bibl not empty"),
            () -> assertEquals(3, lemma.getBibliography().size(), "3 bibl entries"),
            () -> assertEquals("Germer, Flora, 125", lemma.getBibliography().get(1), "value trimmed")
        );
    }
}