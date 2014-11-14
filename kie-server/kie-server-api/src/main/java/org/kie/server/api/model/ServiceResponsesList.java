package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="responses")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceResponsesList {

    @XmlElement(name="response")
    private List<ServiceResponse<? extends Object>> responses;

    public ServiceResponsesList() {
        responses = new ArrayList<ServiceResponse<? extends Object>>();
    }

    public ServiceResponsesList(List<ServiceResponse<? extends Object>> responses) {
        this.responses = responses;
    }

    public List<ServiceResponse<? extends Object>> getResponses() {
        return responses;
    }

    public void setResponses(List<ServiceResponse<? extends Object>> responses) {
        this.responses = responses;
    }
}
