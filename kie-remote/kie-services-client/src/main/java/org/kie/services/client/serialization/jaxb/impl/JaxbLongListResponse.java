package org.kie.services.client.serialization.jaxb.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

public class JaxbLongListResponse extends AbstractJaxbCommandResponse<List<Long>> {

    @XmlElement(name="long")
    @XmlSchemaType(name="long")
    private List<Long> resultList;
    
    public JaxbLongListResponse() {
    }
    
    public JaxbLongListResponse(List<Long> result, int i, Command<?> cmd) {
       super(i, cmd);
       this.resultList = result;
    }

    @Override
    public List<Long> getResult() {
        return resultList;
    }

}
