/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.appformer.maven.integration.MavenRepository;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.jbpm.springboot.samples.entities.Person;
import org.jbpm.springboot.samples.persistence.AbstractJBPMPersistenceUnitPostProcessorMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.appformer.maven.integration.MavenRepository.getMavenRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {JBPMApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-jpa.properties")
@DirtiesContext(classMode=ClassMode.AFTER_CLASS)
public class ExtendedEMFBusinessProcessTest {

    static final String ARTIFACT_ID = "evaluation";
    static final String GROUP_ID = "org.jbpm.test";
    static final String VERSION = "1.0.0";

    private KModuleDeploymentUnit unit = null;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private List<JBPMPersistenceUnitPostProcessor> jbpmPersistenceUnitPostProcessors;

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
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION, null, null, "PER_PROCESS_INSTANCE");
        deploymentService.deploy(unit);
    }

    @After
    public void cleanup() {

        deploymentService.undeploy(unit);
    }

    @Test
    public void testProcessStartAndAbort() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("employee", "john");
        parameters.put("reason", "SpringBoot jBPM evaluation");
        long processInstanceId = processService.startProcess(unit.getIdentifier(), "evaluation");
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        processService.abortProcessInstance(processInstanceId);

        Person person = new Person();
        person.setName("john");

        EntityManager em = entityManagerFactory.createEntityManager();

        em.persist(person);

        assertNotNull(person.getId());

        em.close();
    }

    @Test
    public void processorsInitialization() {
        assertEquals(2, jbpmPersistenceUnitPostProcessors.size());
        List<MutablePersistenceUnitInfo> persistenceUnitInfos = new ArrayList<>();
        jbpmPersistenceUnitPostProcessors.forEach(postProcessor -> assertTrue(postProcessor instanceof AbstractJBPMPersistenceUnitPostProcessorMock));
        jbpmPersistenceUnitPostProcessors.stream().
                map(postProcessor -> (AbstractJBPMPersistenceUnitPostProcessorMock) postProcessor)
                .forEach(postProcessor -> {
                    assertEquals("JBPMPersistencePostProcessor: " + postProcessor.getName() + " was not invoked the expected times.", 1, postProcessor.getInvocations());
                    persistenceUnitInfos.add(postProcessor.getPersistenceUnitInfo());
                });
        assertEquals(persistenceUnitInfos.get(0), persistenceUnitInfos.get(1));
    }
}

