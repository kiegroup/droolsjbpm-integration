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
package org.kie.integration.eap.maven;

import org.junit.Before;
import org.kie.integration.eap.maven.model.dependency.EAPBaseModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPCustomModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * This base test provides:
 * - A mocked instance of an ArtifactsHolder (EAPBaseTest.class)
 * - A mocked logger instance (EAPBaseTest.class)
 * - Mocked repository objects for aether handling. (EAPBaseTest.class)
 * - An EAP base layer with one default module: org.hibernate:main (EAPBaseLayerTest.class)
 * - An EAP static layer with two modules by default:
 *   - org.drools:1.0:pom
 *      - org.drools:drools-templates:jar
 *      - org.drools:drools-decisiontables:jar
 *   - org.jbpm:1.0:pom
 *      - org.jbpm:jbpm-executor:jar
 *      - org.jbpm:jbpm-persistence-jpa:jar
 */
public abstract class EAPStaticLayerTest extends EAPBaseLayerTest {

    protected static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML = "/modules/static/drools/org.drools-static-module-pom.xml";
    protected static final String MODULES_STATIC_ORG_JBPM_STATIC_MODULE_POM_XML = "/modules/static/jbpm/pom.xml";

    // Static Layer constants.
    protected static final String STATIC_MODULE_GROUPID = "org.kie";
    protected static final String STATIC_LAYER_NAME = "staticLayer";
    // Drools module constants.
    protected static final String DROOLS_MODULE_ARTIFACTID = "org-drools";
    protected static final String DROOLS_MODULE_VERSION = "1.0";
    protected static final String DROOLS_RESOURCES_GROUPID = "org.drools";
    protected static final String DROOLS_TEMPLATES_RESOURCE_ARTIFACTID = "drools-templates";
    protected static final String DROOLS_DECISION_TABLES_RESOURCE_ARTIFACTID = "drools-decisiontables";
    protected static final String DROOLS_TEMPLATES_RESOURCE_NAME= DROOLS_RESOURCES_GROUPID + ":" + DROOLS_TEMPLATES_RESOURCE_ARTIFACTID + ":jar";
    protected static final String DROOLS_DECISION_TABLES_RESOURCE_NAME= DROOLS_RESOURCES_GROUPID + ":" + DROOLS_DECISION_TABLES_RESOURCE_ARTIFACTID + ":jar";
    protected static final String DROOLS_MODULE_NAME = "org.drools";
    protected static final String DROOLS_MODULE_SLOT = "1.0";
    // jBBPM module constants.
    protected static final String JBPM_MODULE_ARTIFACTID = "org-jbpm";
    protected static final String JBPM_MODULE_VERSION = "1.0";
    protected static final String JBPM_RESOURCES_GROUPID = "org.jbpm";
    protected static final String JBPM_EXECUTOR_ARTIFACTID = "jbpm-executor";
    protected static final String JBPM_JPA_RESOURCE_ARTIFACTID = "jbpm-persistence";
    protected static final String JBPM_EXECUTOR_RESOURCE_NAME= JBPM_MODULE_ARTIFACTID + ":" + JBPM_EXECUTOR_ARTIFACTID + ":jar";
    protected static final String JBPM_JPA_RESOURCE_RESOURCE_NAME= JBPM_MODULE_ARTIFACTID + ":" + JBPM_JPA_RESOURCE_ARTIFACTID + ":jar";
    protected static final String JBPM_MODULE_NAME = "org.jbpm";
    protected static final String JBPM_MODULE_SLOT = "1.0";

    @Mock
    protected Artifact droolsStaticModulePom;

    @Mock
    protected Artifact droolsTemplatesDependency;

    @Mock
    protected Artifact droolsDecisionTablesDependency;

    @Mock
    protected EAPModule droolsModule;

    @Mock
    protected EAPModuleResource droolsTemplatesResource;

    @Mock
    protected EAPModuleResource droolsDecisionTablesResource;

    @Mock
    protected Artifact jbpmStaticModulePom;

    @Mock
    protected Artifact jbpmExecutorDependency;

    @Mock
    protected Artifact jbpmJPADependency;

