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

package org.kie.server.client.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.api.model.instance.RequestInfoInstanceList;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesConfiguration;

import static org.kie.server.api.rest.RestURI.*;

public class JobServicesClientImpl extends AbstractKieServicesClientImpl implements JobServicesClient {

    public JobServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public JobServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public Long scheduleRequest(JobRequestInstance jobRequest) {
        return scheduleRequest("", jobRequest);
    }

    @Override
    public Long scheduleRequest(String containerId, JobRequestInstance jobRequest) {
        Object result = null;
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI, valuesMap) + "?containerId="+containerId, jobRequest,
                    Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "scheduleRequest", serialize(jobRequest), marshaller.getFormat().getType(), new Object[]{containerId}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = deserialize(response.getResult(), Object.class);
        }

        if (result instanceof Wrapped) {
            return (Long) ((Wrapped) result).unwrap();
        }

        return ((Number) result).longValue();
    }

    @Override
    public void cancelRequest(long requestId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(JOB_ID, requestId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI + "/" + CANCEL_JOB_DEL_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "cancelRequest", new Object[]{requestId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void requeueRequest(long requestId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(JOB_ID, requestId);

            makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI + "/" + REQUEUE_JOB_PUT_URI, valuesMap), "", String.class, new HashMap<String, String>());
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "requeueRequest", new Object[]{requestId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<RequestInfoInstance> getRequestsByStatus(List<String> statuses, Integer page, Integer pageSize) {
        RequestInfoInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            String statusQuery = getAdditionalParams("", "status", statuses);
            String queryString = getPagingQueryString(statusQuery, page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI, valuesMap) + queryString, RequestInfoInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "getRequestsByStatus", new Object[]{safeList(statuses), page, pageSize}) ) );
            ServiceResponse<RequestInfoInstanceList> response = (ServiceResponse<RequestInfoInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            list = response.getResult();
        }

        if (list != null && list.getRequestInfoInstances() != null) {
            return Arrays.asList(list.getRequestInfoInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<RequestInfoInstance> getRequestsByBusinessKey(String businessKey, Integer page, Integer pageSize) {
        RequestInfoInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(JOB_KEY, businessKey);

            String queryString = getPagingQueryString("", page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI + "/" + JOB_INSTANCES_BY_KEY_GET_URI, valuesMap) + queryString, RequestInfoInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "getRequestsByBusinessKey", new Object[]{businessKey, page, pageSize}) ) );
            ServiceResponse<RequestInfoInstanceList> response = (ServiceResponse<RequestInfoInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            list = response.getResult();
        }

        if (list != null && list.getRequestInfoInstances() != null) {
            return Arrays.asList(list.getRequestInfoInstances());
        }

        return Collections.emptyList();

    }

    @Override
    public List<RequestInfoInstance> getRequestsByCommand(String command, Integer page, Integer pageSize) {
        RequestInfoInstanceList list = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(JOB_CMD_NAME, command);

            String queryString = getPagingQueryString("", page, pageSize);

            list = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI + "/" + JOB_INSTANCES_BY_CMD_GET_URI, valuesMap) + queryString, RequestInfoInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "getRequestsByCommand", new Object[]{command, page, pageSize}) ) );
            ServiceResponse<RequestInfoInstanceList> response = (ServiceResponse<RequestInfoInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            list = response.getResult();
        }

        if (list != null && list.getRequestInfoInstances() != null) {
            return Arrays.asList(list.getRequestInfoInstances());
        }

        return Collections.emptyList();

    }

    @Override
    public RequestInfoInstance getRequestById(Long requestId, boolean withErrors, boolean withData) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(JOB_ID, requestId);

            return makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, JOB_URI + "/" + JOB_INSTANCE_GET_URI, valuesMap) + "?withErrors=" + withErrors + "&withData=" + withData , RequestInfoInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "JobService", "getRequestById", marshaller.getFormat().getType(), new Object[]{requestId, withErrors, withData} )) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            return deserialize(response.getResult(), RequestInfoInstance.class);
        }
    }
}
