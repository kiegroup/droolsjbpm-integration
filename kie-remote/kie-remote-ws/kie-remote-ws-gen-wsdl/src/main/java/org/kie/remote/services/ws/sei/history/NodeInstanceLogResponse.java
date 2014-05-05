package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeInstanceLogResponse", propOrder = {
    "id",
    "processInstanceId",
    "processId",
    "date",
    "type",
    "nodeInstanceId",
    "nodeId",
    "nodeName",
    "nodeType",
    "workItemId",
    "connection",
    "externalId"
})
public class NodeInstanceLogResponse {

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long processInstanceId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement
    @XmlSchemaType(name="dateTime")
    private XMLGregorianCalendar date;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer type;

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
    @XmlSchemaType(name="string")
    private String nodeType;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long workItemId;    

    @XmlElement
    @XmlSchemaType(name="string")
    private String connection;

    @XmlElement
    @XmlSchemaType(name="string")
    private String externalId;

}
