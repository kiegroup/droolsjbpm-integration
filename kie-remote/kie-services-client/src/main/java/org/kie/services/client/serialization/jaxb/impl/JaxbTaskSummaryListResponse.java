package org.kie.services.client.serialization.jaxb.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;

@XmlRootElement(name="task-summary-list")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={TaskSummaryImpl.class})
public class JaxbTaskSummaryListResponse extends AbstractJaxbCommandResponse<List<TaskSummary>> {

    @XmlElements({
        @XmlElement(name="task-summary",type=TaskSummaryImpl.class)
    })
    private List<TaskSummary> taskSummaryList;

    public JaxbTaskSummaryListResponse() { 
        this.taskSummaryList = new ArrayList<TaskSummary>();
    }
    
    public JaxbTaskSummaryListResponse(Collection<TaskSummaryImpl> taskSummaryCollection) { 
       this.taskSummaryList = new ArrayList<TaskSummary>(taskSummaryCollection);
    }
    
    public JaxbTaskSummaryListResponse(List<TaskSummary> taskSummaryCollection, int i, Command<?> cmd ) { 
        super(i, cmd);
        this.taskSummaryList = new ArrayList<TaskSummary>(taskSummaryCollection);
    }
    
    @Override
    public List<TaskSummary> getResult() {
        return taskSummaryList;
    }

    @Override
    public void setResult(List<TaskSummary> result) {
        this.taskSummaryList = result;
    }
    
    

}
