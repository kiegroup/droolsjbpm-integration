/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "map-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbMap implements Wrapped<Map<String, Object>> {

    @XmlElements({

            // types model
            @XmlElement(name = "boolean-type", type = JaxbBoolean.class),
            @XmlElement(name = "byte-type", type = JaxbByte.class),
            @XmlElement(name = "char-type", type = JaxbCharacter.class),
            @XmlElement(name = "double-type", type = JaxbDouble.class),
            @XmlElement(name = "float-type", type = JaxbFloat.class),
            @XmlElement(name = "int-type", type = JaxbInteger.class),
            @XmlElement(name = "long-type", type = JaxbLong.class),
            @XmlElement(name = "short-type", type = JaxbShort.class),
            @XmlElement(name = "string-type", type = JaxbString.class),
            @XmlElement(name = "map-type", type = JaxbMap.class),
            @XmlElement(name = "list-type", type = JaxbList.class)
    })
    @XmlElementWrapper(name = "entries")
    private Map<String, Object> entries = new HashMap<>();

    public JaxbMap() {
    }

    public JaxbMap(Map<String, Object> entries) {
        if (entries != null && !entries.isEmpty()) {
            this.entries.putAll(entries);
            for (Map.Entry<String, Object> entry : this.entries.entrySet()) {
                entry.setValue(ModelWrapper.wrapSkipPrimitives(entry.getValue()));
            }
        }
    }

    public Map<String, Object> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, Object> entries) {
        this.entries = entries;
    }

    @Override
    public Map<String, Object> unwrap() {

        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (entry.getValue() instanceof Wrapped) {
                entry.setValue(((Wrapped) entry.getValue()).unwrap());
            }
        }

        return entries;
    }
}
