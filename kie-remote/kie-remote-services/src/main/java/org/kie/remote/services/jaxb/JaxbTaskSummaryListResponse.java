package org.kie.remote.services.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.kie.api.command.Command;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

@XmlRootElement(name="task-summary-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskSummaryListResponse extends AbstractJaxbCommandResponse<List<TaskSummary>> implements JaxbPaginatedList<TaskSummary> {

    @XmlElements({
        @XmlElement(name="task-summary",type=JaxbTaskSummary.class)
    })
    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    private List<TaskSummary> taskSummaryList;

    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
    public JaxbTaskSummaryListResponse() { 
        this.taskSummaryList = new ArrayList<TaskSummary>();
    }
    
    public JaxbTaskSummaryListResponse(Collection<TaskSummary> taskSummaryCollection) { 
       this.taskSummaryList = convertToJaxbTaskSummaryList(taskSummaryCollection);
    }
    
    public JaxbTaskSummaryListResponse(List<TaskSummary> taskSummaryCollection, int i, Command<?> cmd ) { 
        super(i, cmd);
       this.taskSummaryList = convertToJaxbTaskSummaryList(taskSummaryCollection);
    }
    
    private List<TaskSummary> convertToJaxbTaskSummaryList(Collection<TaskSummary> list) {
        if( list == null || list.isEmpty() ) { 
            return new ArrayList<TaskSummary>();
        }
        List<TaskSummary> newList = new ArrayList<TaskSummary>(list.size());
        Iterator<TaskSummary> iter = list.iterator();
        while(iter.hasNext()) { 
            TaskSummary taskSum = iter.next();
            if( taskSum instanceof JaxbTaskSummary ) { 
                newList.add(taskSum);
            } else { 
                newList.add(new JaxbTaskSummary(taskSum));
            }
        }
        return newList;
    }
    
    
    @Override
    public List<TaskSummary> getResult() {
        return taskSummaryList;
    }

    @Override
    public void setResult(List<TaskSummary> result) {
        this.taskSummaryList = convertToJaxbTaskSummaryList(result);
    }

    @Override
    public void addContents(List<TaskSummary> contentList) {
        this.taskSummaryList = convertToJaxbTaskSummaryList(contentList);
    }

    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    public List<TaskSummary> getList() {
        return taskSummaryList;
    }

    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    public void setList(List<TaskSummary> result) {
        this.taskSummaryList = result;
    }

    @Override
    public Integer getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public void setPageNumber(Integer page) {
        this.pageNumber = page;
    }

    @Override
    public Integer getPageSize() {
        return this.pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

}
