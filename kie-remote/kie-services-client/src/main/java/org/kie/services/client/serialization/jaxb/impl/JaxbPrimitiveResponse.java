package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

public class JaxbPrimitiveResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlAttribute
    @XmlSchemaType(name="string")
    private Class<?> type;
    
    @XmlElement
    @XmlSchemaType(name="base64Binary")
    private Object result;
    
    public JaxbPrimitiveResponse() {
    }
    
    public JaxbPrimitiveResponse(Object result, int i, Command<?> cmd) {
       super(i, cmd);
       this.result = result;
       this.type = this.result.getClass();
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Object getResult() {
        return result;
    }

}
