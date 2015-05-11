package org.kie.server.api.model.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
public class JaxbMap implements Wrapped<Map<String, Object>>{

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
    @XmlElementWrapper(name="entries")
    private Map<String, Object> entries;

    public JaxbMap() {
    }

    public JaxbMap(Map<String, Object> entries) {
        this.entries = entries;
        if (entries != null && !entries.isEmpty()) {
            for (Map.Entry<String, Object> entry : entries.entrySet()) {
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
