package org.kie.services.client.serialization.jaxb.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jbpm.services.task.impl.model.xml.adapter.StringObjectMapXmlAdapter;
import org.kie.api.command.Command;
import org.kie.api.runtime.process.WorkItem;

@XmlRootElement(name = "work-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbWorkItem extends AbstractJaxbCommandResponse<WorkItem> implements WorkItem {

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String name;
    
    @XmlElement
    @XmlSchemaType(name="int")
    private Integer state = 0;
    
    @XmlElement(name="param-map")
    @XmlJavaTypeAdapter(StringObjectMapXmlAdapter.class)
    private Map<String, Object> parameters = new HashMap<String, Object>();
    
    @XmlElement(name="results-map")
    @XmlJavaTypeAdapter(StringObjectMapXmlAdapter.class)
    private Map<String, Object> results = new HashMap<String, Object>();
    
    @XmlElement
    @XmlSchemaType(name="long")
    private Long processInstanceId;

    public JaxbWorkItem() { 
        // Default
    }
    
    public JaxbWorkItem(WorkItem workItem) { 
        setResult(workItem);
    }
    
    public JaxbWorkItem(WorkItem result, int i, Command<?> cmd) {
        super(i, cmd);
        setResult(result);
    }
    
    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    // JaxbCommmandResponse methods
    
    public WorkItem getResult() {
        return this;
    }

    @Override
    public void setResult(WorkItem result) {
        this.id = result.getId();
        this.name = result.getName();
        this.parameters = result.getParameters();
        this.processInstanceId = result.getProcessInstanceId();
        this.results = result.getResults();
        this.state = result.getState();
    }

    @Override
    public Object getResult(String name) {
        return this;
    }

}
