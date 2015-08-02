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

import static org.kie.remote.client.jaxb.ConversionUtil.convertXmlGregCalToDate;
import static org.kie.services.client.api.command.AbstractRemoteCommandObject.unsupported;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.Delegation;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.SubTasksStrategy;
import org.kie.remote.jaxb.gen.Type;

abstract class JaxbWrapper {

    private final Class representingClass;
    
    public JaxbWrapper(Class repClass) {
        this.representingClass = repClass;
    }
    
    public void writeExternal( ObjectOutput out ) throws IOException {
        unsupported(representingClass, Void.class);
    }

    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
        unsupported(representingClass, Void.class);
    }
   
    protected static List<OrganizationalEntity> convertGenOrgEngListToOrgEntList(List<org.kie.remote.jaxb.gen.OrganizationalEntity> genOrgEntList) { 
        List<OrganizationalEntity> orgEntList = new ArrayList<OrganizationalEntity>();
        if( genOrgEntList == null || genOrgEntList.isEmpty() ) { 
            return orgEntList;
        }
        for( org.kie.remote.jaxb.gen.OrganizationalEntity genOrgEnt : genOrgEntList ) { 
           if( Type.USER.equals(genOrgEnt.getType()) ) { 
               orgEntList.add( new GroupWrapper(genOrgEnt.getId()));
           } else if( Type.GROUP.equals(genOrgEnt.getType()) ) {  
               orgEntList.add( new UserWrapper(genOrgEnt.getId()));
           } else { 
               throw new IllegalStateException("Unknown organizational entity type: " + genOrgEnt.getType().toString() );
           }
        }
        return orgEntList;
    }

    protected static List<I18NText> convertGenI18NTextToI18NText(List<org.kie.remote.jaxb.gen.I18NText> jaxbTextList) { 
        List<I18NText> textList = new ArrayList<I18NText>();
        if( jaxbTextList == null || jaxbTextList.isEmpty() ) { 
           return textList; 
        }
        for( org.kie.remote.jaxb.gen.I18NText jaxbText : jaxbTextList ) { 
            textList.add(new JaxbI18NTextWrapper(jaxbText));
        }
        return textList;
    }

    protected static User convertStringIdToUser(String userId) { 
        if( userId == null ) { 
            return null;
        }
        return new UserWrapper(userId);
    }
   

    /**
     * Represents a {@link Task} instance
     */
    final static class JaxbTaskWrapper extends JaxbWrapper implements InternalTask {
    
        private final org.kie.remote.jaxb.gen.Task task;
    
        public JaxbTaskWrapper(org.kie.remote.jaxb.gen.Task task) {
            super(Task.class);
            this.task = task;
        }
    
        @Override
        public Long getId() {
            return this.task.getId();
        }
    
        @Override
        public int getPriority() {
            return this.task.getPriority();
        }
    
        @Override
        public List<I18NText> getNames() {
            return convertGenI18NTextToI18NText(this.task.getNames());
        }
    
        @Override
        public List<I18NText> getSubjects() {
            return convertGenI18NTextToI18NText(this.task.getSubjects());
        }
    
        @Override
        public List<I18NText> getDescriptions() {
            return convertGenI18NTextToI18NText(this.task.getDescriptions());
        }
    
        @Override
        public String getName() {
            return this.task.getName();
        }
    
        @Override
        public String getSubject() {
            return this.task.getSubject();
        }
    
        @Override
        public String getDescription() {
            return this.task.getDescription();
        }
    
        @Override
        public PeopleAssignments getPeopleAssignments() {
            return new JaxbPeopleAssignmentsWrapper(this.task.getPeopleAssignments());
        }
    
        @Override
        public TaskData getTaskData() {
            return new JaxbTaskDataWrapper(this.task.getTaskData());
        }
    
        @Override
        public String getTaskType() {
            return this.task.getTaskType();
        }
   
        @Override
        public void setId( long id ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public Boolean isArchived() {
            return unsupported(InternalTask.class, Boolean.class);
        }

        @Override
        public void setArchived( Boolean archived ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public int getVersion() {
            return unsupported(InternalTask.class, int.class);
        }

        @Override
        public void setPriority( int priority ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setNames( List<I18NText> names ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setFormName( String formName ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public String getFormName() {
            return this.task.getFormName();
        }

        @Override
        public void setSubjects( List<I18NText> subjects ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setDescriptions( List<I18NText> descriptions ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setPeopleAssignments( PeopleAssignments peopleAssignments ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public Delegation getDelegation() {
            return unsupported(InternalTask.class, Delegation.class);
        }

        @Override
        public void setDelegation( Delegation delegation ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setTaskData( TaskData taskData ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public Deadlines getDeadlines() {
            return unsupported(InternalTask.class, Deadlines.class);
        } 

        @Override
        public void setDeadlines( Deadlines deadlines ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setTaskType( String taskType ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public SubTasksStrategy getSubTaskStrategy() {
            return unsupported(InternalTask.class, SubTasksStrategy.class);
        }

        @Override
        public void setSubTaskStrategy( SubTasksStrategy subTaskStrategy ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setName( String name ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setSubject( String subject ) {
            unsupported(InternalTask.class, Void.class);
        }

        @Override
        public void setDescription( String description ) {
            unsupported(InternalTask.class, Void.class);
        }
    }
   
    /**
     * Represents a {@link TaskData} instance
     */
    final static class JaxbTaskDataWrapper extends JaxbWrapper implements TaskData {
    
        private final org.kie.remote.jaxb.gen.TaskData taskData;
    
        JaxbTaskDataWrapper(org.kie.remote.jaxb.gen.TaskData genTaskData) {
            super(TaskData.class);
            this.taskData = genTaskData;
        }
    
        @Override
        public Status getStatus() {
            return this.taskData.getStatus();
        }
    
        @Override
        public Status getPreviousStatus() {
            return this.taskData.getPreviousStatus();
        }
    
        @Override
        public User getActualOwner() {
            return convertStringIdToUser(this.taskData.getActualOwner());
        }
    
        @Override
        public User getCreatedBy() {
            return convertStringIdToUser(this.taskData.getCreatedBy());
        }
    
        @Override
        public Date getCreatedOn() {
            return convertXmlGregCalToDate(this.taskData.getCreatedOn());
        }
    
        @Override
        public Date getActivationTime() {
            return convertXmlGregCalToDate(this.taskData.getActivationTime());
        }
    
        @Override
        public Date getExpirationTime() {
            return convertXmlGregCalToDate(this.taskData.getExpirationTime());
        }
    
        @Override
        public boolean isSkipable() {
            return this.taskData.isSkipable();
        }
    
        @Override
        public long getWorkItemId() {
            return this.taskData.getWorkItemId();
        }
    
        @Override
        public long getProcessInstanceId() {
            return this.taskData.getProcessInstanceId();
        }
    
        @Override
        public String getProcessId() {
            return this.taskData.getProcessId();
        }
    
        @Override
        public String getDeploymentId() {
            return this.taskData.getDeploymentId();
        }
    
        @Override
        public long getProcessSessionId() {
            return this.taskData.getProcessSessionId();
        }
    
        @Override
        public String getDocumentType() {
            return this.taskData.getDocumentType();
        }
    
        @Override
        public long getDocumentContentId() {
            return this.taskData.getDocumentContentId();
        }
    
        @Override
        public String getOutputType() {
            return this.taskData.getOutputType();
        }
    
        @Override
        public Long getOutputContentId() {
            return this.taskData.getOutputContentId();
        }
    
        @Override
        public String getFaultName() {
            return this.taskData.getFaultName();
        }
    
        @Override
        public String getFaultType() {
            return this.taskData.getFaultType();
        }
    
        @Override
        public long getFaultContentId() {
            return this.taskData.getFaultContentId();
        }
    
        @Override
        public List<Comment> getComments() {
            List<Comment> commentList = new ArrayList<Comment>();
            if( this.taskData.getComments() == null || this.taskData.getComments().isEmpty() ) {
                return commentList;
            }
            for( org.kie.remote.jaxb.gen.Comment jaxbComment : this.taskData.getComments() ) {
                commentList.add(new JaxbCommentWrapper(jaxbComment));
            }
            return commentList;
        }
    
        @Override
        public List<Attachment> getAttachments() {
            List<Attachment> attachmentList = new ArrayList<Attachment>();
            if( this.taskData.getAttachments() == null || this.taskData.getAttachments().isEmpty() ) {
                return attachmentList;
            }
            for( org.kie.remote.jaxb.gen.Attachment jaxbAttachment : this.taskData.getAttachments() ) {
                attachmentList.add(new JaxbAttachmentWrapper(jaxbAttachment));
            }
            return attachmentList;
        }
    
        @Override
        public long getParentId() {
            return this.taskData.getParentId();
        }
        
    }

    final static class JaxbAttachmentWrapper extends JaxbWrapper implements Attachment {
    
        private final org.kie.remote.jaxb.gen.Attachment attachment;
        
        public JaxbAttachmentWrapper(org.kie.remote.jaxb.gen.Attachment jaxbAttachment) { 
            super(Attachment.class);
            this.attachment = jaxbAttachment;
        }
    
        @Override
        public Long getId() {
            return this.attachment.getId();
        }
    
        @Override
        public String getName() {
            return this.attachment.getName();
        }
    
        @Override
        public String getContentType() {
            return this.attachment.getContentType();
        }
    
        @Override
        public Date getAttachedAt() {
            return convertXmlGregCalToDate(this.attachment.getAttachedAt());
        }
    
        @Override
        public User getAttachedBy() {
            return convertStringIdToUser(this.attachment.getAttachedBy());
        }
    
        @Override
        public int getSize() {
            return this.attachment.getSize();
        }
    
        @Override
        public long getAttachmentContentId() {
            return this.attachment.getAttachmentContentId();
        }
    }

    final static class JaxbCommentWrapper extends JaxbWrapper implements Comment {
    
        private final org.kie.remote.jaxb.gen.Comment comment;
    
        public JaxbCommentWrapper(org.kie.remote.jaxb.gen.Comment jaxbComment) {
            super(Comment.class);
            this.comment = jaxbComment;
        }
    
        @Override
        public Long getId() {
            return this.comment.getId();
        }
    
        @Override
        public String getText() {
            return this.comment.getText();
        }
    
        @Override
        public Date getAddedAt() {
            return convertXmlGregCalToDate(this.comment.getAddedAt());
        }
    
        @Override
        public User getAddedBy() {
            return convertStringIdToUser(this.comment.getAddedBy());
        }
    }

    final static class JaxbContentWrapper extends JaxbWrapper implements Content {

        private final org.kie.remote.jaxb.gen.Content content;

        public JaxbContentWrapper(org.kie.remote.jaxb.gen.Content content) {
            super(Content.class);
            this.content = content;
        }

        @Override
        public Long getId() {
            return content.getId();
        }

        @Override
        public byte[] getContent() {
            return content.getContent();
        }
       
    }

    final static class JaxbI18NTextWrapper extends JaxbWrapper implements I18NText {
    
        private final org.kie.remote.jaxb.gen.I18NText i18nText;
    
        public JaxbI18NTextWrapper(org.kie.remote.jaxb.gen.I18NText text) {
            super(I18NText.class);
            this.i18nText = text;
        }
    
        @Override
        public Long getId() {
            return this.i18nText.getId();
        }
    
        @Override
        public String getLanguage() {
            return this.i18nText.getLanguage();
        }
    
        @Override
        public String getText() {
            return this.i18nText.getText();
        }
    }

    final static class JaxbPeopleAssignmentsWrapper extends JaxbWrapper implements InternalPeopleAssignments {
    
        private final org.kie.remote.jaxb.gen.PeopleAssignments peopleAssignments;
    
        public JaxbPeopleAssignmentsWrapper(org.kie.remote.jaxb.gen.PeopleAssignments peopAssign) {
            super(PeopleAssignments.class);
            this.peopleAssignments = peopAssign;
        }
    
        @Override
        public User getTaskInitiator() {
            return convertStringIdToUser(peopleAssignments.getTaskInitiatorId());
        }
    
        @Override
        public List<OrganizationalEntity> getPotentialOwners() {
            return convertGenOrgEngListToOrgEntList(peopleAssignments.getPotentialOwners());
        }
    
        @Override
        public List<OrganizationalEntity> getBusinessAdministrators() {
            return convertGenOrgEngListToOrgEntList(peopleAssignments.getBusinessAdministrators());
        }
        
        @Override
        public void setTaskInitiator( User taskInitiator ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }

        @Override
        public void setPotentialOwners( List<OrganizationalEntity> potentialOwners ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }

        @Override
        public List<OrganizationalEntity> getExcludedOwners() {
            return convertGenOrgEngListToOrgEntList(peopleAssignments.getExcludedOwners());
        }

        @Override
        public void setExcludedOwners( List<OrganizationalEntity> excludedOwners ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }

        @Override
        public List<OrganizationalEntity> getTaskStakeholders() {
            return convertGenOrgEngListToOrgEntList(peopleAssignments.getTaskStakeholders());
        }

        @Override
        public void setTaskStakeholders( List<OrganizationalEntity> taskStakeholders ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }

        @Override
        public void setBusinessAdministrators( List<OrganizationalEntity> businessAdministrators ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }

        @Override
        public List<OrganizationalEntity> getRecipients() {
            return convertGenOrgEngListToOrgEntList(peopleAssignments.getRecipients());
        }

        @Override
        public void setRecipients( List<OrganizationalEntity> recipients ) {
            unsupported(InternalPeopleAssignments.class, Void.class);
        }
    }

    /**
     * Represents a {@link Group} instance
     */
    final static class GroupWrapper extends JaxbWrapper implements Group {
        
        private final String id;
        public GroupWrapper(String id) {
            super(Group.class);
            this.id = id;
        }
    
        @Override
        public String getId() {
            return id;
        }
    }

    /**
     * Represents a {@link User} instance
     */
    final static class UserWrapper extends JaxbWrapper implements User {
        
        private final String id;
        public UserWrapper(String id) {
            super(User.class);
            this.id = id;
        }
    
        @Override
        public String getId() {
            return id;
        }
    }
}
