/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.kie.services.client.serialization.jaxb.adapter.XmlCdataAdapter;

import com.sun.xml.txw2.annotation.XmlCDATA;

@XmlRootElement(name="process-definition-source")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbProcessDefinitionSource implements Serializable {
    
    /** Generated serial version UID */
    private static final long serialVersionUID = 8300338416158907591L;
  
    @XmlJavaTypeAdapter(value=XmlCdataAdapter.class)
    private String source;

    public JaxbProcessDefinitionSource() { 
        // default constructor for JAXB, etc..
    }
   
    public JaxbProcessDefinitionSource(String bpmn2Source) { 
        this.source = bpmn2Source;
    }
   
    @XmlCDATA
    public String getSource() {
        return source;
    }

    @XmlCDATA
    public void setSource(String source) {
        this.source = source;
    }
   
}