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

package org.kie.spring.jbpm;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManagerFactory;

import org.drools.persistence.api.TransactionManagerFactory;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.runtime.manager.impl.migration.MigrationException;
import org.jbpm.runtime.manager.impl.migration.MigrationManager;
import org.jbpm.runtime.manager.impl.migration.MigrationSpec;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class KieSpringProcessInstanceMigrationServiceImplTest extends AbstractJbpmSpringParameterizedTest {

    private static final String ADDTASKAFTERACTIVE_ID_V1 = "process-migration-testv1.AddTaskAfterActive";
    private static final String ADDTASKAFTERACTIVE_ID_V2 = "process-migration-testv2.AddTaskAfterActive";

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][]{
                {JTA_EMF_SINGLETON_PATH, ProcessInstanceIdContext.get()}
        };
        return Arrays.asList(data);
    }

    @After
    public void cleanup() {
        System.clearProperty("org.kie.txm.factory.class");
        TransactionManagerFactory.resetInstance();
        EntityManagerFactoryManager.get().clear();
        if (context != null) {
            context.close();
            context = null;
        }
    }

    public KieSpringProcessInstanceMigrationServiceImplTest(String contextPath,
                                                            Context<?> runtimeManagerContext) {
        super(contextPath,
              runtimeManagerContext);
    }

    @Test
    public void testMigrateSingleProcessInstance() {
        System.setProperty("org.kie.txm.factory.class", "org.kie.spring.persistence.KieSpringTransactionManagerFactory");
        TransactionManagerFactory.resetInstance();
        EntityManagerFactoryManager.get().addEntityManagerFactory("org.jbpm.persistence.spring.jta", context.getBean("jbpmEMF", EntityManagerFactory.class));

        RuntimeManager firstManager = getManager();

        ((DeploymentDescriptorImpl) ((InternalRuntimeManager) firstManager).
                getDeploymentDescriptor()).setAuditPersistenceUnit("org.jbpm.persistence.spring.jta");
        ((DeploymentDescriptorImpl) ((InternalRuntimeManager) firstManager).
                getDeploymentDescriptor()).setPersistenceUnit("org.jbpm.persistence.spring.jta");

        assertNotNull(firstManager);

        RuntimeManager secondManager = getSecondManager();
        ((DeploymentDescriptorImpl) ((InternalRuntimeManager) secondManager).
                getDeploymentDescriptor()).setAuditPersistenceUnit("org.jbpm.persistence.spring.jta");
        ((DeploymentDescriptorImpl) ((InternalRuntimeManager) secondManager).
                getDeploymentDescriptor()).setPersistenceUnit("org.jbpm.persistence.spring.jta");


        assertNotNull(secondManager);

        RuntimeEngine engine = firstManager.getRuntimeEngine(runtimeManagerContext);

        ProcessInstance instance = engine.getKieSession().startProcess(ADDTASKAFTERACTIVE_ID_V1);
        assertNotNull(instance);

        long processInstanceId = instance.getId();
        assertNotNull(processInstanceId);

        org.kie.api.runtime.manager.audit.ProcessInstanceLog log = engine.getAuditService().findProcessInstance(processInstanceId);
        assertEquals(ProcessInstance.STATE_ACTIVE, log.getStatus().intValue());
        assertEquals(ADDTASKAFTERACTIVE_ID_V1, log.getProcessId());

        MigrationSpec migrationSpec = new MigrationSpec(firstManager.getIdentifier(), processInstanceId, secondManager.getIdentifier(), ADDTASKAFTERACTIVE_ID_V2);
        MigrationManager migrationManager = new MigrationManager(migrationSpec);

        org.jbpm.runtime.manager.impl.migration.MigrationReport report = null;
        try {
            report = migrationManager.migrate();

        } catch (MigrationException e) {
            report = e.getReport();
        }

        assertNotNull(report);
        assertTrue(report.isSuccessful());

        log = engine.getAuditService().findProcessInstance(processInstanceId);
        assertEquals(ProcessInstance.STATE_ACTIVE, log.getStatus().intValue());
        assertEquals(ADDTASKAFTERACTIVE_ID_V2, log.getProcessId());

        firstManager.disposeRuntimeEngine(engine);
    }
}
