package tla.web.model.mappings;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import tla.domain.dto.LemmaDto;
import tla.domain.dto.TextDto;
import tla.domain.dto.meta.NamedDocumentDto;
import tla.domain.model.EditorInfo;
import tla.domain.model.Language;
import tla.domain.util.IO;
import tla.web.config.LinkFormatter;
import tla.web.model.Lemma;
import tla.web.model.Sentence;
import tla.web.model.Text;
import tla.web.model.meta.BTSObject;
import tla.web.repo.TlaClient;

public class MappingConfTest {

    ExternalReferencesConverter externalReferencesConverter() {
        return new ExternalReferencesConverter(
            Map.of("provider", new LinkFormatter("ftp://foo.bar/{id}"))
        );
    }

    LemmaDto lemmaDto() throws Exception {
        return IO.getMapper().readValue(
            """
                {
                    "eclass": "BTSLemmaEntry",
                    "id": "1",
                    "externalReferences": {
                        "provider": [{"id": "11"}]
                    }
                }
            """, LemmaDto.class
        );
    }

    @Test
    @DisplayName("test domain model registration with mapper and backend client")
    void registryTest() {
        MappingConfig.registerModelClass(Lemma.class);
        MappingConfig.registerModelClass(Sentence.class);
        TlaClient.registerModelclass(Lemma.class);
        assertAll("see whether model mapping registry is set up",
            () -> assertNotNull(
                MappingConfig.getModelClass("BTSLemmaEntry"), "lemma eclass mapping"
            ),
            () -> assertEquals(
                Sentence.class, MappingConfig.getModelClass("BTSSentence"),
                "sentence model registered"
            ),
            () -> assertNull(
                MappingConfig.getModelClass("BTSText"),
                "text model not registered, model classes must be registered by someone"
            ),
            () -> assertEquals(
                "lemma",
                TlaClient.getBackendPathPrefix(MappingConfig.getModelClass("BTSLemmaEntry")),
                "lemma model registered with backend client"
            ),
            () -> assertFalse(MappingConfig.getRegisteredModelClasses().isEmpty(), "model class registry empty")
        );
    }

    @Test
    @DisplayName("model mappings should be applied to inheriting model classes")
    void testMappingInheritance() throws Exception {
        var dto = lemmaDto();

        var mapper = new ModelMapper();
        mapper.createTypeMap(NamedDocumentDto.class, BTSObject.class).addMappings(
            m -> m.using(externalReferencesConverter()).map(
                NamedDocumentDto::getExternalReferences, BTSObject::setExternalReferences
            )
        );
        mapper.createTypeMap(LemmaDto.class, Lemma.class).includeBase(
            NamedDocumentDto.class, BTSObject.class
        );
        var obj = mapper.map(dto, Lemma.class);

        assertEquals("1", obj.getId());
        assertEquals(
            "11",
            obj.getExternalReferences().get("provider").get(0).getValue()
        );
    }

    @Test
    @DisplayName("dto to domain mapping: external references")
    void testExternalReferencesMapping() throws Exception {
        var dto = lemmaDto();

        var mapper = new MappingConfig(externalReferencesConverter()).initModelMapper();
        var obj = mapper.map(dto, Lemma.class);

        assertEquals("11", obj.getExternalReferences().get("provider").get(0).getValue());
    }

    @Test
    @DisplayName("custom property mappings should be applied to registered domain model classes")
    void testNestedPropertyMapping() {
        var edit = EditorInfo.builder().author("aaew").contributors(
            List.of("hiwi1", "wimi2")
        ).created(
            Date.valueOf("2016-07-14")
        ).updated(
            Date.valueOf("2021-12-01")
        ).build();
        var dto = TextDto.builder().id("t2").editors(edit).build();

        MappingConfig.registerModelClass(Text.class);
        var mapper = new MappingConfig(externalReferencesConverter()).initModelMapper();
        var obj = mapper.map(dto, Text.class);

        assertAll(
            () -> assertNotNull(obj.getEdited()),
            () -> assertEquals(dto.getEditors().getAuthor(), obj.getEdited().getAuthor()),
            () -> assertEquals(dto.getEditors().getContributors(), obj.getEdited().getContributors()),
            () -> assertEquals(dto.getEditors().getCreated(), obj.getEdited().getCreated()),
            () -> assertEquals(dto.getEditors().getUpdated(), obj.getEdited().getUpdated())
        );
    }

    @Test
    @DisplayName("test sentence token/lemma word conversion")
    void testSentenceTokensMapping() throws Exception {
        var dto = IO.getMapper().readValue(
            """
                {
                    "eclass": "BTSLemmaEntry",
                    "id": "2",
                    "words": [
                        {
                            "id": "001",
                            "label": "nfr",
                            "glyphs": {
                                "mdc": "G43"
                            },
                            "transcription": {
                                "mdc": "nfr"
                            },
                            "translations": {
                                "de": ["schön"]
                            },
                            "lemma": {
                                "id": "10050"
                            },
                            "flexion": {
                                "numeric": 3
                            }
                        },
                        {
                            "id": "002"
                        }
                    ]
                }
            """, LemmaDto.class
        );
        var mapper = new MappingConfig(externalReferencesConverter()).modelMapper();
        var obj = mapper.map(dto, Lemma.class);

        var word = obj.getWords().get(0);
        assertAll(
            () -> assertEquals("001", word.getId()),
            () -> assertEquals("G43", word.getGlyphs().getMdc()),
            () -> assertTrue(word.getGlyphs().getSvg().length() > 20),
            () -> assertEquals("nfr", word.getTranscription().getMdc()),
            () -> assertEquals("schön", word.getTranslations().get(Language.DE).get(0)),
            () -> assertEquals("10050", word.getLemma().getId()),
            () -> assertEquals(3, word.getFlexion().getNumeric())
        );
        assertAll(
            () -> assertNotNull(obj.getWords().get(1).getGlyphs())
        );
    }

}
