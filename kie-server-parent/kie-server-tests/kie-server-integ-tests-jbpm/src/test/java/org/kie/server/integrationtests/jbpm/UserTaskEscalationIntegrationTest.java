/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.category.Email;
import org.kie.server.integrationtests.category.Unstable;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Category(Email.class)
public class UserTaskEscalationIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final String ESCALATION_TEXT = "Escalation text";
    private static final String FROM_EMAIL = "kie-server-test@domain.com";
    private static final String EMAIL_DOMAIN = "@domain.com";
    private static final int SMTP_PORT = 2525;
    private static final String SMTP_HOST = "localhost";

    private static final KieServerConfigItem PPI_RUNTIME_STRATEGY = new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, RuntimeStrategy.PER_PROCESS_INSTANCE.name(), String.class.getName());

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", CONTAINER_ID,
            "1.0.0.Final");
    private static ReleaseId releaseNotificationId = new ReleaseId("org.kie.server.testing", CONTAINER_ID_NOTIFICATION,
                                                       "1.0.0.Final");

    private final static Map<String, Object> params = new HashMap<String, Object>();

    static {
        params.put("actor", USER_YODA);
        params.put("reassignTo", USER_JOHN);
        params.put("escUser", USER_JOHN);
        params.put("escalation", ESCALATION_TEXT);
    }

    private Wiser wiser;

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration configuration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][] {
                                {MarshallingFormat.JAXB, configuration}
                        }
        ));

        return parameterData;
    }

    @Before
    public void startWiser() {
        wiser = new Wiser();
        wiser.setHostname(SMTP_HOST);
        wiser.setPort(SMTP_PORT);
        wiser.start();
    }

    @After
    public void stopWiser() {
        if (wiser != null) {
            wiser.stop();
        }
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/" + CONTAINER_ID);
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/" + CONTAINER_ID_NOTIFICATION);

        createContainer(CONTAINER_ID, releaseId, PPI_RUNTIME_STRATEGY);
        createContainer(CONTAINER_ID_NOTIFICATION, releaseNotificationId, PPI_RUNTIME_STRATEGY);
    }

    @Test
    @Category(Unstable.class)
    public void testEscalation() throws InterruptedException, MessagingException, Exception {
        // Unstable on slow DBs where completing of the task overlaps with escalation causing race conditions in Hibernate.
        // Could be stabilized using specific transaction isolation.
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_ESCALATION, params);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        try {
            List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            TaskSummary taskSummary = taskList.get(0);
            assertEquals("User Task", taskSummary.getName());
            Long taskId = taskSummary.getId();

            TaskInstance taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(USER_YODA, taskInstance.getActualOwner());

            changeUser(USER_JOHN);
            waitForAssign(taskId, USER_JOHN);

            taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_JOHN, 0, 10);
            assertNotNull(taskList);
            assertEquals(1, taskList.size());
            taskId = taskList.get(0).getId();
            taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);

            taskClient.startTask(CONTAINER_ID, taskId, USER_JOHN);

            taskInstance = taskClient.findTaskById(taskId);
            assertNotNull(taskInstance);
            assertEquals(USER_JOHN, taskInstance.getActualOwner());

            waitForEmailsReceive(wiser);
            taskClient.completeTask(CONTAINER_ID, taskId, USER_JOHN, new HashMap<String, Object>());

            ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, processInstance.getState().intValue());

            assertTotalOfEmails(1);
            assertEmails("Escalation", ESCALATION_TEXT, USER_JOHN, 1);
        } catch (Exception e) {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            throw e;
        } finally {
            changeUser(TestConfig.getUsername());
        }
    }

    @Test
    @Category(Unstable.class)
    public void testCompleteTaskBeforeEscalation() throws InterruptedException {
        // Unstable on slow DBs where starting of task is called after escalation timeout.
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK_ESCALATION, params);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        List<TaskSummary> taskList = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertNotNull(taskList);
        assertEquals(1, taskList.size());
        TaskSummary taskSummary = taskList.get(0);
        assertEquals("User Task", taskSummary.getName());
        Long taskId = taskSummary.getId();

        taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);
        taskClient.completeTask(CONTAINER_ID, taskId, USER_YODA, new HashMap<String, Object>());

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
        assertNotNull(processInstance);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, processInstance.getState().intValue());

        KieServerAssert.assertNullOrEmpty("Email received!", wiser.getMessages());

        //wait while, cause email is sent 6s after task start
        Thread.sleep(8000L);
        KieServerAssert.assertNullOrEmpty("Email received!", wiser.getMessages());
    }

    @Test(timeout = 10000)
    @Category(Unstable.class) // Potentially unstable on slow DBs.
    public void testRepeatedEscalationsWhenNotStarted() throws Exception {
        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess(CONTAINER_ID_NOTIFICATION, "repeatedEmailNotificationProcess");
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId > 0);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
            Assertions.assertThat(tasks).hasSize(1);

            Thread.sleep(4000L);
            assertTotalOfEmails(3); // 1 NotStarted repeated notification + 2 single NotStarted notifications
            assertEmails("NotStarted at 1secs", "NotStarted at 1secs", USER_YODA, 1);
            assertEmails("NotStarted at 2secs", "NotStarted at 2secs", USER_YODA, 1);
            assertEmails("NotStarted repeated notification every 3secs", "NotStarted repeated notification every 3secs", USER_YODA, 1);

            Thread.sleep(3000L);
            assertTotalOfEmails(5); // 1 NotCompleted + 2 NotStarted repeated notifications + 2 single NotStarted notifications
            assertEmails("NotStarted repeated notification every 3secs", "NotStarted repeated notification every 3secs", USER_YODA, 2);

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID_NOTIFICATION, processInstanceId);
            }
        }
    }

    @Test(timeout = 15000)
    @Category(Unstable.class) // Potentially unstable on slow DBs.
    public void testRepeatedEscalationsWhenNotCompleted() throws Exception {
        Long processInstanceId;
        processInstanceId = processClient.startProcess(CONTAINER_ID_NOTIFICATION, "repeatedEmailNotificationProcess");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks).hasSize(1);

        taskClient.startTask(CONTAINER_ID_NOTIFICATION, tasks.get(0).getId(), USER_YODA);

        Thread.sleep(6000L);
        assertTotalOfEmails(1); // 1 NotCompleted repeated notification
        assertEmails("NotCompleted repeated notification every 5secs", "NotCompleted repeated notification every 5secs", USER_YODA, 1);
        taskClient.completeTask(CONTAINER_ID_NOTIFICATION, tasks.get(0).getId(), USER_YODA, new HashMap<>());

        Thread.sleep(6000L);
        assertTotalOfEmails(1); // No new notifications should be received
        assertEmails("NotCompleted repeated notification every 5secs", "NotCompleted repeated notification every 5secs", USER_YODA, 1);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID_NOTIFICATION, processInstanceId);
        assertNotNull(processInstance);
        assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, processInstance.getState().intValue());
    }

    private void assertEmails(String subj, String content, String userTo, long totalMessages) throws IOException, MessagingException {
        assertEquals(totalMessages, getEmails().stream()
                .filter(message -> subj.equals(message.subject))
                .filter(message -> content.equals(message.content))
                .filter(message -> FROM_EMAIL.equals(message.from))
                .filter(message -> (userTo + EMAIL_DOMAIN).equals(message.to))
                .count());
    }

    private List<Message> getEmails() throws MessagingException, IOException {
        List<WiserMessage> wiserMessages = wiser.getMessages();
        assertNotNull(wiserMessages);
        List<Message> messages = new ArrayList<>(wiserMessages.size());

        for (WiserMessage wiserMessage : wiserMessages) {

            MimeMessage receivedMessage = wiserMessage.getMimeMessage();
            assertNotNull(receivedMessage);
            Message message = new Message();
            message.subject = receivedMessage.getSubject();
            message.content = receivedMessage.getContent();
            message.sentDate = receivedMessage.getSentDate();

            InternetAddress[] from = (InternetAddress[]) receivedMessage.getFrom();
            assertEquals(1, from.length);
            message.from = from[0].getAddress();
            InternetAddress[] to = (InternetAddress[]) receivedMessage.getAllRecipients();
            assertEquals(2, to.length);
            InternetAddress[] toAddrs = (InternetAddress[]) receivedMessage.getAllRecipients();
            if (!toAddrs[0].getAddress().equals(USER_ADMINISTRATOR + EMAIL_DOMAIN)) {
                assertEquals(USER_ADMINISTRATOR + EMAIL_DOMAIN, toAddrs[1].getAddress());
                message.to = toAddrs[0].getAddress();
            } else {
                assertEquals(USER_ADMINISTRATOR + EMAIL_DOMAIN, toAddrs[0].getAddress());
                message.to = toAddrs[1].getAddress();
            }
            if (!messages.contains(message)) { //wiser should catch 2 messages (one for user and one for Administrator)
                messages.add(message);
            }
        }
        return messages;
    }

    private void assertTotalOfEmails(int totalMessages) throws IOException, MessagingException {
        List<WiserMessage> messages = wiser.getMessages();
        assertNotNull(messages);
        assertEquals(totalMessages, getEmails().size());
    }

    private static final long SERVICE_TIMEOUT = 30000;
    private static final long TIMEOUT_BETWEEN_CALLS = 200;

    protected void waitForEmailsReceive(Wiser wiser) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            if (wiser.getMessages().isEmpty()) {
                Thread.sleep(TIMEOUT_BETWEEN_CALLS);
            } else {
                return;
            }
        }
        throw new TimeoutException("Timeout while waiting for process instance to finish.");
    }

    protected void waitForAssign(Long taskId, String potencialOwner) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while (Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            if (taskClient.findTasksAssignedAsPotentialOwner(potencialOwner, 0, 10).isEmpty()) {
                Thread.sleep(TIMEOUT_BETWEEN_CALLS);
            } else {
                return;
            }
        }
        throw new TimeoutException("Timeout while waiting for process instance to finish.");
    }

    private static class Message {
        public String subject;
        public Object content;
        public String from;
        public String to;
        public Date sentDate;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Message other = (Message) obj;
            if (!Objects.equals(this.subject, other.subject)) {
                return false;
            }
            if (!Objects.equals(this.content, other.content)) {
                return false;
            }
            if (!Objects.equals(this.from, other.from)) {
                return false;
            }
            if (!Objects.equals(this.to, other.to)) {
                return false;
            }
            if (!Objects.equals(this.sentDate, other.sentDate)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.subject != null ? this.subject.hashCode() : 0);
            hash = 41 * hash + (this.content != null ? this.content.hashCode() : 0);
            hash = 41 * hash + (this.from != null ? this.from.hashCode() : 0);
            hash = 41 * hash + (this.to != null ? this.to.hashCode() : 0);
            hash = 41 * hash + (this.sentDate != null ? this.sentDate.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "subject='" + subject + '\'' +
                    ", content=" + content +
                    ", from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", sentDate=" + sentDate +
                    '}';
        }
    }
}
