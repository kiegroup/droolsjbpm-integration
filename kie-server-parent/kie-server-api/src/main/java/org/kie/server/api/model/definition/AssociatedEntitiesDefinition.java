/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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

    @Override
    public String toString() {
        return "AssociatedEntitiesDefinition{" +
                "associatedEntities=" + associatedEntities +
                '}';
    }
}
