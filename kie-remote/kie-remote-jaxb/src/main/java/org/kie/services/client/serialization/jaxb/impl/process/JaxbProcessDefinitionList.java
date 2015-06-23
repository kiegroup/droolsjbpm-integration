/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.serialization.jaxb.impl.process;

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

@XmlRootElement(name="process-definition-list")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value={JaxbProcessDefinition.class})
public class JaxbProcessDefinitionList implements JaxbPaginatedList<JaxbProcessDefinition> {

    @XmlElements({
        @XmlElement(name ="process-definition", type=JaxbProcessDefinition.class)
    })
    private List<JaxbProcessDefinition> processDefinitionList = new ArrayList<JaxbProcessDefinition>();

    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
    public  JaxbProcessDefinitionList() { 
        // default for JAXB, etc.
    }
    
    public JaxbProcessDefinitionList(List<JaxbProcessDefinition> procDefList) {
        this.processDefinitionList = procDefList;
    }

    public List<JaxbProcessDefinition> getProcessDefinitionList() {
        return processDefinitionList;
    }

    public void setProcessDefinitionList(List<JaxbProcessDefinition> processDefinitionList) {
        this.processDefinitionList = processDefinitionList;
    }

    @Override
    public void addContents(List<JaxbProcessDefinition> contentList) {
        this.processDefinitionList = contentList;
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
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}