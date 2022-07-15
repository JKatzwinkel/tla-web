package tla.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tla.web.model.Annotation;
import tla.web.model.Lemma;
import tla.web.model.meta.ModelClass;
import tla.web.model.meta.ObjectDetails;
import tla.web.model.meta.TLAObject;

@Service
@ModelClass(Lemma.class)
public class LemmaService extends ObjectService<Lemma> {

    /**
     * Tries to extract a list of annotations from a object details container. Might return null.
     * TODO: should probably be a method of the object details container itself.
     */
    public List<Annotation> extractAnnotations(ObjectDetails<Lemma> container) {
        Map<String, Map<String, TLAObject>> related = container.getRelated();
        if (related != null && related.containsKey("BTSAnnotation") && !related.get("BTSAnnotation").isEmpty()) {
            return related.get("BTSAnnotation").values().stream().map(
                relatedObject -> relatedObject instanceof Annotation ? (Annotation) relatedObject : null
            ).filter(
                annotation -> annotation != null && (annotation.getName() != null && annotation.getBody() != null)
            ).toList();
        } else {
            return null;
        }
    }

    @Override
    public String getLabel(Lemma object) {
        return object.getName();
    }

}