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

package org.kie.remote.client.documentation;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.kie.api.task.model.Task;

//TODO: changed, add to documentation
public class DocumentationRestCallExample {

    /*
     * Example 13.8
     * A GET call that returns a task details to a locally running application in Java with the direct tasks/TASKID request
    */ 
    
    private static final String USER_AGENT_ID = "org.kie.remote.client.docs";
    private static final int REST_REQUEST_TIMEOUT_IN_SECONDS = 1;
  
    private static final AtomicInteger userAgentIdGen = new AtomicInteger(0);
    
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
       
        String slashIfNeeded = "/";
        if( serverUrl.toExternalForm().endsWith("/") ) {
           slashIfNeeded = ""; 
        }
        URL restServicesBaseUrl = new URL(serverUrl.toExternalForm() + slashIfNeeded + "business-central/rest/");
        // TODO
//        ClientRequestFactory requestFactory 
//            = createAuthenticatingRequestFactory(restServicesBaseUrl, user, password, REST_REQUEST_TIMEOUT_IN_SECONDS);
//        ClientRequest restRequest = requestFactory.createRelativeRequest("task/" + taskId);
//        
//        ClientResponse<?> responseObj = restRequest.get();
//        JaxbTask jaxbTask = responseObj.getEntity(JaxbTask.class);
//        return (Task) jaxbTask;
        return null;
    }

}