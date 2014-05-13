package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceResponse<T> {
    public static enum ResponseType {
        SUCCESS, FAILURE;
    }
    
    @XmlAttribute
    private ServiceResponse.ResponseType type;
    @XmlAttribute
    private String msg;
    @XmlElements({
        @XmlElement(name = "kie-server-info", type = KieServerInfo.class),
        @XmlElement(name = "kie-container", type = KieContainerResource.class),
        @XmlElement(name = "results", type = String.class),
        @XmlElement(name = "kie-containers", type = KieContainerResourceList.class),
        @XmlElement(name = "kie-scanner", type = KieScannerResource.class)
    })
    private T result;
    
    
    public ServiceResponse() {
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg) {
        this.type = type;
        this.msg = msg;
    }
    
    public ServiceResponse(ServiceResponse.ResponseType type, String msg, T result ) {
        this.type = type;
        this.msg = msg;
        this.result = result;
    }
    
    public ServiceResponse.ResponseType getType() {
        return type;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setType(ServiceResponse.ResponseType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ServiceResponse[" + type + ", msg='" + msg + "']";
    }
}