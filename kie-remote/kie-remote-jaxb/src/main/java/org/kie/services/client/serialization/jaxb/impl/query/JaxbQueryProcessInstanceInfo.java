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

package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;

@XmlRootElement(name="query-process-instance-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbQueryProcessInstanceInfo {

    @XmlElement(name="process-instance")
    private JaxbProcessInstance processInstance;
    
    @XmlElement
    private List<JaxbVariableInfo> variables = new ArrayList<JaxbVariableInfo>();

    public JaxbQueryProcessInstanceInfo() {
        // default for JAXB
    }
   
    public JaxbProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance( JaxbProcessInstance processInstance ) {
        this.processInstance = processInstance;
    }

    public List<JaxbVariableInfo> getVariables() {
        return variables;
    }

    public void setVariables( List<JaxbVariableInfo> variables ) {
        this.variables = variables;
    }
}
