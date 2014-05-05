package org.kie.remote.services.ws.sei.task;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskQueryResponse", propOrder = {
    "taskSummary",
})
public class TaskQueryResponse {

    @XmlElement(type=TaskSummaryResponse.class)
    private List<TaskSummaryResponse> taskSummary;
    
}
