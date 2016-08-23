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
package org.kie.server.integrationtests.jbpm.jms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jms.XAConnectionFactory;
import javax.transaction.UserTransaction;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jms.PoolingConnectionFactory;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.jms.AsyncResponseHandler;
import org.kie.server.client.jms.BlockingResponseCallback;
import org.kie.server.client.jms.FireAndForgetResponseHandler;
import org.kie.server.client.jms.ResponseHandler;
import org.kie.server.integrationtests.category.JMSOnly;
import org.kie.server.integrationtests.category.Transactional;
import org.kie.server.integrationtests.jbpm.JbpmKieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.assertj.core.api.Assertions.*;

@Category({JMSOnly.class, Transactional.class})
public class JmsTransactionsIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @Parameterized.Parameters(name = "{index}: {0} {2}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();

        return new ArrayList<Object[]>(Arrays.asList(new Object[][]{
                {MarshallingFormat.JAXB, jmsConfiguration, new FireAndForgetResponseHandler()},
                {MarshallingFormat.JAXB, jmsConfiguration, new AsyncResponseHandler(new BlockingResponseCallback(null))},
                {MarshallingFormat.JSON, jmsConfiguration, new FireAndForgetResponseHandler()},
                {MarshallingFormat.JSON, jmsConfiguration, new AsyncResponseHandler(new BlockingResponseCallback(null))},
                {MarshallingFormat.XSTREAM, jmsConfiguration, new FireAndForgetResponseHandler()},
                {MarshallingFormat.XSTREAM, jmsConfiguration, new AsyncResponseHandler(new BlockingResponseCallback(null))}
        }));
    }

    @Parameterized.Parameter(2)
    public ResponseHandler responseHandler;

    private PoolingConnectionFactory connectionFactory;
    private UserTransaction transaction;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, RELEASE_ID);
    }

    private ProcessServicesClient createTransactionalProcessClient(List<String> capabilities) throws Exception {
        TransactionManagerServices.getConfiguration().setJournal("null").setGracefulShutdownInterval(2);
        transaction = TransactionManagerServices.getTransactionManager();

        KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
        KieServerXAConnectionFactory.connectionFactory = (XAConnectionFactory) jmsConfiguration.getConnectionFactory();

        connectionFactory = new PoolingConnectionFactory();
        connectionFactory.setClassName("org.kie.server.integrationtests.jbpm.jms.KieServerXAConnectionFactory");
        connectionFactory.setUniqueName("cf");
        connectionFactory.setMaxPoolSize(5);
        connectionFactory.setAllowLocalTransactions(true);
        connectionFactory.init();
        jmsConfiguration.setConnectionFactory(connectionFactory);

        jmsConfiguration.setCapabilities(capabilities);
        jmsConfiguration.setJmsTransactional(true);
        jmsConfiguration.setResponseHandler(responseHandler);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(jmsConfiguration);
        return kieServicesClient.getServicesClient(ProcessServicesClient.class);
    }

    @After
    public void releaseResources() {
        if (connectionFactory != null) {
            connectionFactory.close();
        }
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    @Test
    public void testTransactionCommit() throws Exception {
        List<String> capabilities = new ArrayList<String>();
        capabilities.add(KieServerConstants.CAPABILITY_BPM);
        ProcessServicesClient transactionalProcessClient = createTransactionalProcessClient(capabilities);

        transaction.begin();

        Long processInstanceId = transactionalProcessClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertThat(processInstanceId).isNull();

        transaction.commit();

        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 10);
        assertThat(processInstances).isNotNull().hasSize(1);
        assertThat(processInstances.get(0).getProcessId()).isEqualTo(PROCESS_ID_USERTASK);
    }

    @Test
    public void testTransactionRollback() throws Exception {
        List<String> capabilities = new ArrayList<String>();
        capabilities.add(KieServerConstants.CAPABILITY_BPM);
        ProcessServicesClient transactionalProcessClient = createTransactionalProcessClient(capabilities);

        transaction.begin();

        Long processInstanceId = transactionalProcessClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        assertThat(processInstanceId).isNull();

        transaction.rollback();

        List<ProcessInstance> processInstances = queryClient.findProcessInstances(0, 10);
        assertThat(processInstances).isNotNull().isEmpty();
    }

}
