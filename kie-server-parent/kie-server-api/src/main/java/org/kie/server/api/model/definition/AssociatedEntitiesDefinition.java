package org.kie.server.api.model.definition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-associated-entities")
public class AssociatedEntitiesDefinition {

    @XmlElementWrapper(name="associated-entities")
    private Map<String, String[]> associatedEntities;

    public AssociatedEntitiesDefinition() {
    }

    public AssociatedEntitiesDefinition(Map<String, String[]> associatedEntities) {
        this.associatedEntities = associatedEntities;
    }

    public static AssociatedEntitiesDefinition from(Map<String, Collection<String>> associatedEntities) {
        Map<String, String[]> data = new HashMap<String, String[]>();

        for (Map.Entry<String, Collection<String>> entry : associatedEntities.entrySet()) {
            data.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }

        return new AssociatedEntitiesDefinition(data);
    }

    public Map<String, String[]> getAssociatedEntities() {
        return associatedEntities;
    }

    public void setAssociatedEntities(Map<String, String[]> associatedEntities) {
        this.associatedEntities = associatedEntities;
    }
}
