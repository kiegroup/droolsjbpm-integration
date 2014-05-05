package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoryInstanceLogRequest", propOrder = {
    "logType",
    "processInstanceId",
    "nodeId",
    "variableId",
    "variableValue"
})
public class HistoryInstanceLogRequest {

    @XmlElement
    private HistoryInstanceLogType logType;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private Long processInstanceId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String nodeId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String variableId;

    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String variableValue;
}
