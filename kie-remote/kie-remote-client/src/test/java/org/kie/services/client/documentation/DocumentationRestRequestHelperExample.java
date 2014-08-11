package org.kie.services.client.documentation;

import java.net.URL;

import org.kie.api.task.model.Task;
import org.kie.services.client.api.RestRequestHelper;

public class DocumentationRestRequestHelperExample {
    
    /**
     * Retrieves a task instance from the remote REST API
     * 
     * @param serverUrl The URL of the machine on which BPMS is running
     * @param taskId The task id of the task that should be retrieved via the remote REST API
     * @return A Task instance, with information about the task specified in the taskId parameter
     * @throws Exception if something goes wrong
     */
    public Task getTaskInstanceInfo(URL serverUrl, long taskId, String user, String password) throws Exception {
        // serverUrl should look something like this: "http://192.178.168.1:8080/"
        RestRequestHelper requestHelper = RestRequestHelper.newInstance(serverUrl, user, password);
        
        // TODO
        
//        ClientRequest restRequest = requestHelper.createRequest("task/" + taskId);
//        
//        ClientResponse<?> responseObj = restRequest.get();
//        JaxbTask jaxbTask = responseObj.getEntity(JaxbTask.class);
//        return (Task) jaxbTask;
        return null;
    }

}