package org.kie.services.remote.ws.common;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * This contains the information for a web service fault.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WebServiceFaultInfo", propOrder = {
    "type",
    "correlationId"
})
public class WebServiceFaultInfo extends SerializableServiceObject {

	/** Generated Serial version UID. */
    private static final long serialVersionUID = -8214848295651544674L;
    
    /** Type of the exception. */
    @XmlElement
    protected ExceptionType type;
    
    /** For matching the response to the request. */
    @XmlElement
    @XmlSchemaType(name="string")
    protected String correlationId;

    /**
     * @return type type of exception
     */
    public ExceptionType getType() {
        return type;
    }

    /**
     * @param type type of exception
     */
    public void setType(ExceptionType type) {
        this.type = type;
    }

    /**
     * @return Correlation Id
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * @param correlationId Correlation Id
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

}
