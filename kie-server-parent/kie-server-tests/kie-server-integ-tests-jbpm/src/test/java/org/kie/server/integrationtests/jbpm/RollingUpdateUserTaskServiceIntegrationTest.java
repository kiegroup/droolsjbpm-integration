/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskAttachment;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class RollingUpdateUserTaskServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    protected static final String CONTAINER_ALIAS = "project";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project-101");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @Test
    public void testGetTaskInputAndOutputWithAlias() throws Exception {
        try {
            changeUser(USER_ADMINISTRATOR);

            Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
            assertThat(pid).isNotNull().isGreaterThan(0);

            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
            assertThat(processInstance).isNotNull();

            List<TaskSummary> taskList = taskClient.findTasksAssignedAsBusinessAdministrator(USER_YODA, 0, 10);
            assertThat(taskList).hasSize(1);

            TaskSummary taskSummary = taskList.get(0);

            Map<String, Object> inputContent = taskClient.getTaskInputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(inputContent).isNotNull().hasSize(4);
            assertThat(inputContent.get("new content")).isNull();

            Map<String, Object> data = new HashMap<>();
            data.put("new content", "test");

            userTaskAdminClient.addTaskInputs(CONTAINER_ALIAS, taskSummary.getId(), data);

            // check this method too
            TaskInstance taskInstance = taskClient.getTaskInstance(CONTAINER_ALIAS, taskSummary.getId(), false, false, false);
            assertThat(taskInstance).isNotNull();
            assertThat(taskInstance.getId()).isEqualTo(taskSummary.getId());

            inputContent = taskClient.getTaskInputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(inputContent).isNotNull().hasSize(5);
            assertThat(inputContent.get("new content")).isEqualTo("test");

            userTaskAdminClient.removeTaskInputs(CONTAINER_ALIAS, taskSummary.getId(), "new content");

            inputContent = taskClient.getTaskInputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(inputContent).isNotNull().hasSize(4);
            assertThat(inputContent.get("new content")).isNull();

            Map<String, Object> outputContent = taskClient.getTaskOutputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(outputContent).isNotNull().hasSize(0);

            taskClient.saveTaskContent(CONTAINER_ALIAS, taskSummary.getId(), data);

            outputContent = taskClient.getTaskOutputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(outputContent).isNotNull().hasSize(1);
            assertThat(outputContent.get("new content")).isEqualTo("test");

            data.put("Outcome", "Approved");

            taskClient.completeAutoProgress(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA, data);

            outputContent = taskClient.getTaskOutputContentByTaskId(CONTAINER_ALIAS, taskSummary.getId());
            assertThat(outputContent).isNotNull().hasSize(2);
            assertThat(outputContent.get("new content")).isEqualTo("test");
            assertThat(outputContent.get("Outcome")).isEqualTo("Approved");
        } finally {
            // In case the test fails, we need to switch back to YODA user
            changeUser(USER_YODA);
        }

    }

    @Test
    public void testTaskAttachmentsWithAlias() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
        assertThat(pid).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(processInstance).isNotNull();

        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(taskList).hasSize(1);

        TaskSummary taskSummary = taskList.get(0);

        List<TaskAttachment> attachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(attachments).isNotNull().hasSize(0);

        taskClient.addTaskAttachment(CONTAINER_ALIAS, taskSummary.getId(), USER_YODA, "My attachment", "Attachment Content");

        attachments = taskClient.getTaskAttachmentsByTaskId(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(attachments).isNotNull().hasSize(1);
        assertThat(attachments.get(0).getContentType()).isEqualTo("java.lang.String");

        TaskAttachment taskAttachment = taskClient.getTaskAttachmentById(CONTAINER_ALIAS, taskSummary.getId(), attachments.get(0).getId());
        assertThat(taskAttachment.getId()).isEqualTo(attachments.get(0).getId());

        Object content = taskClient.getTaskAttachmentContentById(CONTAINER_ALIAS, taskSummary.getId(), attachments.get(0).getId());
        assertThat(content).isEqualTo("Attachment Content");

    }

    @Test
    public void testTaskCommentsWithAlias() throws Exception {
        Long pid = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION);
        assertThat(pid).isNotNull().isGreaterThan(0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, pid);
        assertThat(processInstance).isNotNull();

        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertThat(taskList).hasSize(1);

        TaskSummary taskSummary = taskList.get(0);

        List<TaskComment> taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(taskComments).isNotNull().hasSize(0);

        Date commentDate = new SimpleDateFormat("dd-MM-yyyy hh:mm").parse("18-03-1992 08:06");

        taskClient.addTaskComment(CONTAINER_ALIAS, taskSummary.getId(), "May the force be with you!", USER_YODA, commentDate);

        taskComments = taskClient.getTaskCommentsByTaskId(CONTAINER_ALIAS, taskSummary.getId());
        assertThat(taskComments).isNotNull().hasSize(1);

        TaskComment taskComment = taskComments.get(0);
        assertThat(taskComment.getText()).isEqualTo("May the force be with you!");
        assertThat(taskComment.getAddedBy()).isEqualTo(USER_YODA);
        assertThat(taskComment.getAddedAt()).isEqualTo(commentDate);

        taskComment = taskClient.getTaskCommentById(CONTAINER_ALIAS, taskSummary.getId(), taskComments.get(0).getId());
        assertThat(taskComment.getId()).isEqualTo(taskComments.get(0).getId());
        assertThat(taskComment.getText()).isEqualTo("May the force be with you!");
        assertThat(taskComment.getAddedBy()).isEqualTo(USER_YODA);
        assertThat(taskComment.getAddedAt()).isEqualTo(commentDate);

    }
}
