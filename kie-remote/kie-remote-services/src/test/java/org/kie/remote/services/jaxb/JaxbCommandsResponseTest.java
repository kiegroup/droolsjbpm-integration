/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.jaxb;

import org.jbpm.services.task.commands.GetTaskByWorkItemIdCommand;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.junit.Assert;
import org.junit.Test;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

import java.util.List;

public class JaxbCommandsResponseTest {

    @Test
    //https://issues.jboss.org/browse/JBPM-5282
    public void testAddGetTaskByWorkItemIdCommand() {
        JaxbCommandsResponse jaxbCommandsResponse = new JaxbCommandsResponse();
        String taskName = "task name";
        GetTaskByWorkItemIdCommand command = new GetTaskByWorkItemIdCommand(10L);
        TaskImpl task = new TaskImpl();
        task.setName(taskName);
        task.setPeopleAssignments(new PeopleAssignmentsImpl());
        task.setTaskData(new TaskDataImpl());
        jaxbCommandsResponse.addResult(task, 0, command);

        List<JaxbCommandResponse<?>> responses = jaxbCommandsResponse.getResponses();
        Assert.assertEquals(1, responses.size());
        @SuppressWarnings("unchecked")
        JaxbCommandResponse<JaxbTaskResponse> response = (JaxbCommandResponse<JaxbTaskResponse>)responses.get(0);
        JaxbTaskResponse jaxbTaskResponse = response.getResult();
        Assert.assertEquals(taskName, jaxbTaskResponse.getName());
        Assert.assertNotNull(jaxbTaskResponse.getPeopleAssignments());
        Assert.assertNotNull(jaxbTaskResponse.getTaskData());
    }
}
