package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

@XmlRootElement(name="primitive-response")
@XmlAccessorType(XmlAccessType.FIELD)
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

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

}
