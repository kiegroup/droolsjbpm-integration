package org.kie.services.client.serialization.jaxb.impl.deploy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="deployment-unit-list")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDeploymentUnitList {

    @XmlElement(name ="deployment-unit")
    private List<JaxbDeploymentUnit> deploymentUnitList = new ArrayList<JaxbDeploymentUnit>();

    public JaxbDeploymentUnitList() { 
        // default for JAXB, etc.
    }
    
    public List<JaxbDeploymentUnit> getDeploymentUnitList() {
        return deploymentUnitList;
    }

    public void setDeploymentUnitList(List<JaxbDeploymentUnit> deploymentUnitList) {
        this.deploymentUnitList = deploymentUnitList;
    }
    
}
