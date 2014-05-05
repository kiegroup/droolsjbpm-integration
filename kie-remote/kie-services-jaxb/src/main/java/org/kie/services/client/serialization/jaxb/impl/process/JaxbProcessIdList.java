package org.kie.services.client.serialization.jaxb.impl.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="process-id-list")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcessIdList {

    @XmlElement(name="process-id")
    private List<String> processIdList = new ArrayList<String>();

    public JaxbProcessIdList() { 
        // default for JAXB, etc.
    }
    
    public JaxbProcessIdList(Collection<String> processIdList) {
        this.processIdList = new ArrayList<String>(processIdList);
    }

    public List<String> getProcessIdList() {
        return processIdList;
    }

    public void setProcessIdList(List<String> processIdList) {
        this.processIdList = processIdList;
    }
    
    
}
