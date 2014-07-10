package org.kie.services.client.serialization.jaxb.impl.deploy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlRootElement(name="deployment-unit-list")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={JaxbDeploymentUnit.class})
public class JaxbDeploymentUnitList {

    @XmlElements({
        @XmlElement(name ="deployment-unit", type=JaxbDeploymentUnit.class)
    })
    private List<JaxbDeploymentUnit> deploymentUnitList = new ArrayList<JaxbDeploymentUnit>();

    public JaxbDeploymentUnitList() { 
        // default for JAXB, etc.
    }
    
    public JaxbDeploymentUnitList(List<JaxbDeploymentUnit> depUnitList) {
        this.deploymentUnitList = depUnitList;
    }
    
    public List<JaxbDeploymentUnit> getDeploymentUnitList() {
        return deploymentUnitList;
    }

    public void setDeploymentUnitList(List<JaxbDeploymentUnit> deploymentUnitList) {
        this.deploymentUnitList = deploymentUnitList;
    }
    
}
