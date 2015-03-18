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
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.EAPBaseLayerTest;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPArtifactResource;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.model.resource.EAPUnresolvableArtifactResource;
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EAPStaticModulesScannerTest extends EAPBaseLayerTest {

    private static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML = "/modules/static/drools/org.drools-static-module-pom.xml";
    private static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENAME_XML = "/modules/static/drools/org.drools-static-module-pom-nomodulename.xml";
    private static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENSLOT_XML = "/modules/static/drools/org.drools-static-module-pom-nomoduleslot.xml";
    private static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENLOCATION_XML = "/modules/static/drools/org.drools-static-module-pom-nomodulelocation.xml";
    private static final String MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML_FORCE_EXPORTS = "/modules/static/drools/org.drools-static-module-pom-forceexports.xml";
    private static final String MODULES_STATIC_DUPLICATED_MODULE_NAME_POM_XML = "/modules/static/duplicated-module-pom.xml";
    @Mock
    private Artifact droolsStaticModulePom;

    @Mock
    private Artifact droolsTemplatesDependency;

    @Mock
    private Artifact droolsDecisionTablesDependency;

    private EAPStaticModulesScanner tested;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the tested instance.
        tested = new EAPStaticModulesScanner();
        tested.setLogger(logger);


        // Set up the org.drools pom artifact mock object.
        initDroolsStaticModulePom(MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML);

        // Set up the drools dependencies mocked artifacts.
        initMockArtifact(droolsTemplatesDependency, "org.drools", "drools-templates", null, "jar", null);
        initMockArtifact(droolsDecisionTablesDependency, "org.drools", "drools-decisiontables", null, "jar", null);
    }

    protected void initDroolsStaticModulePom(String pomUri) throws Exception {
        initMockArtifact(droolsStaticModulePom, "org.kie", "org-drools", "1.0", "pom", null, pomUri);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - true
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts present and resolved in artifacts holder.
     * - base static layer is set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayer() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(true);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, true, EAPArtifactResource.class, true);
    }

    /**
     * Test with these scanner using a module duplicated in module base layer.
     * @throws Exception
     */
    @Test
    public void testScanLayerDuplicatedModuleInBaeLayer() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";

        Artifact duplicatedModuleArtifact = mock(Artifact.class);
        initMockArtifact(duplicatedModuleArtifact, "org.kie", "org-duplicated-module", "1.0", "pom", null, MODULES_STATIC_DUPLICATED_MODULE_NAME_POM_XML);

        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        moduleArtifacts.add(duplicatedModuleArtifact);
        
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(true);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        Exception result = null;
        try {
            EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        } catch (Exception e) {
            result = e;
        }

        assertNotNull(result);
        assertEquals(result.getClass(), EAPModuleDefinitionException.class);
    }



    /**
     * Test with these scanner options:
     * - scanStaticDependencies - true
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts NOT present and resolved in artifacts holder.
     * - base static layer is set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerModuleArtifactsNotInHolder() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(true);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Clean artifacts in holder.
        cleanArtifactsInHolder();

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, true, EAPUnresolvableArtifactResource.class, true);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - true
     * - scanResources - true
     * - artifactTreeResolved - false
     * - Artifacts present and resolved in artifacts holder.
     * - base static layer is set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerTreeNotResolved() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(false);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, true, EAPArtifactResource.class, true);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - false
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts present and resolved in artifacts holder.
     * - base static layer is set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoScanStaticDeps() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(false);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(false);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, true, EAPArtifactResource.class, false);
        EAPModule droolsModule = droolsLayer.getModule("org.drools:1.0");
        assertTrue(droolsModule.getDependencies().size() == 0);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - false
     * - scanResources - false
     * - artifactTreeResolved - true
     * - Artifacts not present and not resolved in artifacts holder.
     * - base static layer is set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoScanStaticDepsNeitherResources() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(false);
        tested.setScanResources(false);
        tested.setArtifactTreeResolved(false);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, false, EAPArtifactResource.class, false);
        EAPModule droolsModule = droolsLayer.getModule("org.drools:1.0");
        assertTrue(droolsModule.getResources().size() == 0);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - true
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts present and resolved in artifacts holder.
     * - base static layer is NOT set.
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoBaseLayer() throws Exception {
        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(false);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        assertDroolsLayer(droolsLayer, layerName, true, EAPArtifactResource.class, false);
        EAPModule droolsModule = droolsLayer.getModule("org.drools:1.0");
        assertTrue(droolsModule.getDependencies().size() == 0);
    }

    /**
     * Test with these scanner options:
     * - scanStaticDependencies - true
     * - scanResources - true
     * - artifactTreeResolved - true
     * - Artifacts present and resolved in artifacts holder.
     * - base static layer is set.
     * - static dependencies contains harcoded export values
     *
     * Module dependencies are already resolved in the artifacts holder instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerForceExports() throws Exception {
        // Set up the org.drools pom artifact mock object.
        initDroolsStaticModulePom(MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_XML_FORCE_EXPORTS);

        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(true);
        tested.setBaseModulesLayer(baseModuleLayer);

        // Create the artifact and the base module for javax.api EAP base module.
        EAPModule javaxApiModule = mock(EAPModule.class);
        initMockModule(javaxApiModule, "javax.api", "main", null);
        when(baseModuleLayer.getModule("javax.api:main")).thenReturn(javaxApiModule);

        // Add the module dependencies in artifacts holder.
        addArtifactIntoHolder(droolsTemplatesDependency);
        addArtifactIntoHolder(droolsDecisionTablesDependency);

        //  Test the module scan.
        EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);

        // Assert the export value for dependencies.
        EAPModule droolsModule = droolsLayer.getModule("org.drools:1.0");
        assertNotNull(droolsModule);
        assertNotNull(droolsModule.getDependencies());
        assertTrue(droolsModule.getDependencies().size() == 2);
        EAPModuleDependency hibernateDependency = droolsModule.getDependency("org.hibernate:main");
        EAPModuleDependency javaxApiDependency = droolsModule.getDependency("javax.api:main");


        assertNotNull(hibernateDependency);
        assertTrue(hibernateDependency instanceof EAPStaticModuleDependency);
        assertEquals(hibernateDependency.getName(), "org.hibernate");
        assertEquals(hibernateDependency.getSlot(), "main");
        assertTrue(hibernateDependency.isExport() == false);

        assertNotNull(javaxApiDependency);
        assertTrue(javaxApiDependency instanceof EAPStaticModuleDependency);
        assertEquals(javaxApiDependency.getName(), "javax.api");
        assertEquals(javaxApiDependency.getSlot(), "main");
        assertTrue(javaxApiDependency.isExport() == true);
    }

    /**
     * Test module without module name pom property.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoModuleName() throws Exception {
        testScanLayerModuleException(MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENAME_XML, EAPModuleDefinitionException.class);
    }

    /**
     * Test module without module slot pom property.
     * @throws Exception
     */
    @Test
    public void testScanLayerNoModuleSlot() throws Exception {
        testScanLayerModuleException(MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENSLOT_XML, EAPModuleDefinitionException.class);
    }

    /**
     * Test module without module location pom property.
     * @throws Exception
     */
    @Test
    public void testScanLayerModuleNoModuleLocation() throws Exception {
        testScanLayerModuleException(MODULES_STATIC_ORG_DROOLS_STATIC_MODULE_POM_NOMODULENLOCATION_XML, EAPModuleDefinitionException.class);
    }

    /**
     * Test that module scan throws an Exception.
     * @throws Exception
     */
    protected  void testScanLayerModuleException(String pomUri, Class theExceptionClazz) throws Exception {
        // Set up the org.drools pom artifact mock object.
        initDroolsStaticModulePom(pomUri);

        // Configure the tested instance.
        String layerName = "droolsLayer";
        Collection<Artifact> moduleArtifacts = new ArrayList<Artifact>(1);
        moduleArtifacts.add(droolsStaticModulePom);
        tested.setScanStaticDependencies(true);
        tested.setScanResources(true);
        tested.setArtifactTreeResolved(false);


        //  Test the module scan.
        Exception result = null;
        try {
            EAPLayer droolsLayer = tested.scan(layerName, moduleArtifacts, null, artifactsHolder);
        } catch (Exception e) {
            result = e;
        }

        assertNotNull(result);
        assertEquals(result.getClass(), theExceptionClazz);
    }

    protected void assertDroolsLayer(EAPLayer layer, String layerName, boolean assertResources, Class resourcesClazz, boolean assertStaticDependencies) {
        assertEquals(layerName, layer.getName());
        EAPModule droolsModule = layer.getModule("org.drools:1.0");
        assertNotNull(droolsModule);
        assertEquals(droolsModule.getName(), "org.drools");
        assertEquals(droolsModule.getLocation(), "org/drools");
        assertEquals(droolsModule.getSlot(), "1.0");
        assertEquals(droolsModule.getUniqueId(), "org.drools:1.0");
        assertEquals(droolsModule.getArtifact(), droolsStaticModulePom);
        assertEquals(droolsModule.getLayer(), layer);

        if (assertResources) {
            assertNotNull(droolsModule.getResources());
            assertTrue(droolsModule.getResources().size() == 2);
            Collection<EAPModuleResource> resources = droolsModule.getResources();
            for (EAPModuleResource resource : resources) {
                assertEquals(resource.getClass(), resourcesClazz);
                // TODO: Assert resources.
            }

        }

        if (assertStaticDependencies) {
            assertNotNull(droolsModule.getDependencies());
            assertTrue(droolsModule.getDependencies().size() > 0);
            EAPModuleDependency hibernateDependency = droolsModule.getDependency("org.hibernate:main");
            assertNotNull(hibernateDependency);
            assertTrue(hibernateDependency instanceof EAPStaticModuleDependency);
            assertEquals(hibernateDependency.getName(), "org.hibernate");
            assertEquals(hibernateDependency.getSlot(), "main");
        }
    }


    @After
    public void tearDown() throws Exception {

    }
}
