package org.kie.remote.services.ws.sei.history;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableInstanceLogResponse", propOrder = {
    "id",
    "processInstanceId",
    "processId",
    "date",
    "variableInstanceId",
    "variableId",
    "value",
    "oldValue",
    "externalId"
})
public class VariableInstanceLogResponse {

    @XmlElement(required=true)
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
    @XmlSchemaType(name="string")
    private String variableInstanceId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String variableId;

    @XmlElement
    @XmlSchemaType(name="string")
    private String value;

    @XmlElement
    @XmlSchemaType(name="string")
    private String oldValue;

    @XmlElement
    @XmlSchemaType(name="string")
    private String externalId;

}
