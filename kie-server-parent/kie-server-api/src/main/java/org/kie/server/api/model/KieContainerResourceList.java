package org.kie.server.api.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="kie-containers")
@XStreamAlias( "kie-containers" )
@XmlAccessorType (XmlAccessType.FIELD)
public class KieContainerResourceList {
    
    @XmlElement(name="kie-container")
    @XStreamImplicit(itemFieldName = "kie-container")
    private List<KieContainerResource> containers;

    public KieContainerResourceList() {
        super();
        containers = new ArrayList<KieContainerResource>();
    }

    public KieContainerResourceList(List<KieContainerResource> containers) {
        super();
        this.containers = containers;
    }
    
    public List<KieContainerResource> getContainers() {
        return containers;
    }
    
    public void setContainers(List<KieContainerResource> containers) {
        this.containers = containers;
    }

}
