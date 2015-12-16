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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.rest.AbstractJaxbResponse;


@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"result"})
public abstract class AbstractJaxbCommandResponse<T> extends AbstractJaxbResponse implements JaxbCommandResponse<T> {

    @XmlAttribute
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlElement(name="command-name")
    @XmlSchemaType(name="string")
    protected String commandName;

    public AbstractJaxbCommandResponse() { 
       // Default constructor 
    }
   
    public AbstractJaxbCommandResponse(Integer i, Command<?> cmd) { 
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }
    
    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getIndex()
     */
    @Override
    public Integer getIndex() {
        return index;
    }

    /*
     * (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#setIndex(java.lang.Integer)
     */
    @Override
    public void setIndex(Integer index) {
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    /*
     * (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#setCommandName(java.lang.String)
     */
    @Override
    public void setCommandName(String cmdName) { 
        this.commandName = cmdName;
    }

}
