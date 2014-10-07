package org.kie.services.client.serialization.jaxb.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.kie.api.command.Command;

@XmlRootElement(name="long-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
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

    @Override
    public void setResult(List<Long> result) {
        this.resultList = result;
    }

}
