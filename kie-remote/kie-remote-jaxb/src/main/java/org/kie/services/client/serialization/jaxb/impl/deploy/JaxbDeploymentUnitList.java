package org.kie.services.client.serialization.jaxb.impl.deploy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;

@XmlRootElement(name="deployment-unit-list")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={JaxbDeploymentUnit.class})
public class JaxbDeploymentUnitList implements JaxbPaginatedList<JaxbDeploymentUnit> {

    @XmlElements({
        @XmlElement(name ="deployment-unit", type=JaxbDeploymentUnit.class)
    })
    private List<JaxbDeploymentUnit> deploymentUnitList = new ArrayList<JaxbDeploymentUnit>();

    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
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

    @Override
    public Integer getPageNumber() {
        return pageNumber;
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

    @Override
    public void addContents(List<JaxbDeploymentUnit> contentList) {
        this.deploymentUnitList = contentList;
    }
    
}
