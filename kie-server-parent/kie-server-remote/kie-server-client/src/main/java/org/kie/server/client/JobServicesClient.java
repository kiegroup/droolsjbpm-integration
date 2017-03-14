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

import java.util.List;
import java.util.Map;

import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.client.jms.ResponseHandler;

public interface JobServicesClient {

    Long scheduleRequest(JobRequestInstance jobRequest);

    Long scheduleRequest(String containerId, JobRequestInstance jobRequest);

    void cancelRequest(long requestId);

    void updateRequestData(long requestId, String containerId, Map<String, Object> data);

    void requeueRequest(long requestId);

    List<RequestInfoInstance> getRequestsByStatus(List<String> statuses, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByBusinessKey(String businessKey, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByBusinessKey(String businessKey, List<String> statuses, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByCommand(String command, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByCommand(String command, List<String> statuses, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByContainer(String containerId, List<String> statuses, Integer page, Integer pageSize);

    List<RequestInfoInstance> getRequestsByProcessInstance(Long processInstanceId, List<String> statuses, Integer page, Integer pageSize);

    RequestInfoInstance getRequestById(Long requestId, boolean withErrors, boolean withData);

    void setResponseHandler(ResponseHandler responseHandler);
}
