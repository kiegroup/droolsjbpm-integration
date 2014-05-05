/**
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kie.remote.services.ws.sei.task;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskSummaryResponse", propOrder = {
    "id",
    "name",
    "subject",
    "description",
    "status",
    "priority",
    "skippable",
    "actualOwner",
    "createdByUser",
    "createdOn",
    "activationTime",
    "expirationTime",
    "processInstanceId",
    "processId",
    "processSessionId",
    "subTaskStrategy",
    "parentId",
    "potentialOwners"
})
public class TaskSummaryResponse {

    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String name;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String subject;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String description;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String status;

    @XmlElement(required=true)
    @XmlSchemaType(name="int")
    private Integer priority;

    @XmlElement(required=true)
    @XmlSchemaType(name="boolean")
    private Boolean skippable;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String actualOwner;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String createdByUser;

    @XmlElement(required=true)
    @XmlSchemaType(name="date")
    private XMLGregorianCalendar createdOn;

    @XmlElement(required=true)
    @XmlSchemaType(name="dateTime")
    private XMLGregorianCalendar activationTime;

    @XmlElement(required=true)
    @XmlSchemaType(name="dateTime")
    private XMLGregorianCalendar expirationTime;

    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long processInstanceId;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement(required=true)
    @XmlSchemaType(name="int")
    private Integer processSessionId;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String subTaskStrategy;

    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long parentId;

    @XmlElement(required=true)
    private List<String> potentialOwners;
}
