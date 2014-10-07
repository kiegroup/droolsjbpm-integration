package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name="query-process-instance-result")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({JaxbQueryProcessInstanceInfo.class})
public class JaxbQueryProcessInstanceResult {

    @XmlElement
    private List<JaxbQueryProcessInstanceInfo> processInstanceInfoList = new ArrayList<JaxbQueryProcessInstanceInfo>();

    public JaxbQueryProcessInstanceResult() { 
        // default for JAXB
    }
    
    public List<JaxbQueryProcessInstanceInfo> getProcessInstanceInfoList() {
        return processInstanceInfoList;
    }

    public void setProcessInstanceInfoList( List<JaxbQueryProcessInstanceInfo> processInstanceInfoList ) {
        this.processInstanceInfoList = processInstanceInfoList;
    }
}
