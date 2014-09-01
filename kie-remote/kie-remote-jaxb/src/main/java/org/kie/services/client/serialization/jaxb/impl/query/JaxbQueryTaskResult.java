package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name="query-task-result")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({JaxbQueryTaskInfo.class})
public class JaxbQueryTaskResult {

    @XmlElement
    private List<JaxbQueryTaskInfo> taskInfoList;

    public JaxbQueryTaskResult() { 
        // default for JAXB
    }

    public List<JaxbQueryTaskInfo> getTaskInfoList() {
        return taskInfoList;
    }

    public void setTaskInfoList( List<JaxbQueryTaskInfo> taskInfoList ) {
        this.taskInfoList = taskInfoList;
    }

}
