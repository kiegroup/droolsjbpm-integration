package org.kie.services.client.serialization.jaxb.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

@XmlRootElement(name="string-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbStringListResponse extends AbstractJaxbCommandResponse<List<String>> {

    @XmlElement(name="string")
    @XmlSchemaType(name="string")
    private List<String> resultList;
    
    public JaxbStringListResponse() {
    }
    
    public JaxbStringListResponse(List<String> result, int i, Command<?> cmd) {
       super(i, cmd);
       this.resultList = result;
    }

    @Override
    public List<String> getResult() {
        return resultList;
    }

    @Override
    public void setResult(List<String> result) {
        this.resultList = result;
    }

}
