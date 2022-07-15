package tla.web.mvc;

import java.util.LinkedList;
import java.util.List;

import tla.domain.command.SearchCommand;
import tla.domain.model.ObjectPath;
import tla.domain.model.meta.Hierarchic;
import tla.web.model.meta.TLAObject;
import tla.web.model.ui.BreadCrumb;

public abstract class HierarchicObjectController<T extends TLAObject, S extends SearchCommand<?>> extends ObjectController<T, S> {

    /**
     * converts the object reference chains by which an object can be located within the tree structure
     * it comes from (i.e. thesaurus, text corpus) to link containers that can be rendered in a template.
     *
     */
    public List<List<BreadCrumb>> createObjectPathLinks(Hierarchic object) {
        List<ObjectPath> paths = object.getPaths() != null ? object.getPaths() : new LinkedList<>();
        return paths.stream().map(
            path -> path.stream().map(
                ref -> createLink(ref)
            ).toList()
        ).toList();
    }

}