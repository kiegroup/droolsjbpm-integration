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
import org.kie.integration.eap.maven.EAPBaseDependencyNodeTest;
import org.kie.integration.eap.maven.model.dependency.EAPCustomModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleMissingDependency;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

import java.util.List;

import static org.mockito.Mockito.*;

public class EAPStaticModulesDependencyBuilderTest extends EAPBaseDependencyNodeTest {

    private EAPStaticModulesDependencyBuilderImpl tested;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the tested instance.
        tested = new EAPStaticModulesDependencyBuilderImpl();
    }

    /**
     * Test the dependency builder for the staticLayer and baseLayer.
     * @throws Exception
     */
    @Test
    public void testScanLayer() throws Exception {
        // Build the mocked dependency node tree object.
        buildDependencyNodeTree();

        // Build the inter-module dependencies.
        tested.build(staticLayer, dependencyNode, artifactsHolder);

        // Verify the depdencnies for the modules have been added.
        verify(droolsModule, times(3)).addDependency(isA(EAPCustomModuleDependency.class));
        verify(jbpmModule, times(4)).addDependency(isA(EAPCustomModuleDependency.class));

        // TODO: Verify the exact dependencies to the concrete modules.
        /*EAPCustomModuleDependency jbpmDep = new EAPCustomModuleDependency("org.jbpm");
        jbpmDep.setSlot("1.0");
        jbpmDep.setOptional(false);

        EAPBaseModuleDependency hibernateDep = new EAPBaseModuleDependency("org.hibernate");
        jbpmDep.setSlot("main");
        jbpmDep.setOptional(false);

        EAPCustomModuleDependency droolsDep = new EAPCustomModuleDependency("org.drools");
        jbpmDep.setSlot("1.0");
        jbpmDep.setOptional(false);

        verify(droolsModule, times(2)).addDependency(jbpmDep);
        verify(droolsModule, times(1)).addDependency(hibernateDep);

        verify(jbpmModule, times(2)).addDependency(droolsDep);
        verify(jbpmModule, times(2)).addDependency(hibernateDep);*/
    }

    /**
     * Test the dependency builder for the staticLayer and baseLayer.
     * Added an artifact in the dependencyNode mocked object in order to produce a module missing dependency instance.
     * @throws Exception
     */
    @Test
    public void testScanLayerWithMissingDependency() throws Exception {
        // Build the mocked dependency node tree object.
        buildDependencyNodeTree(new EAPDependencyNodeMockInterceptor() {
            @Override
            public void addChildren(Artifact artifact, List<DependencyNode> children) {
                // Add a missing dependency from org.jbpm:jbpm-executor:jar resource.
                if (artifact == jbpmExecutorDependency) {
                    Artifact missingDepArtifact = mock(Artifact.class);
                    // Set up the drools module descriptor pom artifact.
                    initMockArtifact(missingDepArtifact, "org.kie", "missing-artifact", "1.0", "jar", null);
                    DependencyNode missingDepNode = createDependencyNodeMock(missingDepArtifact, COMPILE_SCOPE, null);
                    children.add(missingDepNode);
                }
            }
        });

        // Build the inter-module dependencies.
        tested.build(staticLayer, dependencyNode, artifactsHolder);

        // Verify the depdencnies for the modules have been added.
        verify(droolsModule, times(3)).addDependency(isA(EAPCustomModuleDependency.class));
        verify(jbpmModule, times(4)).addDependency(isA(EAPCustomModuleDependency.class));
        verify(jbpmModule, times(1)).addDependency(isA(EAPModuleMissingDependency.class));
    }


    @After
    public void tearDown() throws Exception {

    }
}
