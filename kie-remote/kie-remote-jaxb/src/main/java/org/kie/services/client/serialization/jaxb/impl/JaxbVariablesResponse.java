/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.services.client.serialization.jaxb.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.kie.api.command.Command;
import org.kie.internal.jaxb.StringKeyObjectValueMapXmlAdapter;

@XmlRootElement(name="variables-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbVariablesResponse extends AbstractJaxbCommandResponse<Map<String,String>> {

    @XmlElement
    @XmlJavaTypeAdapter(value=StringKeyObjectValueMapXmlAdapter.class)
    private Map<String, String> variables = new HashMap<String, String>();
    
    public JaxbVariablesResponse() {
    }
    
    public JaxbVariablesResponse(Command<?> cmd, Map<String, String> variables) {
       this.commandName = cmd.getClass().getSimpleName();
       this.variables = variables;
    }

    public JaxbVariablesResponse(Map<String, String> variables, String requestUrl) { 
        this.url = requestUrl;
        this.status = JaxbRequestStatus.SUCCESS;
    }
    
    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public Map<String, String> getResult() {
        return variables;
    }

    @Override
    public void setResult(Map<String, String> variables) {
        this.variables = variables;
    }
    
}
