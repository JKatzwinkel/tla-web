package tla.web.model;

import java.util.List;
import java.util.SortedMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import tla.domain.dto.SentenceDto;
import tla.domain.dto.SentenceDto.SentenceContext;
import tla.domain.model.EditorInfo;
import tla.domain.model.Language;
import tla.domain.model.ObjectPath;
import tla.domain.model.meta.BTSeClass;
import tla.domain.model.meta.Hierarchic;
import tla.domain.model.meta.TLADTO;
import tla.web.model.meta.BackendPath;
import tla.web.model.meta.TLAObject;
import tla.web.model.parts.Transcription;
import tla.web.model.parts.Token;

@Getter
@Setter
@NoArgsConstructor
@BackendPath("sentence")
@BTSeClass("BTSSentence")
@TLADTO(SentenceDto.class)
public class Sentence extends TLAObject implements Hierarchic {

    private SentenceContext context;

    private List<Token> tokens;

    private int wordCount;

    private Transcription transcription;

    private Text text;

    @Singular
    private SortedMap<Language, List<String>> translations;

    /**
     * Determine whether any of a sentence's tokens have any hieroglyph encodings.
     */
    public boolean hasGlyphs() {
        return this.tokens.stream().anyMatch(
            token -> !token.getGlyphs().isEmpty()
        );
    }

    public String getName() {
        return this.getText() != null ? this.getText().getName() : null;
    }

    public String reviewState() {
        return this.getText() != null ? this.getText().getReviewState() : "published";
    }

    public EditorInfo getEdited() {
        return this.getText() != null ? this.getText().getEdited() : null;
    }

    @Override
    public List<ObjectPath> getPaths() {
        return this.getText() != null ? this.getText().getPaths() : null;
    }

}
