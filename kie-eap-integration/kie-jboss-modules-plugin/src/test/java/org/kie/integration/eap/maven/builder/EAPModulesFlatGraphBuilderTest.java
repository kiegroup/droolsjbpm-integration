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
package org.kie.integration.eap.maven.builder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.EAPStaticLayerTest;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EAPModulesFlatGraphBuilderTest extends EAPStaticLayerTest {

    private EAPModulesFlatGraphBuilder tested;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the tested instance.
        tested = new EAPModulesFlatGraphBuilder();
    }

    /**
     * Test the flat graph builder for the staticLayer.
     * @throws Exception
     */
    @Test
    public void testBuildGraph() throws Exception {
        // Build the dependencies for the staticLayer.
        buildStaticLayerModuleDependencies();

        EAPModulesGraph result = tested.build(STATIC_LAYER_NAME, staticLayer);

        // Assert the graph
        assertNotNull(result);
        assertEquals(result.getDistributionName(), STATIC_LAYER_NAME);
        assertNotNull(result.getNodes());
        assertTrue(result.getNodes().size() == 2);

        // Assert the drools module node graph.
        EAPModuleGraphNode droolsNode = result.getNodes().get(0);
        assertNotNull(droolsNode);
        assertEquals(droolsNode.getName(), droolsModule.getName());
        assertEquals(droolsNode.getSlot(), droolsModule.getSlot());
        assertEquals(droolsNode.getUniqueId(), droolsModule.getUniqueId());
        assertNotNull(droolsNode.getResources());
        assertTrue(droolsNode.getResources().size() == 2);
        assertNotNull(droolsNode.getDependencies());
        assertTrue(droolsNode.getDependencies().size() == 3);
        EAPModuleGraphNodeDependency droolsJbpmDep1 = droolsNode.getDependencies().get(0);
        EAPModuleGraphNodeDependency droolsHibernateDep = droolsNode.getDependencies().get(1);
        EAPModuleGraphNodeDependency droolsJbpmDep2 = droolsNode.getDependencies().get(2);
        assertGraphDependency(droolsJbpmDep1, JBPM_MODULE_NAME, JBPM_MODULE_SLOT, false);
        assertGraphDependency(droolsHibernateDep, HIBERNATE_MODULE_NAME, HIBERNATE_MODULE_SLOT, false);
        assertGraphDependency(droolsJbpmDep2, JBPM_MODULE_NAME, JBPM_MODULE_SLOT, false);

        // Assert the jbpm module node graph.
        EAPModuleGraphNode jbpmNode = result.getNodes().get(1);
        assertNotNull(jbpmNode);
        assertEquals(jbpmNode.getName(), jbpmModule.getName());
        assertEquals(jbpmNode.getSlot(), jbpmModule.getSlot());
        assertEquals(jbpmNode.getUniqueId(), jbpmModule.getUniqueId());
        assertNotNull(jbpmNode.getResources());
        assertTrue(jbpmNode.getResources().size() == 2);
        assertNotNull(jbpmNode.getDependencies());
        assertTrue(jbpmNode.getDependencies().size() == 4);
        EAPModuleGraphNodeDependency jbpmHibernateDep1 = jbpmNode.getDependencies().get(0);
        EAPModuleGraphNodeDependency jbpmDroolsDep1 = jbpmNode.getDependencies().get(1);
        EAPModuleGraphNodeDependency jbpmDroolsDep2 = jbpmNode.getDependencies().get(2);
        EAPModuleGraphNodeDependency jbpmHibernateDep2 = jbpmNode.getDependencies().get(3);
        assertGraphDependency(jbpmHibernateDep1, HIBERNATE_MODULE_NAME, HIBERNATE_MODULE_SLOT, false);
        assertGraphDependency(jbpmDroolsDep1, DROOLS_MODULE_NAME, DROOLS_MODULE_SLOT, false);
        assertGraphDependency(jbpmDroolsDep2, DROOLS_MODULE_NAME, DROOLS_MODULE_SLOT, false);
        assertGraphDependency(jbpmHibernateDep2, HIBERNATE_MODULE_NAME, HIBERNATE_MODULE_SLOT, false);
    }

    protected void assertGraphDependency(EAPModuleGraphNodeDependency dependency, String name, String slot, boolean export) {
        assertNotNull(dependency);
        assertEquals(dependency.getName(), name);
        assertEquals(dependency.getSlot(), slot);
        assertEquals(dependency.isExport(), export);
    }


    @After
    public void tearDown() throws Exception {

    }
}
