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

package org.kie.server.integrationtests.drools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentRequestsIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(Worker.class);
    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "kie-concurrent";
    private static final int NR_OF_THREADS = 5;
    private static final int NR_OF_REQUESTS_PER_THREAD = 20;
    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String PERSON_OUT_IDENTIFIER = "person1";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());

        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        if (extraClasses.isEmpty()) {
            KieContainer kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
            extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
        }
    }

    @Test
    public void testCallingStatelessSessionFromMultipleThreads() throws Exception {
        List<Future<String>> futureResults = new ArrayList<Future<String>>();
        ExecutorService es = Executors.newFixedThreadPool(NR_OF_THREADS);
        for (int i = 0; i < NR_OF_THREADS; i++) {
            futureResults.add(es.submit(new Worker(createDefaultClient())));
        }
        es.shutdown();
        for (Future<String> future : futureResults) {
            assertEquals("SUCCESS", future.get());
        }
    }

    public class Worker implements Callable<String> {

        private final RuleServicesClient ruleServicesClient;

        public Worker(KieServicesClient client) {
            this.ruleServicesClient = client.getServicesClient(RuleServicesClient.class);
        }

        @Override
        public String call() {
            List<Command<?>> commands = new ArrayList<Command<?>>();

            BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

            Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
            commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

            long threadId = Thread.currentThread().getId();
            ServiceResponse<ExecutionResults> reply;
            for (int i = 0; i < NR_OF_REQUESTS_PER_THREAD; i++) {
                logger.trace("Container call #{}, thread-id={}", i, threadId);
                reply = ruleServicesClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
                logger.trace("Container reply for request #{}: {}, thread-id={}", i, reply, threadId);
                assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
            }
            return "SUCCESS";
        }
    }

}
