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

package org.kie.server.services.jbpm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.jbpm.executor.RequeueAware;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ErrorInfo;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.ExecutorService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.model.instance.ErrorInfoInstance;
import org.kie.server.api.model.instance.ErrorInfoInstanceList;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(ExecutorServiceBase.class);

    private ExecutorService executorService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    public ExecutorServiceBase(ExecutorService executorService, KieServerRegistry context) {
        this.executorService = executorService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
    }

    // operations
    public String scheduleRequest(String containerId, String payload, String marshallingType) {


        JobRequestInstance jobRequest = null;
        if (containerId != null && !containerId.isEmpty()) {
            logger.debug("About to unmarshal job request from payload: '{}' using container {} marshaller", payload, containerId);
            jobRequest = marshallerHelper.unmarshal(containerId, payload, marshallingType, JobRequestInstance.class);
            // set deployment id which is given by container id
            jobRequest.getData().put("deploymentId", containerId);
        } else {
            logger.debug("About to unmarshal job request from payload: '{}' using server marshaller", payload);
            jobRequest = marshallerHelper.unmarshal(payload, marshallingType, JobRequestInstance.class);
        }
        Long requestId = null;

        if(!validateCommand(jobRequest.getCommand())){
            throw new IllegalArgumentException("Invalid command type "+jobRequest.getCommand());
        }

        if (jobRequest.getScheduledDate() != null) {
            logger.debug("Scheduling job at future date '{}' for request {}", jobRequest.getScheduledDate(), jobRequest);
            requestId = executorService.scheduleRequest(jobRequest.getCommand(), jobRequest.getScheduledDate(), new CommandContext(jobRequest.getData()));
        } else {
            logger.debug("Scheduling job at current date '{}' for request {}", new Date(), jobRequest);
            requestId = executorService.scheduleRequest(jobRequest.getCommand(), new CommandContext(jobRequest.getData()));
        }
        // return response
        String response = marshallerHelper.marshal(marshallingType, requestId);
        return response;
    }

    public void cancelRequest(long requestId) {
        logger.debug("About to cancel job with id {}", requestId);
        executorService.cancelRequest(requestId);
    }


    public void requeueRequest(long requestId) {
        logger.debug("About to requeue job with id {}", requestId);
        ((RequeueAware) executorService).requeueById(requestId);
    }

    public void updateRequestData(long requestId, String containerId, String payload, String marshallingType) {

        Map<String, Object> data = null;
        if (containerId != null && !containerId.isEmpty()) {
            logger.debug("About to unmarshal job data from payload: '{}' using container {} marshaller", payload, containerId);
            data = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        } else {
            logger.debug("About to unmarshal job data from payload: '{}' using server marshaller", payload);
            data = marshallerHelper.unmarshal(payload, marshallingType, Map.class);
        }

        logger.debug("About to update job's ( with id {}) data {}", requestId, data);
        executorService.updateRequestData(requestId, data);
    }

    // queries

    public RequestInfoInstanceList getRequestsByStatus(List<String> statuses, Integer page, Integer pageSize) {

        List<STATUS> statusList = convertStatusList(statuses);
        List<RequestInfo> requests = executorService.getRequestsByStatus(statusList, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByBusinessKey(String businessKey, Integer page, Integer pageSize) {

        List<RequestInfo> requests = executorService.getRequestsByBusinessKey(businessKey, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByBusinessKey(String businessKey, List<String> statuses, Integer page, Integer pageSize) {
        List<STATUS> statusList = convertStatusList(statuses);
        List<RequestInfo> requests = executorService.getRequestsByBusinessKey(businessKey, statusList, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByCommand(String command, Integer page, Integer pageSize) {

        List<RequestInfo> requests = executorService.getRequestsByCommand(command, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByCommand(String command, List<String> statuses, Integer page, Integer pageSize) {
        List<STATUS> statusList = convertStatusList(statuses);
        List<RequestInfo> requests = executorService.getRequestsByCommand(command, statusList, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByContainer(String containerId, List<String> statuses, Integer page, Integer pageSize) {

        List<STATUS> statusList = convertStatusList(statuses);
        List<RequestInfo> requests = executorService.getRequestsByDeployment(containerId, statusList, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    public RequestInfoInstanceList getRequestsByProcessInstance(Number processInstanceId, List<String> statuses, Integer page, Integer pageSize) {

        List<STATUS> statusList = convertStatusList(statuses);
        List<RequestInfo> requests = executorService.getRequestsByProcessInstance(processInstanceId.longValue(), statusList, buildQueryContext(page, pageSize));

        RequestInfoInstanceList result = convertToRequestInfoList(requests, false, false);

        return result;
    }

    // instance details
    public String getRequestById(long requestId, boolean withErrors, boolean withData, String marshallingType) {

        String result = null;

        RequestInfo request = executorService.getRequestById(requestId);

        if (request == null) {
            throw new IllegalArgumentException("Request with id: " + requestId + " doesn't exist");
        } else {
            RequestInfoInstance requestInstance = convertToRequestInfo(request, withErrors, withData);

            String deploymentId = ((org.jbpm.executor.entities.RequestInfo) request).getDeploymentId();
            if (deploymentId != null && context.getContainer(deploymentId) != null) {
                result = marshallerHelper.marshal(deploymentId, marshallingType, requestInstance);
            } else {
                result = marshallerHelper.marshal(marshallingType, requestInstance);
            }
        }

        return result;
    }

    // utility methods for conversion
    protected RequestInfoInstanceList convertToRequestInfoList(List<RequestInfo> requests, boolean withErrors, boolean withData) {

        RequestInfoInstance[] requestInfos = new RequestInfoInstance[requests.size()];

        int index = 0;
        for (RequestInfo request : requests) {
            requestInfos[index] = convertToRequestInfo(request, withErrors, withData);
            index++;
        }

        return new RequestInfoInstanceList(requestInfos);
    }

    protected RequestInfoInstance convertToRequestInfo(RequestInfo request, boolean withErrors, boolean withData) {

        RequestInfoInstance.Builder builder = RequestInfoInstance.builder()
                .id(request.getId())
                .businessKey(request.getKey())
                .command(request.getCommandName())
                .executions(request.getExecutions())
                .message(request.getMessage())
                .retries(request.getRetries())
                .scheduledDate(request.getTime())
                .status(request.getStatus().toString())
                .containerId(request.getDeploymentId());

        if (withErrors) {
            ErrorInfoInstance[] errors = new ErrorInfoInstance[request.getErrorInfo().size()];
            int index = 0;
            for (ErrorInfo error : request.getErrorInfo()) {

                errors[index] = ErrorInfoInstance.builder()
                        .id(error.getId())
                        .errorDate(error.getTime())
                        .message(error.getMessage())
                        .requestId(request.getId())
                        .stacktrace(error.getStacktrace())
                        .build();

                index++;
            }

            builder.errors(new ErrorInfoInstanceList(errors));
        }

        if (withData) {
            ClassLoader classLoader = this.getClass().getClassLoader(); // by default use kieserver classloader
            String deploymentId = ((org.jbpm.executor.entities.RequestInfo) request).getDeploymentId();

            if (deploymentId != null && context.getContainer(deploymentId) != null) {
                KieContainerInstanceImpl containerInstance = context.getContainer(deploymentId);
                classLoader = containerInstance.getKieContainer().getClassLoader();
            }

            builder.data(readContent(request.getRequestData(), classLoader));
            builder.responseData(readContent(request.getResponseData(), classLoader));
        }

        return builder.build();
    }

    protected Map<String, Object> readContent(byte[] data, ClassLoader classLoader) {
        Object result = null;
        if (data != null) {
            ObjectInputStream in = null;
            try {
                in = new ClassLoaderObjectInputStream(classLoader, new ByteArrayInputStream(data));
                result = in.readObject();
            } catch (Exception e) {
                logger.warn("Exception while serializing context data", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {

                    }
                }
            }
            if (result instanceof CommandContext) {
                return ((CommandContext)result).getData();
            } else if (result instanceof ExecutionResults) {
                return ((ExecutionResults) result).getData();
            }
        }

        return new HashMap<String, Object>();
    }

    protected QueryContext buildQueryContext(Integer page, Integer pageSize) {
        return new QueryContext(page * pageSize, pageSize);
    }

    protected boolean validateCommand(String command) {
        try {
            Class.forName(command);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected List<STATUS> convertStatusList(List<String> statuses) {
        List<STATUS> statusList = new ArrayList<STATUS>();
        if (statuses != null && !statuses.isEmpty()) {
            for (String status : statuses) {
                statusList.add(STATUS.valueOf(status));
            }
        } else {
            statusList.add(STATUS.QUEUED);
        }

        return statusList;
    }
}
