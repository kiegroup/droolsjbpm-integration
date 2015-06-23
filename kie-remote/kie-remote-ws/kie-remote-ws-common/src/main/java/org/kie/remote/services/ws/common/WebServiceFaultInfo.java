/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.ws.common;


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
