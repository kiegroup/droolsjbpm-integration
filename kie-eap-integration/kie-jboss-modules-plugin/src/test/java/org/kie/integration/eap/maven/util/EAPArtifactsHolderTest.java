/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tested methods:
 * - add(Artifact obj)
 * - add(Artifact obj, EAPModule module)
 * - setModule(String artifactCoordinates, EAPModule module)
 * - setModule(Artifact artifact, EAPModule module)
 * - getArtifact(String artifactCoordinates)
 * - getModule(String artifactCoordinates)
 * - getArtifact(Artifact artifact)
 * - getModule(Artifact artifact)
 *
 * Tested exceptions:
 * - org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException
 */
public class EAPArtifactsHolderTest {

    protected static final String DROOLS_MODULE_GROUPID = "org.drools";
    protected static final String DROOLS_MODULE_ARTIFACTID = "org-drools";
    protected static final String DROOLS_MODULE_VERSION = "1.0";
    protected static final String DROOLS_MODULE_TYPE = "jar";
    protected static final String JBPM_MODULE_GROUPID = "org.jbpm";
    protected static final String JBPM_MODULE_ARTIFACTID = "org-jbpm";
    protected static final String JBPM_MODULE_VERSION = "1.0";
    protected static final String JBPM_MODULE_TYPE = "jar";
    protected static final String JBPM_MODULE_NAME= "org.jbpm-name";
    protected static final String JBPM_MODULE_SLOT = "jbpm-slot";
    protected static final String DROOLS_MODULE_NAME= "org.drools-name";
    protected static final String DROOLS_MODULE_SLOT = "drools-slot";

    protected EAPArtifactsHolder artifactsHolder;

    @Mock
    protected org.eclipse.aether.RepositorySystem repoSystem;

    @Mock
    protected RepositorySystemSession repoSession;

    @Mock
    protected List<RemoteRepository> remoteRepos;

    @Before
    public void setUp() throws Exception {
        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);

