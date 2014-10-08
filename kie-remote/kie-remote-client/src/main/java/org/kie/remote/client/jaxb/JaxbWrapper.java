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
    static class JaxbTaskWrapper extends JaxbWrapper implements Task {
    
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
   
        public org.kie.remote.jaxb.gen.Task getInternalTask() { 
           return this.task; 
        }
    }

    /**
     * Represents a {@link TaskData} instance
     */
    static class JaxbTaskDataWrapper extends JaxbWrapper implements TaskData {
    
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
        public int getProcessSessionId() {
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
        public long getOutputContentId() {
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
        
        public org.kie.remote.jaxb.gen.TaskData getInternalTaskData() { 
            return this.taskData;
        }
    }

    static class JaxbAttachmentWrapper extends JaxbWrapper implements Attachment {
    
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
        
        public org.kie.remote.jaxb.gen.Attachment getInternalAttachment() { 
            return this.attachment;
        }
    }

    static class JaxbCommentWrapper extends JaxbWrapper implements Comment {
    
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
        
        public org.kie.remote.jaxb.gen.Comment getInternalComment() { 
            return this.comment;
        }
    }

    static class JaxbContentWrapper extends JaxbWrapper implements Content {

        private final org.kie.remote.jaxb.gen.Content content;

        public JaxbContentWrapper(org.kie.remote.jaxb.gen.Content content) {
            super(Content.class);
            this.content = content;
        }

        @Override
        public long getId() {
            return content.getId();
        }

        @Override
        public byte[] getContent() {
            return content.getContent();
        }
       
        public org.kie.remote.jaxb.gen.Content getInternalContent() { 
            return this.content;
        }
        
    }

    static class JaxbI18NTextWrapper extends JaxbWrapper implements I18NText {
    
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
        
        public org.kie.remote.jaxb.gen.I18NText getInternalI18nText() { 
           return this.i18nText; 
        }
    }

    static class JaxbPeopleAssignmentsWrapper extends JaxbWrapper implements PeopleAssignments {
    
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
        
        public org.kie.remote.jaxb.gen.PeopleAssignments getInternalPeopleAssignments() { 
            return this.peopleAssignments;
        }
    }

    /**
     * Represents a {@link Group} instance
     */
    static class GroupWrapper extends JaxbWrapper implements Group {
        
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
    static class UserWrapper extends JaxbWrapper implements User {
        
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
