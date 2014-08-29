package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeInstanceLogResponse", propOrder = {
    "processInstanceId",
    "processId",
    "nodeInstanceId",
    "nodeId",
    "nodeName",
    "date",
    "workItemId",
    "connection",
    "externalId",
    "nodeType",
    "type"
})
public class NodeInstanceLogResponse {

    @XmlElement
    @XmlSchemaType(name="long")
    private Long processInstanceId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String nodeInstanceId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String nodeId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String nodeName;

    @XmlElement
    @XmlSchemaType(name="dateTime")
    private XMLGregorianCalendar date;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long workItemId;    

    @XmlElement
    @XmlSchemaType(name="string")
    private String connection;

    @XmlElement
    @XmlSchemaType(name="string")
    private String externalId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String nodeType;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer type;

}
