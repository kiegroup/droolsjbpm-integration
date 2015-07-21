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

package org.kie.remote.client.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.User;
import org.kie.remote.client.jaxb.JaxbWrapper.JaxbCommentWrapper;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name = "task-comment-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskCommentResponse extends org.kie.remote.jaxb.gen.Comment implements JaxbCommandResponse<Comment> {

    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;

    public JaxbTaskCommentResponse() {
        // Default constructor
    }

    public JaxbTaskCommentResponse(int i, Command<?> cmd) {
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getIndex()
     */
    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex(Integer index) {
        this.index = index;
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String cmdName) {
        this.commandName = cmdName;
    }
    
    public JaxbTaskCommentResponse(Comment comment, int i, Command<?> cmd) {
        setCommentFields(comment);
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    private void setCommentFields(Comment comment) { 
        this.addedAt = ConversionUtil.convertDateToXmlGregorianCalendar(comment.getAddedAt());
        User addedByUser = comment.getAddedBy();
        if( addedByUser != null ) { 
            this.addedBy = addedByUser.getId();
        }
        this.id = comment.getId();
        this.text = comment.getText();
    }
    
    @Override
    public Comment getResult() {
        return new JaxbCommentWrapper(this);
    }

    @Override
    public void setResult( Comment result ) {
        setCommentFields(result);
    }
    
}