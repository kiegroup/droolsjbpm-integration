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
import org.kie.api.task.model.TaskSummary;
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

    protected static Status convertGenStatusToStatus(org.kie.remote.jaxb.gen.Status genStatus) { 
        if( genStatus == null ) { 
            return null;
        }
         return Status.valueOf(genStatus.value());
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
     * Represents a {@link TaskSummary} instance
     */
    static class JaxbTaskSummaryWrapper extends JaxbWrapper implements TaskSummary {
    
        private final org.kie.remote.jaxb.gen.TaskSummary genJaxbTaskSummary;
    
        public JaxbTaskSummaryWrapper(org.kie.remote.jaxb.gen.TaskSummary genJaxbTaskSum) {
            super(TaskSummary.class);
            this.genJaxbTaskSummary = genJaxbTaskSum;
        }
    
        @Override
        public String getStatusId() {
            return genJaxbTaskSummary.getStatus().toString();
        }
    
        @Override
        public Long getId() {
            return genJaxbTaskSummary.getId();
        }
    
        @Override
        public String getName() {
            return genJaxbTaskSummary.getName();
        }
    
        @Override
        public Integer getPriority() {
            return genJaxbTaskSummary.getPriority();
        }
    
        @Override
        public String getActualOwnerId() {
            return genJaxbTaskSummary.getActualOwner();
        }
    
        @Override
        public String getCreatedById() {
            return genJaxbTaskSummary.getCreatedBy();
        }
    
        @Override
        public Date getCreatedOn() {
            return convertXmlGregCalToDate(genJaxbTaskSummary.getCreatedOn());
        }
    
        @Override
        public Date getActivationTime() {
            return convertXmlGregCalToDate(genJaxbTaskSummary.getActivationTime());
        }
    
        @Override
        public Date getExpirationTime() {
            return convertXmlGregCalToDate(genJaxbTaskSummary.getExpirationTime());
        }
    
        @Override
        public String getProcessId() {
            return genJaxbTaskSummary.getProcessId();
        }
    
        @Override
        public Long getProcessInstanceId() {
            return genJaxbTaskSummary.getProcessInstanceId();
        }
    
        @Override
        public String getDeploymentId() {
            return genJaxbTaskSummary.getDeploymentId();
        }
    
        @Override
        public Long getParentId() {
            return genJaxbTaskSummary.getParentId();
        }
    
        @Override
        public String getSubject() {
            return genJaxbTaskSummary.getSubject();
        }
    
        @Override
        public String getDescription() {
            return genJaxbTaskSummary.getDescription();
        }
    
        @Override
        public Status getStatus() {
            return Status.valueOf(genJaxbTaskSummary.getStatus().value());
        }
    
        @Override
        public Boolean isSkipable() {
            return genJaxbTaskSummary.isSkipable();
        }
    
        @Override
        public User getActualOwner() {
            return convertStringIdToUser(genJaxbTaskSummary.getActualOwner());
        }
    
        @Override
        public User getCreatedBy() {
            return convertStringIdToUser(genJaxbTaskSummary.getCreatedBy());
        }
    
        @Override
        public Integer getProcessSessionId() {
            return genJaxbTaskSummary.getProcessSessionId();
        }
    
        @Override
        public List<String> getPotentialOwners() {
            return genJaxbTaskSummary.getPotentialOwners();
        }
    
        @Override
        public Boolean isQuickTaskSummary() {
            return genJaxbTaskSummary.isQuickTaskSummary();
        }
    
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
            return convertGenStatusToStatus(this.taskData.getStatus());
        }
    
        @Override
        public Status getPreviousStatus() {
            return convertGenStatusToStatus(this.taskData.getPreviousStatus());
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
    }

    static class JaxbAttachmentWrapper extends JaxbWrapper implements Attachment {
    
        private final org.kie.remote.jaxb.gen.Attachment jaxbAttachment;
        
        public JaxbAttachmentWrapper(org.kie.remote.jaxb.gen.Attachment jaxbAttachment) { 
            super(Attachment.class);
            this.jaxbAttachment = jaxbAttachment;
        }
    
        @Override
        public Long getId() {
            return this.jaxbAttachment.getId();
        }
    
        @Override
        public String getName() {
            return this.jaxbAttachment.getName();
        }
    
        @Override
        public String getContentType() {
            return this.jaxbAttachment.getContentType();
        }
    
        @Override
        public Date getAttachedAt() {
            return convertXmlGregCalToDate(this.jaxbAttachment.getAttachedAt());
        }
    
        @Override
        public User getAttachedBy() {
            return convertStringIdToUser(this.jaxbAttachment.getAttachedBy());
        }
    
        @Override
        public int getSize() {
            return this.jaxbAttachment.getSize();
        }
    
        @Override
        public long getAttachmentContentId() {
            return this.jaxbAttachment.getAttachmentContentId();
        }
    }

    static class JaxbCommentWrapper extends JaxbWrapper implements Comment {
    
        private final org.kie.remote.jaxb.gen.Comment jaxbComment;
    
        public JaxbCommentWrapper(org.kie.remote.jaxb.gen.Comment jaxbComment) {
            super(Comment.class);
            this.jaxbComment = jaxbComment;
        }
    
        @Override
        public Long getId() {
            return this.jaxbComment.getId();
        }
    
        @Override
        public String getText() {
            return this.jaxbComment.getText();
        }
    
        @Override
        public Date getAddedAt() {
            return convertXmlGregCalToDate(this.jaxbComment.getAddedAt());
        }
    
        @Override
        public User getAddedBy() {
            return convertStringIdToUser(this.jaxbComment.getAddedBy());
        }
    }

    static class JaxbContentWrapper extends JaxbWrapper implements Content {

        private final org.kie.remote.jaxb.gen.Content genContent;

        public JaxbContentWrapper(org.kie.remote.jaxb.gen.Content content) {
            super(Content.class);
            this.genContent = content;
        }

        @Override
        public long getId() {
            return genContent.getId();
        }

        @Override
        public byte[] getContent() {
            return genContent.getSerializedContent();
        }
    }

    static class JaxbI18NTextWrapper extends JaxbWrapper implements I18NText {
    
        private final org.kie.remote.jaxb.gen.I18NText jaxbText;
    
        public JaxbI18NTextWrapper(org.kie.remote.jaxb.gen.I18NText text) {
            super(I18NText.class);
            this.jaxbText = text;
        }
    
        @Override
        public Long getId() {
            return this.jaxbText.getId();
        }
    
        @Override
        public String getLanguage() {
            return this.jaxbText.getLang();
        }
    
        @Override
        public String getText() {
            return this.jaxbText.getText();
        }
    }

    static class JaxbPeopleAssignmentsWrapper extends JaxbWrapper implements PeopleAssignments {
    
        private final org.kie.remote.jaxb.gen.PeopleAssignments genPeopAssign;
    
        public JaxbPeopleAssignmentsWrapper(org.kie.remote.jaxb.gen.PeopleAssignments peopAssign) {
            super(PeopleAssignments.class);
            this.genPeopAssign = peopAssign;
        }
    
        @Override
        public User getTaskInitiator() {
            return convertStringIdToUser(genPeopAssign.getTaskInitiator());
        }
    
        @Override
        public List<OrganizationalEntity> getPotentialOwners() {
            return convertGenOrgEngListToOrgEntList(genPeopAssign.getPotentialOwners());
        }
    
        @Override
        public List<OrganizationalEntity> getBusinessAdministrators() {
            return convertGenOrgEngListToOrgEntList(genPeopAssign.getBusinessAdministrators());
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
