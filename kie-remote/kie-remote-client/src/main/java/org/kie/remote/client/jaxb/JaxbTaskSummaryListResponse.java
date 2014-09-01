package org.kie.remote.client.jaxb;

import static org.kie.services.client.api.command.AbstractRemoteCommandObject.unsupported;

import java.util.ArrayList;
import java.util.Collection;
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
public class JaxbTaskSummaryListResponse extends AbstractJaxbCommandResponse<List<TaskSummary>> implements JaxbPaginatedList<JaxbTaskSummary> {

    @XmlElements({
        @XmlElement(name="task-summary",type=JaxbTaskSummary.class)
    })
    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    private List<JaxbTaskSummary> taskSummaryList;

    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
    public JaxbTaskSummaryListResponse() { 
        this.taskSummaryList = new ArrayList<JaxbTaskSummary>();
    }
    
    public JaxbTaskSummaryListResponse(Collection<JaxbTaskSummary> taskSummaryCollection) { 
       this.taskSummaryList = new ArrayList<JaxbTaskSummary>(taskSummaryCollection);
    }
    
    public JaxbTaskSummaryListResponse(List<JaxbTaskSummary> taskSummaryCollection, int i, Command<?> cmd ) { 
        super(i, cmd);
       this.taskSummaryList = new ArrayList<JaxbTaskSummary>(taskSummaryCollection);
    }
    
    @Override
    public List<org.kie.api.task.model.TaskSummary> getResult() {
        List<org.kie.api.task.model.TaskSummary> resultList = new ArrayList<org.kie.api.task.model.TaskSummary>();
        if( this.taskSummaryList == null || this.taskSummaryList.isEmpty() ) { 
            return resultList;
        }
        for( JaxbTaskSummary jaxbTaskSum : this.taskSummaryList ) { 
           resultList.add(jaxbTaskSum);
        }
        return resultList;
    }

    @Override
    public void setResult(List<org.kie.api.task.model.TaskSummary> result) {
        unsupported(JaxbTaskSummaryListResponse.class, Void.class);
    }

    @Override
    public void addContents(List<JaxbTaskSummary> contentList) {
        this.taskSummaryList = new ArrayList<JaxbTaskSummary>(contentList);
    }

    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    public List<JaxbTaskSummary> getList() {
        return taskSummaryList;
    }

    @JsonTypeInfo(defaultImpl=JaxbTaskSummary.class, use=Id.CLASS)
    public void setList(List<JaxbTaskSummary> result) {
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
