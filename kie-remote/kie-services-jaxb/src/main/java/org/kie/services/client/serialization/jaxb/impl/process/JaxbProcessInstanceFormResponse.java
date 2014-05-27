package org.kie.services.client.serialization.jaxb.impl.process;

import org.kie.services.client.serialization.jaxb.rest.AbstractJaxbResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="process-instance-form")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcessInstanceFormResponse extends AbstractJaxbResponse {

    @XmlElement
    private String formUrl;

    public JaxbProcessInstanceFormResponse() {
        // Default Constructor
    }

    public JaxbProcessInstanceFormResponse(String formUrl) {
        this.formUrl = formUrl;
    }

    public JaxbProcessInstanceFormResponse(String formUrl, String requestUrl) {
        super(requestUrl);
        this.formUrl = formUrl;
    }

    public String getFormUrl() {
        return formUrl;
    }
}
