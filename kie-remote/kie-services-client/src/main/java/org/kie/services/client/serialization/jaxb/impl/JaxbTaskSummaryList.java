package org.kie.services.client.serialization.jaxb.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.jbpm.services.task.query.TaskSummaryImpl;

@XmlRootElement(name="taskSummaryList")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={TaskSummaryImpl.class})
public class JaxbTaskSummaryList {

    @XmlElement(name="taskSummary")
    private List<TaskSummaryImpl> taskSummaryList;

    public JaxbTaskSummaryList() { 
        this.taskSummaryList = new ArrayList<TaskSummaryImpl>();
    }
    
    public JaxbTaskSummaryList(Collection<TaskSummaryImpl> taskSummaryCollection ) { 
        this.taskSummaryList = new ArrayList<TaskSummaryImpl>(taskSummaryCollection);
    }
    public List<TaskSummaryImpl> getTaskSummaryList() {
        return taskSummaryList;
    }

}
