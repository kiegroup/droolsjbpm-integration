package org.kie.services.client.serialization.jaxb.impl;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.api.command.Command;

@XmlRootElement(name = "other-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbOtherResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlElement
    private Object result;

    public JaxbOtherResponse() {
    }

    public JaxbOtherResponse(Object result, int i, Command<?> cmd) {
        super(i, cmd);
        this.result = result;
    }

    public JaxbOtherResponse(Map<String, String> variables, String requestUrl) {
        this.url = requestUrl;
        this.status = JaxbRequestStatus.SUCCESS;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
