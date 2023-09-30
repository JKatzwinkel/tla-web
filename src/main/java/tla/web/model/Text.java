package tla.web.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tla.domain.dto.TextDto;
import tla.domain.model.meta.BTSeClass;
import tla.domain.model.meta.TLADTO;
import tla.web.model.meta.BackendPath;

@Getter
@Setter
@NoArgsConstructor
@BackendPath("text")
@BTSeClass("BTSText")
@TLADTO(TextDto.class)
public class Text extends CorpusObject {

    public static final Text EMPTY = new Text();

    private TextDto.WordCount wordCount;

}
