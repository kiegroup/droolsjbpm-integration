/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.remote.rest.taskassigning.runtime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.model.taskassigning.ExecutePlanningResult;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_EXECUTE_PLANNING_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_QUERIES_TASK_DATA_URI;
import static org.kie.server.api.model.taskassigning.TaskAssigningRestURI.TASK_ASSIGNING_RUNTIME_URI;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Path("server/" + TASK_ASSIGNING_RUNTIME_URI)
public class TaskAssigningRuntimeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningRuntimeResource.class);

    private TaskAssigningRuntimeServiceBase runtimeServiceBase;
    private KieServerRegistry context;
    private MarshallerHelper marshallerHelper;

    public TaskAssigningRuntimeResource(TaskAssigningRuntimeServiceBase runtimeServiceBase,
                                        KieServerRegistry context) {
        this.runtimeServiceBase = runtimeServiceBase;
        this.context = context;
        this.marshallerHelper = new MarshallerHelper(context);
    }

    @POST
    @Path(TASK_ASSIGNING_EXECUTE_PLANNING_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executePlanning(@javax.ws.rs.core.Context HttpHeaders headers,
                                    @QueryParam("user") String userId,
                                    String payload) {
        final Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        final Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            final String contentType = getContentType(headers);
            final PlanningItemList planningItemList = marshallerHelper.unmarshal(payload,
                                                                                 contentType,
                                                                                 PlanningItemList.class);
            final ExecutePlanningResult result = runtimeServiceBase.executePlanning(planningItemList, userId);
            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            LOGGER.error("Unexpected error executing planning {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v);
        }
    }

    @POST
    @Path(TASK_ASSIGNING_QUERIES_TASK_DATA_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeTasksQuery(@javax.ws.rs.core.Context HttpHeaders headers, String payload) {
        final Variant v = getVariant(headers);
        // no container id available so only used to transfer conversation id if given by client
        final Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        try {
            String contentType = getContentType(headers);
            @SuppressWarnings("unchecked")
            final Map<String, Object> queryParameters = marshallerHelper.unmarshal(payload,
                                                                                   contentType,
                                                                                   Map.class);
            final LocalDateTime queryTime = LocalDateTime.now();
            final List<TaskData> taskDataList = runtimeServiceBase.executeFindTasksQuery(queryParameters);
            final TaskDataList result = new TaskDataList(taskDataList);
            result.setQueryTime(queryTime);
            return createCorrectVariant(result, headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            LOGGER.error("Unexpected error finding tasks {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
}
