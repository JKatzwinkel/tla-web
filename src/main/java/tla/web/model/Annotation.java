package tla.web.model;

import java.util.Collection;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tla.domain.dto.AnnotationDto;
import tla.domain.model.Passport;
import tla.domain.model.meta.BTSeClass;
import tla.domain.model.meta.TLADTO;
import tla.web.model.mappings.Util;
import tla.web.model.meta.BTSObject;

@Getter
@Setter
@NoArgsConstructor
@BTSeClass("BTSAnnotation")
@TLADTO(AnnotationDto.class)
public class Annotation extends BTSObject {

    @Getter(AccessLevel.NONE)
    private Collection<String> body;

    /**
     * Escape markup before returning value.
     */
    @Override
    public String getName() {
        return Util.escapeMarkup(super.getName());
    }

    /**
     * Lemma annotations have textual content in their passports
     * and we want to access that conveniently so just copy it
     * into here.
     * Might return null tho.
     * Escapes markup.
     */
    public String getBody() {
        if (this.body == null) {
            this.body = this.extractBody();
        }
        return Util.escapeMarkup(
            String.join("\n\n", this.body)
        );
    }

    /**
     * Try to extract text content from <code>"annotation.lemma"</code> nodes in the annotation's passport.
     */
    private Collection<String> extractBody() {
        if (this.getPassport() == null) {
            return List.of();
        }
        List<Passport> nodes = this.getPassport().extractProperty(
            "annotation.lemma"
        );
        if (nodes == null) {
            return List.of();
        }
        return nodes.stream().filter(
            node -> {
                return !node.isEmpty() && !node.getLeafNodeValue().isBlank();
            }
        ).map(
            Passport::getLeafNodeValue
        ).map(
            String::trim
        ).toList();
    }

}