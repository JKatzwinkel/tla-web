package tla.web.model.meta;

import java.util.List;
import java.util.TreeMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tla.domain.model.EditorInfo;
import tla.domain.model.Passport;
import tla.web.model.parts.ExternalReference;

@Getter
@Setter
@NoArgsConstructor
public abstract class BTSObject extends TLAObject {

    private Passport passport;

    private EditorInfo edited;

    private String reviewState;

    private String subtype;

    private String name;

    private TreeMap<String, List<ExternalReference>> externalReferences;

}
