package org.kie.server.remote.rest.jbpm.resources;

public class Messages {

    public static final String PROCESS_DEFINITION_NOT_FOUND = "Could not find process definition \"{0}\" in container \"{1}\"";
    public static final String PROCESS_DEFINITION_FETCH_ERROR = "Error when retrieving process definition \"{0}\" in container \"{1}\":: Cause: {2}";

    public static final String PROCESS_INSTANCE_NOT_FOUND = "Could not find process instance with id \"{0}\"";
    public static final String TASK_INSTANCE_NOT_FOUND = "Could not find task instance with id \"{0}\"";
    public static final String TASK_INSTANCE_NOT_FOUND_FOR_WORKITEM = "Could not find task instance for work item with id \"{0}\"";
    public static final String TASK_ATTACHMENT_NOT_FOUND = "Could not find task attachment with id \"{0}\" attached to task with id \"{1}\"";
    public static final String TASK_COMMENT_NOT_FOUND = "Could not find task comment with id \"{0}\" attached to task with id \"{1}\"";

    public static final String WORK_ITEM_NOT_FOUND = "Could not find work item instance with id \"{0}\"";

    public static final String NODE_INSTANCE_NOT_FOUND = "Could not find node instance with id \"{0}\" within process instance with id \"{1}\"";

    public static final String VAR_INSTANCE_NOT_FOUND = "Could not find variable instance with name \"{0}\" within process instance with id \"{1}\"";

    public static final String VARIABLE_INSTANCE_NOT_FOUND = "Could not find variable \"{0}\" in process instance with id \"{1}\"";

    public static final String CONTAINER_NOT_FOUND = "Could not find container \"{0}\"";

    public static final String CREATE_RESPONSE_ERROR = "Unable to create response: {0}";

    public static final String UNEXPECTED_ERROR = "Unexpected error during processing: {0}";
}
