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
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This base test provides:
 * - A mocked instance of an ArtifactsHolder (EAPBaseTest.class)
 * - A mocked logger instance (EAPBaseTest.class)
 * - Mocked repository objects for aether handling. (EAPBaseTest.class)
 * - An EAP base layer with one default module: org.hibernate:main (EAPBaseLayerTest.class)
 * - An EAP static layer with two modules by default: org.drools:1.0 & org.jbpm:1.0 (EAPStaticLayerTest.class)
 * - A DependencyNode mocked instance, with this structure:
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
public abstract class EAPBaseDependencyNodeTest extends EAPStaticLayerTest {

    public static final String COMPILE_SCOPE = "compile";

    @Mock
    protected DependencyNode dependencyNode;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    protected void buildDependencyNodeTree() {
        buildDependencyNodeTree(null);
    }

    protected void buildDependencyNodeTree(EAPDependencyNodeMockInterceptor interceptor) {
        List<DependencyNode> rootChildren = new ArrayList<DependencyNode>(1);

        // Drools resources dependency nodes.
        DependencyNode drools_droolsTemplates_jbpmExectuorNode = createDependencyNodeMock(jbpmExecutorDependency, COMPILE_SCOPE, null);
        DependencyNode drools_droolsTemplates_hibernateCoreNode = createDependencyNodeMock(hibernateCoreDependency, COMPILE_SCOPE, null);
        List<DependencyNode> drools_droolsTemplates_children = new ArrayList<DependencyNode>(2);
        drools_droolsTemplates_children.add(drools_droolsTemplates_jbpmExectuorNode);
        drools_droolsTemplates_children.add(drools_droolsTemplates_hibernateCoreNode);
        if (interceptor != null) interceptor.addChildren(droolsTemplatesDependency, drools_droolsTemplates_children);

        DependencyNode drools_droolsDecisionTables_jbpmJPANode = createDependencyNodeMock(jbpmJPADependency, COMPILE_SCOPE, null);
        List<DependencyNode> drools_droolsDecisionTables_children = new ArrayList<DependencyNode>(1);
        drools_droolsDecisionTables_children.add(drools_droolsDecisionTables_jbpmJPANode);
        if (interceptor != null) interceptor.addChildren(droolsDecisionTablesDependency, drools_droolsDecisionTables_children);

        // jBPM resources dependency nodes.
        DependencyNode jbpm_jbpmExecutor_hibernateCoreNode = createDependencyNodeMock(hibernateCoreDependency, COMPILE_SCOPE, null);
        DependencyNode jbpm_jbpmExecutor_droolsDecisionTablesNode = createDependencyNodeMock(droolsDecisionTablesDependency, COMPILE_SCOPE, null);
        List<DependencyNode> jbpm_jbpmExecutor_children = new ArrayList<DependencyNode>(2);
        jbpm_jbpmExecutor_children.add(jbpm_jbpmExecutor_hibernateCoreNode);
        jbpm_jbpmExecutor_children.add(jbpm_jbpmExecutor_droolsDecisionTablesNode);
        if (interceptor != null) interceptor.addChildren(jbpmExecutorDependency, jbpm_jbpmExecutor_children);

        DependencyNode jbpm_jbpmJPA_droolsTemplatesNode = createDependencyNodeMock(droolsTemplatesDependency, COMPILE_SCOPE, null);
        DependencyNode jbpm_jbpmJPA_hibernateCoreNode = createDependencyNodeMock(hibernateCoreDependency, COMPILE_SCOPE, null);
        List<DependencyNode> jbpm_jbpmJPA_droolsTemplates_children = new ArrayList<DependencyNode>(2);
        jbpm_jbpmJPA_droolsTemplates_children.add(jbpm_jbpmJPA_droolsTemplatesNode);
        jbpm_jbpmJPA_droolsTemplates_children.add(jbpm_jbpmJPA_hibernateCoreNode);
        if (interceptor != null) interceptor.addChildren(jbpmJPADependency,jbpm_jbpmJPA_droolsTemplates_children);

        // Drools module resources nodes.
        DependencyNode drools_droolsTemplatesNode = createDependencyNodeMock(droolsTemplatesDependency, COMPILE_SCOPE, drools_droolsTemplates_children);
        DependencyNode drools_droolsDecisionTablesNode = createDependencyNodeMock(droolsDecisionTablesDependency, COMPILE_SCOPE, drools_droolsDecisionTables_children);
        List<DependencyNode> drools_children = new ArrayList<DependencyNode>(2);
        drools_children.add(drools_droolsTemplatesNode);
        drools_children.add(drools_droolsDecisionTablesNode);

        // jBPM module resources nodes.
        DependencyNode jbpm_jbpmExectuorNode = createDependencyNodeMock(jbpmExecutorDependency, COMPILE_SCOPE, jbpm_jbpmExecutor_children);
        DependencyNode jbpm_JPANode = createDependencyNodeMock(jbpmJPADependency, COMPILE_SCOPE, jbpm_jbpmJPA_droolsTemplates_children);
        List<DependencyNode> jbpm_children = new ArrayList<DependencyNode>(2);
        jbpm_children.add(jbpm_jbpmExectuorNode);
        jbpm_children.add(jbpm_JPANode);

        // Drools module node.
        DependencyNode droolsNode = createDependencyNodeMock(droolsStaticModulePom, COMPILE_SCOPE, drools_children);
        rootChildren.add(droolsNode);

        // jBPM module node.
        DependencyNode jbpmNode = createDependencyNodeMock(jbpmStaticModulePom, COMPILE_SCOPE, jbpm_children);
        rootChildren.add(jbpmNode);

        // Mock the root dependency node.
        when(dependencyNode.getChildren()).thenReturn(rootChildren);
    }

    protected DependencyNode createDependencyNodeMock(Artifact artifact, String scope, List<DependencyNode> children) {
        Dependency nodeDependency = new Dependency(artifact, scope);
        DependencyNode node = mock(DependencyNode.class);
        when(node.getDependency()).thenReturn(nodeDependency);
        when(node.getChildren()).thenReturn(children);

        return node;
    }

    /**
     * Provides a callback to add custom nodes in the dependency tree mocked object at build time.
     */
    protected static interface EAPDependencyNodeMockInterceptor {
        void addChildren(Artifact artifact, List<DependencyNode> children);
    }

}
