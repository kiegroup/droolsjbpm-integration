package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceLogResponse", propOrder = {
    "id",
    "processInstanceId",
    "processId",
    "startDate",
    "endDate",
    "status",
    "parentProcessInstanceId",
    "outcome",
    "duration",
    "identity",
    "processVersion",
    "processName",
    "externalId"
})
public class ProcessInstanceLogResponse {

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
    private XMLGregorianCalendar startDate;

    @XmlElement
    @XmlSchemaType(name="dateTime")
    private XMLGregorianCalendar endDate;

    @XmlElement
    @XmlSchemaType(name="int")
    private Integer status;

    @XmlElement
    @XmlSchemaType(name="long")
    private Long parentProcessInstanceId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String outcome;    

    @XmlElement
    @XmlSchemaType(name="long")
    private Long duration;

    @XmlElement
    @XmlSchemaType(name="string")
    private String identity;    

    @XmlElement
    @XmlSchemaType(name="string")
    private String processVersion;

    @XmlElement
    @XmlSchemaType(name="string")
    private String processName;

    @XmlElement
    @XmlSchemaType(name="string")
    private String externalId;

}