        // Init the artifacts holder tested object.
        artifactsHolder = new EAPArtifactsHolder(repoSystem, repoSession, remoteRepos);
    }

    @Test
    public void testAddArtifact() throws Exception {
        // Create mocked artifact objects.
        Artifact droolsArtifact = createDroolsArtifact();
        String droolsArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(droolsArtifact);
        Artifact jbpmArtifact = createJBPMArtifact();
        String jbpmArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(jbpmArtifact);

        // Add the mocked artifacts into the artifact holder instance.
        artifactsHolder.add(droolsArtifact);
        artifactsHolder.add(jbpmArtifact);

        // Retrieve the drools artifact from the artifact holder instance.
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifactCoords));
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifact));
        // Retrieve the jbpm artifact from the artifact holder instance.
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifactCoords));
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifact));
        // Retrieve a non existing artifact from the artifact holder instance.
        assertNull(artifactsHolder.get("org.drools:org-drools:jar:1.1"));

    }


    @Test
    public void testAddArtifactAndModule() throws Exception {
        // Create mocked artifact objects.
        Artifact droolsArtifact = createDroolsArtifact();
        String droolsArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(droolsArtifact);
        Artifact jbpmArtifact = createJBPMArtifact();
        String jbpmArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(jbpmArtifact);

        // Create the mocked drools and jbpm modules.
        EAPModule droolsModule = createDroolsModule();
        EAPModule jbpmModule = createJBPMModule();

        // Add the mocked artifacts into the artifact holder instance and the related modules.
        artifactsHolder.add(droolsArtifact, droolsModule);
        artifactsHolder.add(jbpmArtifact, jbpmModule);

        // Retrieve the drools artifact from the artifact holder instance.
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifactCoords));
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifact));
        // Retrieve the jbpm artifact from the artifact holder instance.
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifactCoords));
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifact));
        // Retrieve a non existing artifact from the artifact holder instance.
        assertNull(artifactsHolder.get("org.drools:org-drools:jar:1.1"));

        // Retrieve the module for drools artifact.
        assertEquals(droolsModule, artifactsHolder.getModule(droolsArtifactCoords));
        assertEquals(droolsModule, artifactsHolder.getModule(droolsArtifact));

        // Retrieve the module for jbpm artifact.
        assertEquals(jbpmModule, artifactsHolder.getModule(jbpmArtifactCoords));
        assertEquals(jbpmModule, artifactsHolder.getModule(jbpmArtifact));
    }

    @Test
    public void testAddArtifactAndSetModule() throws Exception {
        // Create mocked artifact objects.
        Artifact droolsArtifact = createDroolsArtifact();
        String droolsArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(droolsArtifact);
        Artifact jbpmArtifact = createJBPMArtifact();
        String jbpmArtifactCoords = EAPArtifactUtils.getArtifactCoordinates(jbpmArtifact);

        // Create the mocked drools and jbpm modules.
        EAPModule droolsModule = createDroolsModule();
        EAPModule jbpmModule = createJBPMModule();

        // Add the mocked artifacts into the artifact holder instance.
        artifactsHolder.add(droolsArtifact);
        artifactsHolder.add(jbpmArtifact);

        // Set the module related to each artifact.
        artifactsHolder.setModule(droolsArtifact, droolsModule);
        artifactsHolder.setModule(jbpmArtifactCoords, jbpmModule);

        // Retrieve the drools artifact from the artifact holder instance.
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifactCoords));
        assertEquals(droolsArtifact, artifactsHolder.getArtifact(droolsArtifact));
        // Retrieve the jbpm artifact from the artifact holder instance.
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifactCoords));
        assertEquals(jbpmArtifact, artifactsHolder.getArtifact(jbpmArtifact));
        // Retrieve a non existing artifact from the artifact holder instance.
        assertNull(artifactsHolder.get("org.drools:org-drools:jar:1.1"));

        // Retrieve the module for drools artifact.
        assertEquals(droolsModule, artifactsHolder.getModule(droolsArtifactCoords));
        assertEquals(droolsModule, artifactsHolder.getModule(droolsArtifact));

        // Retrieve the module for jbpm artifact.
        assertEquals(jbpmModule, artifactsHolder.getModule(jbpmArtifactCoords));
        assertEquals(jbpmModule, artifactsHolder.getModule(jbpmArtifact));
    }

    @Test(expected = EAPModuleResourceDuplicationException.class)
    public void testModuleResourceDuplicationException() throws Exception {
        // Create mocked artifact objects.
        Artifact droolsArtifact = createDroolsArtifact();
        Artifact jbpmArtifact = createJBPMArtifact();

        // Create the mocked drools and jbpm modules.
        EAPModule droolsModule = createDroolsModule();
        EAPModule jbpmModule = createJBPMModule();

        // Create another dummy module.
        EAPModule dummyModule = createModule("dummy-name","dummy-slot");

        // Add the mocked artifacts into the artifact holder instance.
        artifactsHolder.add(droolsArtifact, droolsModule);
        artifactsHolder.add(jbpmArtifact, jbpmModule);

        // Force the exception trying to add droolsArtifact in the dummy module, which is already added in droolsModule;
        artifactsHolder.add(droolsArtifact, dummyModule);
    }

    protected Artifact createJBPMArtifact() {
        return createArtifact(JBPM_MODULE_GROUPID, JBPM_MODULE_ARTIFACTID, JBPM_MODULE_VERSION, JBPM_MODULE_TYPE, null);
    }

    protected Artifact createDroolsArtifact() {
        return createArtifact(DROOLS_MODULE_GROUPID, DROOLS_MODULE_ARTIFACTID, DROOLS_MODULE_VERSION, DROOLS_MODULE_TYPE, null);
    }

    protected Artifact createArtifact(String groupId, String artifactId, String version, String type, String classifier) {
        Artifact artifact = mock(Artifact.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getVersion()).thenReturn(version != null ? version : "");
        when(artifact.getExtension()).thenReturn(type != null ? type : "");
        when(artifact.getClassifier()).thenReturn(classifier != null ? classifier : "");

        return artifact;
    }

    protected EAPModule createJBPMModule() {
        return createModule(JBPM_MODULE_NAME, JBPM_MODULE_SLOT);
    }

    protected EAPModule createDroolsModule() {
        return createModule(DROOLS_MODULE_NAME, DROOLS_MODULE_SLOT);
    }

    protected EAPModule createModule(String name, String slot) {
        EAPModule module = mock(EAPModule.class);
        when(module.getName()).thenReturn(name);
        when(module.getSlot()).thenReturn(slot);
        return module;
    }

    @After
    public void tearDown() throws Exception {

    }

}
