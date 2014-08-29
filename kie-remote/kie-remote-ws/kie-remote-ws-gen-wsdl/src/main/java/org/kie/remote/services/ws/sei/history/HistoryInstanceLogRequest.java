package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoryInstanceLogRequest", propOrder = {
    "processInstanceId",
    "processDefId",
    "nodeId",
    "variableId",
    "variableValue",
    "pageNumber",
    "pageSize"
})
public class HistoryInstanceLogRequest {

    @XmlElement(required=false)
    @XmlSchemaType(name="long")
    private Long processInstanceId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String processDefId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String nodeId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String variableId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String variableValue;
    
    @XmlElement(required=false)
    private Integer pageNumber;

    @XmlElement(required=false)
    private Integer pageSize;
}
