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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.taskassigning.PlanningExecutionResult;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskDataList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.kie.server.api.KieServerConstants.KIE_CONTENT_TYPE_HEADER;
import static org.kie.server.api.marshalling.MarshallingFormat.XSTREAM;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAssigningRuntimeResourceTest {

    private static final String USER_ID = "USER_ID";

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private TaskAssigningRuntimeServiceBase runtimeServiceBase;

    @Spy
    private KieServerRegistry kieServerRegistry = new KieServerRegistryImpl();

    private TaskAssigningRuntimeResource resource;

    @Before
    public void setUp() {
        when(httpHeaders.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());
        when(httpHeaders.getRequestHeader(KIE_CONTENT_TYPE_HEADER)).thenReturn(Collections.singletonList(XSTREAM.getType()));
        resource = new TaskAssigningRuntimeResource(runtimeServiceBase, kieServerRegistry);
    }

    @Test
    public void executePlanning() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(XSTREAM, this.getClass().getClassLoader());
        PlanningItemList planningItemList = new PlanningItemList();
        String payload = marshaller.marshall(planningItemList);

        PlanningExecutionResult expectedResult = PlanningExecutionResult.builder().build();
        when(runtimeServiceBase.executePlanning(eq(planningItemList), eq(USER_ID))).thenReturn(expectedResult);

        String rawResult = resource.executePlanning(httpHeaders, USER_ID, payload).getEntity().toString();
        String expectedResultInRawFormat = marshaller.marshall(expectedResult);
        assertEquals(expectedResultInRawFormat, rawResult);
    }

    @Test
    public void executeFindTasksQuery() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(XSTREAM, this.getClass().getClassLoader());
        Map<String, Object> params = new HashMap<>();
        params.put("param", "value");
        String payload = marshaller.marshall(params);

        List<TaskData> expectedResult = new ArrayList<>();
        expectedResult.add(TaskData.builder()
                                   .taskId(1L)
                                   .name("SomeTask")
                                   .build());
        when(runtimeServiceBase.executeFindTasksQuery(eq(params))).thenReturn(expectedResult);

        String rawResult = resource.executeTasksQuery(httpHeaders, payload).getEntity().toString();
        TaskDataList unMarshalledResult = marshaller.unmarshall(rawResult, TaskDataList.class);
        assertEquals(expectedResult, unMarshalledResult.getItems());
    }
}