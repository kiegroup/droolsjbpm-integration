package org.kie.remote.services.ws.sei.deployment;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessIdsResponse", propOrder = {
    "processId",
})
public class ProcessIdsResponse {

    @XmlElement
    private List<String> processId;
    
}
