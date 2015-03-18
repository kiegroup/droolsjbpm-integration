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
package org.kie.integration.eap.maven.scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.EAPBaseTest;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

public class EAPBaseModulesScannerTest extends EAPBaseTest {

    private static final String MODULES_HIBERNATE_MODULE_POM_XML = "/modules/base/hibernate/pom.xml";
    private static final String MODULES_HIBERNATE_MODULE_NO_NAME_POM_XML = "/modules/base/hibernate/pom-nomodulename.xml";
    private static final String MODULES_HIBERNATE_MODULE_NO_SLOT_POM_XML = "/modules/base/hibernate/pom-nomoduleslot.xml";
    private static final String MODULES_COMMONS_IO_MODULE_POM_XML = "/modules/base/commons-io/pom.xml";
    private static final String BASE_LAYER_NAME = "baseLayer";

    @Mock
    private Artifact hibernateModulePom;

    @Mock
    private Artifact hibernateCoreDependency;

    @Mock
    private EAPModule hibernateModule;

    @Mock
    private Artifact commonsIOModulePom;

    @Mock
    private Artifact commonsIODependency;

    @Mock
    private EAPModule commonsIOModule;

    @Mock
    private EAPLayer baseLayer;


    private EAPBaseModulesScanner tested;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the tested instance.
        tested = new EAPBaseModulesScanner();
        tested.setLogger(logger);

        // Init the base artifact and hibnerate module mocks.
        initMockArtifact(hibernateModulePom,"org.kie", "org-hibernate-main", null, "pom", null,  MODULES_HIBERNATE_MODULE_POM_XML);
        initMockArtifact(hibernateCoreDependency,"org.hibernate", "hibernate-core", "4.2.0.SP1", "pom", null);

        // Init the base artifact and commons-io module mocks.
        initMockArtifact(commonsIOModulePom,"org.kie", "org-apache-commons-io-main", null, "pom", null,  MODULES_COMMONS_IO_MODULE_POM_XML);
        initMockArtifact(commonsIODependency,"commons-io", "commons-io", "2.1", "jar", null);
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Test with these scanner options:
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts present and resolved in artifacts holder.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayer() throws Exception {
        // Configure the tested instance.
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(hibernateModulePom);
        moduleArtifacts.add(commonsIOModulePom);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(true);

        //  Test the module scan.
        EAPLayer baseLayer = tested.scan(BASE_LAYER_NAME, moduleArtifacts, null, artifactsHolder);
        assertLayer(baseLayer, BASE_LAYER_NAME, true, EAPArtifactResource.class);
    }

    /**
     * Test with these scanner options:
     * - scanResources - false
     * - artifactTreeResolved - true
     * - Artifacts not present and not resolved in artifacts holder.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoResources() throws Exception {
        // Configure the tested instance.
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(hibernateModulePom);
        moduleArtifacts.add(commonsIOModulePom);
        tested.setScanResources(false);
        tested.setArtifactTreeResolved(false);

        //  Test the module scan.
        EAPLayer baseLayer = tested.scan(BASE_LAYER_NAME, moduleArtifacts, null, artifactsHolder);
        assertLayer(baseLayer, BASE_LAYER_NAME, false, EAPArtifactResource.class);
        EAPModule hibernateModule = baseLayer.getModule("org.hibernate:main");
        assertTrue(hibernateModule.getResources().size() == 0);
        EAPModule commonIOModule = baseLayer.getModule("org.apache.commons.io:main");
        assertTrue(commonIOModule.getResources().size() == 0);
    }

    /**
     * Test module without module name pom property.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoModuleName() throws Exception {
        testScanLayerModuleException(MODULES_HIBERNATE_MODULE_NO_NAME_POM_XML, EAPModuleDefinitionException.class);
    }

    /**
     * Test module without module slot pom property.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoModuleSlot() throws Exception {
        testScanLayerModuleException(MODULES_HIBERNATE_MODULE_NO_SLOT_POM_XML, EAPModuleDefinitionException.class);
    }

    /**
     * Test that module scan throws an Exception.
     * @throws Exception
     */
    protected  void testScanLayerModuleException(String pomUri, Class theExceptionClazz) throws Exception {
        initMockArtifact(hibernateModulePom,"org.kie", "org-hibernate-main", null, "pom", null,  pomUri);

        // Configure the tested instance.
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(hibernateModulePom);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(false);


        //  Test the module scan.
        Exception result = null;
        try {
            EAPLayer baseLayer = tested.scan(BASE_LAYER_NAME, moduleArtifacts, null, artifactsHolder);
        } catch (Exception e) {
            result = e;
        }

        assertNotNull(result);
        assertEquals(result.getClass(), theExceptionClazz);
    }

    protected void assertLayer(EAPLayer layer, String layerName, boolean assertResources, Class resourcesClazz) {
        Assert.assertTrue(layer != null);
        assertEquals(layerName, layer.getName());

        // Assert the hibernate module.
        EAPModule hibernateModule = layer.getModule("org.hibernate:main");
        assertModule(layer, hibernateModule, "org.hibernate", "main", hibernateModulePom, assertResources, resourcesClazz);

        // Assert the commons-io module.
        EAPModule commonsIOModule = layer.getModule("org.apache.commons.io:main");
        assertModule(layer, commonsIOModule, "org.apache.commons.io", "main", commonsIOModulePom, assertResources, resourcesClazz);

    }

    protected void assertModule(EAPLayer layer, EAPModule module, String moduleName, String moduleSlot, Artifact modulePom, boolean assertResources, Class resourcesClazz) {
        assertNotNull(module);
        assertEquals(module.getName(), moduleName);
        assertEquals(module.getSlot(), moduleSlot);
        assertEquals(module.getUniqueId(), moduleName + ":" + moduleSlot);
        assertEquals(module.getArtifact(), modulePom);
        assertEquals(module.getLayer(), layer);

        if (assertResources) {
            assertNotNull(module.getResources());
            assertTrue(module.getResources().size() == 1);
            Collection<EAPModuleResource> resources = module.getResources();
            for (EAPModuleResource resource : resources) {
                assertEquals(resource.getClass(), resourcesClazz);
                // TODO: Assert resources.
            }

        }
    }

}
