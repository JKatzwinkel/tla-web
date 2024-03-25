package tla.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import tla.domain.command.LemmaSearch;
import tla.web.model.CorpusObject;
import tla.web.model.meta.TemplateModelName;
import tla.web.service.CorpusObjectService;
import tla.web.service.ObjectService;

@Controller
@RequestMapping("/object")
@TemplateModelName("object")
public class CorpusObjectController extends ObjectController<CorpusObject, LemmaSearch> {

    private CorpusObjectService service;

    public CorpusObjectController(CorpusObjectService service) {
        this.service = service;
    }

    @Override
    public ObjectService<CorpusObject> getService() {
        return service;
    }

}