    @Mock
    protected EAPModule jbpmModule;

    @Mock
    protected EAPModuleResource jbpmExecutorResource;

    @Mock
    protected EAPModuleResource jbpmJPAResource;

    @Mock
    protected EAPLayer staticLayer;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the drools static module.
        initDroolsStaticModule();
        doReturn(new EAPCustomModuleDependency(droolsModule.getName())).when(droolsModule).createDependency();

        // Init the jbp static module.
        initjBPMStaticModule();
        doReturn(new EAPCustomModuleDependency(jbpmModule.getName())).when(jbpmModule).createDependency();

        // Init the layer mock.
        Collection<EAPModule> modules = new ArrayList<EAPModule>(2);
        modules.add(droolsModule);
        modules.add(jbpmModule);
        initMockLayer(staticLayer, STATIC_LAYER_NAME, modules);
    }

    protected void initDroolsStaticModule() throws Exception {
        // Set up the drools module descriptor pom artifact.
        initMockArtifact(droolsStaticModulePom, STATIC_MODULE_GROUPID, DROOLS_MODULE_ARTIFACTID, DROOLS_MODULE_VERSION, "pom", null, MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML);
        // Set up the drools dependencies mocked artifacts.
        initMockArtifact(droolsTemplatesDependency, DROOLS_RESOURCES_GROUPID, DROOLS_TEMPLATES_RESOURCE_ARTIFACTID, null, "jar", null);
        initMockArtifact(droolsDecisionTablesDependency, DROOLS_RESOURCES_GROUPID, DROOLS_DECISION_TABLES_RESOURCE_ARTIFACTID, null, "jar", null);

        // Init the drools static module and the static layer.
        initMockModuleResource(droolsTemplatesResource, DROOLS_TEMPLATES_RESOURCE_NAME, null, droolsTemplatesDependency);
        initMockModuleResource(droolsDecisionTablesResource, DROOLS_DECISION_TABLES_RESOURCE_NAME, null, droolsDecisionTablesDependency);
        Collection<EAPModuleResource> resources = new ArrayList<EAPModuleResource>(2);
        resources.add(droolsTemplatesResource);
        resources.add(droolsDecisionTablesResource);
        initMockModule(droolsModule, DROOLS_MODULE_NAME, DROOLS_MODULE_SLOT, droolsStaticModulePom, resources);

        // Fill artifact holder instance.
        addArtifactIntoHolder(droolsTemplatesDependency, droolsModule);
        addArtifactIntoHolder(droolsDecisionTablesDependency, droolsModule);
    }

    protected void initjBPMStaticModule() throws Exception {
        // Set up the drools module descriptor pom artifact.
        initMockArtifact(jbpmStaticModulePom, STATIC_MODULE_GROUPID, JBPM_MODULE_ARTIFACTID, JBPM_MODULE_VERSION, "pom", null, MODULES_STATIC_ORG_JBPM_STATIC_MODULE_POM_XML);
        // Set up the jbpm dependencies mocked artifacts.
        initMockArtifact(jbpmExecutorDependency, JBPM_RESOURCES_GROUPID, JBPM_EXECUTOR_ARTIFACTID, null, "jar", null);
        initMockArtifact(jbpmJPADependency, JBPM_RESOURCES_GROUPID, JBPM_JPA_RESOURCE_ARTIFACTID, null, "jar", null);

        // Init the jbpm static module and the static layer.
        initMockModuleResource(jbpmExecutorResource, JBPM_EXECUTOR_RESOURCE_NAME, null, jbpmExecutorDependency);
        initMockModuleResource(jbpmJPAResource, JBPM_JPA_RESOURCE_RESOURCE_NAME, null, jbpmJPADependency);
        Collection<EAPModuleResource> resources = new ArrayList<EAPModuleResource>(2);
        resources.add(jbpmExecutorResource);
        resources.add(jbpmJPAResource);
        initMockModule(jbpmModule, JBPM_MODULE_NAME, JBPM_MODULE_SLOT, jbpmStaticModulePom, resources);

        // Fill artifact holder instance.
        addArtifactIntoHolder(jbpmExecutorDependency, jbpmModule);
        addArtifactIntoHolder(jbpmJPADependency, jbpmModule);
    }

    /**
     * Builds the module dependency instances considering this tree structure:
     *
     *      - org.kie:org-drools:1.0:pom (drools module pom)
     *        +- org.drools:drools-templates:jar
     *        |  +- org.jbpm:jbpm-executor:jar (jbpm static module)
     *        |  +- org.hibernate:hibernate-core:jar (EAP base module)
     *        +- org.drools:drools-decisiontables:jar
     *        |  +- org.jbpm:jbpm-persistence:jar (jbpm static module)
     *      - org.kie:org-jbpm:1.0:pom (jbpm module pom)
     *        +- org.jbpm:jbpm-executor:jar
     *        |  +- org.hibernate:hibernate-core:jar (EAP base module)
     *        |  +- org.drools:drools-decisiontables:jar (drools static module)
     *        +- org.jbpm:jbpm-persistence:jar
     *        |  +- org.drools:drools-templates:jar (drools static module)
     *        |  +- org.hibernate:hibernate-core:jar (EAP base module)
     */
    protected void buildStaticLayerModuleDependencies() {
        // Drools module - mocked depdendecy objects.
        EAPModuleDependency drools_jbpmDependency = mock(EAPCustomModuleDependency.class);
        when(drools_jbpmDependency.getName()).thenReturn(JBPM_MODULE_NAME);
        when(drools_jbpmDependency.getSlot()).thenReturn(JBPM_MODULE_SLOT);
        EAPModuleDependency drools_hibernateDependency = mock(EAPBaseModuleDependency.class);
        when(drools_hibernateDependency.getName()).thenReturn(HIBERNATE_MODULE_NAME);
        when(drools_hibernateDependency.getSlot()).thenReturn(HIBERNATE_MODULE_SLOT);

        Collection<EAPModuleDependency> droolsDeps = new ArrayList<EAPModuleDependency>(2);
        droolsDeps.add(drools_jbpmDependency);
        droolsDeps.add(drools_hibernateDependency);
        droolsDeps.add(drools_jbpmDependency);

        // jBPM module - mocked depdendecy objects.
        EAPModuleDependency jbpm_droolsDependency = mock(EAPCustomModuleDependency.class);
        when(jbpm_droolsDependency.getName()).thenReturn(DROOLS_MODULE_NAME);
        when(jbpm_droolsDependency.getSlot()).thenReturn(DROOLS_MODULE_SLOT);
        EAPModuleDependency jbpm_hibernateDependency = mock(EAPBaseModuleDependency.class);
        when(jbpm_hibernateDependency.getName()).thenReturn(HIBERNATE_MODULE_NAME);
        when(jbpm_hibernateDependency.getSlot()).thenReturn(HIBERNATE_MODULE_SLOT);

        Collection<EAPModuleDependency> jbpmDeps = new ArrayList<EAPModuleDependency>(2);
        jbpmDeps.add(jbpm_hibernateDependency);
        jbpmDeps.add(jbpm_droolsDependency);
        jbpmDeps.add(jbpm_droolsDependency);
        jbpmDeps.add(jbpm_hibernateDependency);


        // Add the dependencies for the static modules.
        when(droolsModule.getDependencies()).thenReturn(droolsDeps);
        when(droolsModule.getDependency(JBPM_MODULE_NAME + ":" + JBPM_MODULE_SLOT)).thenReturn(drools_jbpmDependency);
        when(droolsModule.getDependency(HIBERNATE_MODULE_NAME + ":" + HIBERNATE_MODULE_SLOT)).thenReturn(drools_hibernateDependency);
        when(jbpmModule.getDependencies()).thenReturn(jbpmDeps);
        when(jbpmModule.getDependency(DROOLS_MODULE_NAME + ":" + DROOLS_MODULE_SLOT)).thenReturn(jbpm_droolsDependency);
        when(jbpmModule.getDependency(HIBERNATE_MODULE_NAME + ":" + HIBERNATE_MODULE_SLOT)).thenReturn(jbpm_hibernateDependency);
    }

}
