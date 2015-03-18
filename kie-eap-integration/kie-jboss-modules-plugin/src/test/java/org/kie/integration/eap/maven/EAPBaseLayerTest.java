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
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.doReturn;

/**
 * This base test provides:
 * - A mocked instance of an ArtifactsHolder (EAPBaseTest.class)
 * - A mocked logger instance (EAPBaseTest.class)
 * - Mocked repository objects for aether handling. (EAPBaseTest.class)
 * - An EAP base layer with one default module:
 *   - org.hibernate:pom (module pom)
 *     - org.hibernate:hibernate-core:jar
 */
public abstract class EAPBaseLayerTest extends EAPBaseTest {

    protected static final String BASE_LAYER_NAME = "baseLayer";
    protected static final String BASE_LAYER_GROUP_ID = "org.kie";
    // The hibernate mocked module constants.
    protected static final String HIBERNATE_MODULE_NAME = "org.hibernate";
    protected static final String HIBERNATE_MODULE_SLOT = "main";
    protected static final String HIBERNATE_GROUPID = "org.hibernate";
    protected static final String HIBERNATE_MODULE_POM = "org-hibernate-main";
    protected static final String HIBERNATE_CORE_ARTIFACTID = "hibernate-core";
    protected static final String HIBERNATE_CORE_RESOURCE_NAME= HIBERNATE_MODULE_NAME + ":" + HIBERNATE_CORE_ARTIFACTID + ":jar";
    protected static final String MODULES_HIBERNATE_MODULE_POM_XML = "/modules/base/hibernate/pom.xml";

    @Mock
    protected EAPModule hibernateBaseModule;

    @Mock
    protected Artifact hibernateBaseModulePom;

    @Mock
    protected Artifact hibernateCoreDependency;

    @Mock
    protected EAPModuleResource hibernateCoreResource;

    @Mock
    protected EAPLayer baseModuleLayer;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the hibernate base module and base static layer.
        initHibernateModule();
        doReturn(new EAPBaseModuleDependency(hibernateBaseModule.getName())).when(hibernateBaseModule).createDependency();
        Collection<EAPModule> modules = new ArrayList<EAPModule>(1);
        modules.add(hibernateBaseModule);
        initMockLayer(baseModuleLayer, BASE_LAYER_NAME, modules);
    }

    protected void initHibernateModule() throws Exception{
        // Set up the hibernate module descriptor pom artifact.
        initMockArtifact(hibernateBaseModulePom, BASE_LAYER_GROUP_ID, HIBERNATE_MODULE_POM, null, "pom", null, MODULES_HIBERNATE_MODULE_POM_XML);
        // Set up the hibernate dependenciy mocked artifact.
        initMockArtifact(hibernateCoreDependency, HIBERNATE_GROUPID, HIBERNATE_CORE_ARTIFACTID, null, "jar", null);

        // Init the hibernate static module and the static layer.
        initMockModuleResource(hibernateCoreResource, HIBERNATE_CORE_RESOURCE_NAME, null, hibernateCoreDependency);
        Collection<EAPModuleResource> resources = new ArrayList<EAPModuleResource>(1);
        resources.add(hibernateCoreResource);
        initMockModule(hibernateBaseModule, HIBERNATE_MODULE_NAME, HIBERNATE_MODULE_SLOT, hibernateBaseModulePom, resources);

        // Fill artifact holder instance.
        addArtifactIntoHolder(hibernateCoreDependency, hibernateBaseModule);
    }

}
