package tla.web.model.parts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import tla.domain.model.Language;
import tla.domain.model.SentenceToken;

import java.util.List;
import java.util.SortedMap;

import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    private String id;

    private String label;

    private String type;

    private Transcription transcription;

    private Glyphs glyphs = new Glyphs();

    private SentenceToken.Flexion flexion;

    private SentenceToken.Lemmatization lemma;

    @Singular
    private SortedMap<Language, List<String>> translations;

    @Singular
    private List<String> annoTypes;

    public boolean isRubrum() {
        return this.annoTypes != null ? this.annoTypes.contains(
            "rubrum"
        ) : false;
    }

}
