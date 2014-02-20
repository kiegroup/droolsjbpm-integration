package org.kie.services.client.serialization.jaxb.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.drools.core.common.DefaultFactHandle;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;

@XmlRootElement(name = "other")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbOtherResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlElements({ 
        @XmlElement(name = "fact-handle", type = DefaultFactHandle.class)
        })
    private Object result;

    public JaxbOtherResponse() {
    }

    public JaxbOtherResponse(Object result, int i, Command<?> cmd) {
        super(i, cmd);
        this.result = result;
    }

    public JaxbOtherResponse(Map<String, String> variables, HttpServletRequest request) {
        this.url = getUrl(request);
        this.status = JaxbRequestStatus.SUCCESS;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
