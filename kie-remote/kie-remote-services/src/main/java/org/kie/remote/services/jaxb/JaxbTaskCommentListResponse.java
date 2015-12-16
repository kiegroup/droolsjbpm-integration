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

package org.kie.remote.services.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.jbpm.services.task.impl.model.xml.JaxbComment;
import org.kie.api.command.Command;
import org.kie.api.task.model.Comment;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;

@XmlRootElement(name="task-comment-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbTaskCommentListResponse extends AbstractJaxbCommandResponse<List<Comment>> implements JaxbPaginatedList<Comment> {

    @XmlElements({
        @XmlElement(name="task-comment",type=JaxbComment.class)
    })
    @JsonTypeInfo(defaultImpl=JaxbComment.class, use=Id.CLASS)
    private List<Comment> commentList;

    @XmlElement(name="page-number")
    @XmlSchemaType(name="int")
    private Integer pageNumber;
    
    @XmlElement(name="page-size")
    @XmlSchemaType(name="int")
    private Integer pageSize;
    
    public JaxbTaskCommentListResponse() { 
        this.commentList = new ArrayList<Comment>();
    }
    
    public JaxbTaskCommentListResponse(Collection<Comment> commentCollection) { 
       this.commentList = convertToJaxbComment(commentCollection);
    }
    
    public JaxbTaskCommentListResponse(List<Comment> commentCollection, int i, Command<?> cmd ) { 
        super(i, cmd);
       this.commentList = convertToJaxbComment(commentCollection);
    }
    
    private List<Comment> convertToJaxbComment(Collection<Comment> list) {
        if( list == null || list.isEmpty() ) { 
            return new ArrayList<Comment>();
        }
        List<Comment> newList = new ArrayList<Comment>(list.size());
        Iterator<Comment> iter = list.iterator();
        while(iter.hasNext()) { 
            Comment comment = iter.next();
            if( comment instanceof JaxbComment ) { 
                newList.add(comment);
            } else { 
                newList.add(new JaxbComment(comment));
            }
        }
        return newList;
    }
    
    
    @Override
    public List<Comment> getResult() {
        return commentList;
    }

    @Override
    public void setResult(List<Comment> result) {
        this.commentList = convertToJaxbComment(result);
    }

    @Override
    public void addContents(List<Comment> contentList) {
        this.commentList = convertToJaxbComment(contentList);
    }

    @JsonTypeInfo(defaultImpl=JaxbComment.class, use=Id.CLASS)
    public List<Comment> getList() {
        return commentList;
    }

    @JsonTypeInfo(defaultImpl=JaxbComment.class, use=Id.CLASS)
    public void setList(List<Comment> result) {
        this.commentList = result;
    }

    @Override
    public Integer getPageNumber() {
        return this.pageNumber;
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

}
