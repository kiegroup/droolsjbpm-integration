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

import java.util.ArrayList;
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
import org.kie.api.command.Command;
import org.kie.api.task.model.User;
import org.kie.remote.jaxb.gen.Comment;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;

@XmlRootElement(name="task-comment-list-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonAutoDetect(getterVisibility=JsonAutoDetect.Visibility.NONE, fieldVisibility=JsonAutoDetect.Visibility.ANY)
public class JaxbTaskCommentListResponse extends AbstractJaxbCommandResponse<List<org.kie.api.task.model.Comment>> implements JaxbPaginatedList<Comment> {

    @XmlElements({
        @XmlElement(name="task-comment",type=Comment.class)
    })
    @JsonTypeInfo(defaultImpl=Comment.class, use=Id.CLASS)
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
    
    public JaxbTaskCommentListResponse(List<Comment> commentList) { 
       this.commentList = commentList;
    }
    
    public JaxbTaskCommentListResponse(List<Comment> commentList, int i, Command<?> cmd ) { 
        super(i, cmd);
       this.commentList = commentList;
    }
    
    @Override
    public List<org.kie.api.task.model.Comment> getResult() {
        List<org.kie.api.task.model.Comment> result = new ArrayList<org.kie.api.task.model.Comment>(commentList.size());
        for( Comment genComment : commentList ) { 
           result.add(new JaxbWrapper.JaxbCommentWrapper(genComment));
        }
        return result;
    }

    @Override
    public void setResult(List<org.kie.api.task.model.Comment> result) {
        List<Comment> newCommentList = null;
        if( result != null ) { 
            newCommentList = new ArrayList<Comment>(result.size());
            for( org.kie.api.task.model.Comment kieComment : result ) { 
                Comment genComment = new Comment();
                genComment.setAddedAt(ConversionUtil.convertDateToXmlGregorianCalendar(kieComment.getAddedAt()));
                User addedByUser = kieComment.getAddedBy();
                if( addedByUser != null ) { 
                    genComment.setAddedBy(addedByUser.getId());
                }
                genComment.setId(genComment.getId());
                genComment.setText(genComment.getText());
                newCommentList.add(genComment);
            }
        }
        this.commentList = newCommentList;
    }

    @Override
    public void addContents(List<Comment> contentList) {
        this.commentList = contentList;
    }

    @JsonTypeInfo(defaultImpl=Comment.class, use=Id.CLASS)
    public List<Comment> getList() {
        return commentList;
    }

    @JsonTypeInfo(defaultImpl=Comment.class, use=Id.CLASS)
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
