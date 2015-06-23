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

package org.kie.services.client.serialization.jaxb.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.kie.api.command.Command;

@XmlRootElement(name="string-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbStringListResponse extends AbstractJaxbCommandResponse<List<String>> {

    @XmlElement(name="string")
    @XmlSchemaType(name="string")
    private List<String> resultList;
    
    public JaxbStringListResponse() {
    }
    
    public JaxbStringListResponse(List<String> result, int i, Command<?> cmd) {
       super(i, cmd);
       this.resultList = result;
    }

    @Override
    public List<String> getResult() {
        return resultList;
    }

    @Override
    public void setResult(List<String> result) {
        this.resultList = result;
    }

}
