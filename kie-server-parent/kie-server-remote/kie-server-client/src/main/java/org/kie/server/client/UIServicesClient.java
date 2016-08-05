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

package org.kie.server.client;

import org.kie.server.client.jms.ResponseHandler;

public interface UIServicesClient {

    /**
     * Returns process form for given process id that resides in given container
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @param language language that form should be filtered for
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessForm(String containerId, String processId, String language);

    /**
     * Returns process form for given process id that resides in given container - without filtering values by language
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return string representation (json or xml depending on client marshaling selection) of the process form
     */
    String getProcessForm(String containerId, String processId);

    /**
     * Returns task form for given task id that belongs to given container
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @param language language that form should be filtered for
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskForm(String containerId, Long taskId, String language);

    /**
     * Returns task form for given task id that belongs to given container as raw content - without filtering values by language
     * @param containerId container identifier where task resides
     * @param taskId unique task id
     * @return  string representation (json or xml depending on client marshaling selection) of the task form
     */
    String getTaskForm(String containerId, Long taskId);

    /**
     * Returns process image (svg) of the given process id that belongs to given container
     * @param containerId container identifier where process resides
     * @param processId  unique process id
     * @return svg (xml) representing process image
     */
    String getProcessImage(String containerId, String processId);

    /**
     * Returns process image (svg) with annotated active and completed nodes for given process instance
     * that belongs to given container
     * @param containerId container identifier where process resides
     * @param processInstanceId unique process instance id
     * @return svg (xml) representing process image annotated with active (in red) and completed (in grey) nodes
     */
    String getProcessInstanceImage(String containerId, Long processInstanceId);

    /**
     * Override default response handler to change interaction pattern. Applies only to JMS
     * based integration.
     * @param responseHandler
     */
    void setResponseHandler(ResponseHandler responseHandler);
}
