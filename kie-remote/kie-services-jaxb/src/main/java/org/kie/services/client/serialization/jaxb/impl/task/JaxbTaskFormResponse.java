package org.kie.services.client.serialization.jaxb.impl.task;

import org.kie.services.client.serialization.jaxb.rest.AbstractJaxbResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="task-form")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskFormResponse extends AbstractJaxbResponse {

    @XmlElement
    private String formUrl;

    public JaxbTaskFormResponse() {
        // Default Constructor
    }

    public JaxbTaskFormResponse(String formUrl) {
        this.formUrl = formUrl;
    }

    public JaxbTaskFormResponse(String formUrl, String requestUrl) {
        super(requestUrl);
        this.formUrl = formUrl;
    }

    public String getFormUrl() {
        return formUrl;
    }
}
