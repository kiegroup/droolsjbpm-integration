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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

@XmlRootElement(name="primitive-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbPrimitiveResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlAttribute
    @XmlSchemaType(name="string")
    private Class<?> type;
    
    @XmlElement
    @XmlSchemaType(name="base64Binary")
    private Object result;
    
    public JaxbPrimitiveResponse() {
    }
    
    public JaxbPrimitiveResponse(Object result, int i, Command<?> cmd) {
       super(i, cmd);
       this.result = result;
       this.type = this.result.getClass();
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

}
