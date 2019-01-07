/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.samples;

import java.io.File;
import java.util.Collection;

import org.appformer.maven.integration.MavenRepository;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.springboot.samples.handlers.CustomWorkItemHandler;
import org.jbpm.springboot.samples.handlers.LogWorkItemHandler;
import org.jbpm.springboot.samples.handlers.WidWorkItemHandler;
import org.jbpm.springboot.samples.listeners.CustomProcessEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class ConfigurationTest {
    
    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";

    private KModuleDeploymentUnit unit = null;
     
    @Autowired
    private DeploymentService deploymentService;
    
    
    @BeforeClass
    public static void generalSetup() {
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        File kjar = new File("../kjars/evaluation/jbpm-module.jar");
        File pom = new File("../kjars/evaluation/pom.xml");
        MavenRepository repository = getMavenRepository();
        repository.installArtifact(releaseId, kjar, pom);

        EntityManagerFactoryManager.get().clear();
    }
    
    
    @Before
    public void setup() {
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentService.deploy(unit);
    }
    
    @After
    public void cleanup() {

        deploymentService.undeploy(unit);
    }
 
    @Test
    public void testWorkItemHandlerRegistration() {
        assertNotNull(unit);
        RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(unit.getIdentifier());
        
        assertNotNull("Runtime manager is missing for deployed kjar", runtimeManager);
        
        RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        
        WorkItemManager manager = engine.getKieSession().execute(new ExecutableCommand<WorkItemManager>() {

            private static final long serialVersionUID = 8170184822180761325L;

            @Override
            public WorkItemManager execute(Context context) {
                KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                return ksession.getWorkItemManager();
            }
        });
        assertNotNull(manager);
        assertTrue(manager instanceof JPAWorkItemManager);
        
        WorkItemHandler handler = ((JPAWorkItemManager) manager).getWorkItemHandler("Custom");
        assertNotNull(handler);
        assertTrue(handler instanceof CustomWorkItemHandler);
        
        handler = ((JPAWorkItemManager) manager).getWorkItemHandler("WidCustom");
        assertNotNull(handler);
        assertTrue(handler instanceof WidWorkItemHandler);
        
        handler = ((JPAWorkItemManager) manager).getWorkItemHandler("Log");
        assertNotNull(handler);
        assertTrue(handler instanceof LogWorkItemHandler);
        
        runtimeManager.disposeRuntimeEngine(engine);
        
    }  
    
    @Test
    public void testProcessEventListenerRegistration() {
        assertNotNull(unit);
        RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(unit.getIdentifier());
        
        assertNotNull("Runtime manager is missing for deployed kjar", runtimeManager);
        
        RuntimeEngine engine = runtimeManager.getRuntimeEngine(EmptyContext.get());
        
        Collection<ProcessEventListener> pListeners = engine.getKieSession().getProcessEventListeners();
        assertNotNull(pListeners);
        assertTrue(pListeners.stream().anyMatch(listener -> listener instanceof CustomProcessEventListener));
        
        runtimeManager.disposeRuntimeEngine(engine);
        
    }  
}

