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

import org.apache.maven.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.EAPBaseTest;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.mockito.Mock;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tested methods:
 * - createArtifact(String groupId, String artifactId, String version, String packaging)
 * - createArtifact(String groupId, String artifactId, String version, String packaging, String classifier)
 * - equals(Artifact a1, Artifact a2)
 * - equalsNoVersion(Artifact a1, Artifact a2)
 * - extractArtifactCorrdinates(String artifactCoordinates) 
 * - getArtifactCoordinates(org.apache.maven.artifact.Artifact artifact)
 * - getArtifactCoordinatesWithoutVersion(Artifact artifact)
 * - getArtifactCoordinates(Artifact artifact)
 * - toSnaphostVersion(Artifact artifact)
 * - toSnaphostVersion(org.apache.maven.artifact.Artifact artifact)
 * - getPropertyValue(Model model, String propertyValue)
 * - parseFileName(String fileName)
 * - getStaticDependencies(Artifact moduleArtifact, Model moduleModel ,String moduleDependenciesRaw)
 * - getUID(String name, String slot)
 * - cloneArtifact(Artifact a)
 */
public class EAPArtifactUtilsTest extends EAPBaseTest {

    private EAPArtifactUtils tested;

    @Mock
    private Artifact artifact1;

    @Mock
    private Artifact artifact2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Init the artifacts holder tested object.
        tested = new EAPArtifactUtils();
    }

    @Test
    public void testCreateArtifact() throws Exception {
        Artifact result = tested.createArtifact("org.kie", "artifact", "1.0", "jar");
        assertNotNull(result);
        assertEquals(result.getGroupId(), "org.kie");
        assertEquals(result.getArtifactId(), "artifact");
        assertEquals(result.getVersion(), "1.0");
        assertEquals(result.getExtension(), "jar");

        result = tested.createArtifact("org.kie", "artifact", "1.0", "jar", "eap");
        assertNotNull(result);
        assertEquals(result.getGroupId(), "org.kie");
        assertEquals(result.getArtifactId(), "artifact");
        assertEquals(result.getVersion(), "1.0");
        assertEquals(result.getExtension(), "jar");
        assertEquals(result.getClassifier(), "eap");
    }

    @Test
    public void testEuals() throws Exception {
        initMockArtifact(artifact1, "org.kie", "artifact", "1.0", "jar", null);
        initMockArtifact(artifact2, "org.kie", "artifact", "1.0", "jar", null);

        boolean areEquals = tested.equals(artifact1, artifact2);
        assertTrue(areEquals);

        initMockArtifact(artifact2, "org.kie", "artifact", "1.1", "jar", null);
        areEquals = tested.equals(artifact1, artifact2);
        assertTrue(!areEquals);
        
        // Test the no-version equals
        initMockArtifact(artifact1, "org.kie", "artifact", "2.0", "jar", null);
        initMockArtifact(artifact2, "org.kie", "artifact", "1.0", "jar", null);
        areEquals= tested.equalsNoVersion(artifact1, artifact2);
        assertTrue(areEquals);

        initMockArtifact(artifact1, "org.kie", "artifact2", "2.0", "jar", null);
        initMockArtifact(artifact2, "org.kie", "artifact1", "1.0", "jar", null);
        areEquals= tested.equalsNoVersion(artifact1, artifact2);
        assertTrue(!areEquals);
    }

    @Test
    public void testArtifactCoordinates() throws Exception {
        String[] result = tested.extractArtifactCorrdinates("org.kie:artifact:1.0:jar");
        assertNotNull(result);
        assertTrue(result.length == 4);
        assertEquals(result[0], "org.kie");
        assertEquals(result[1], "artifact");
        assertEquals(result[2], "1.0");
        assertEquals(result[3], "jar");

        initMockArtifact(artifact1, "org.kie", "artifact", "1.0", "jar", null);
        String coords = tested.getArtifactCoordinates(artifact1);
        assertNotNull(coords);
        assertEquals(coords, "org.kie:artifact:jar:1.0");

        initMockArtifact(artifact1, "org.kie", "artifact", "1.0", "jar", null);
        coords = tested.getArtifactCoordinatesWithoutVersion(artifact1);
        assertNotNull(coords);
        assertEquals(coords, "org.kie:artifact:jar");

        org.apache.maven.artifact.Artifact mavenArtifact = mock(org.apache.maven.artifact.Artifact.class);
        when(mavenArtifact.getGroupId()).thenReturn("org.kie");
        when(mavenArtifact.getArtifactId()).thenReturn("artifact");
        when(mavenArtifact.getVersion()).thenReturn("1.0");
        when(mavenArtifact.getType()).thenReturn("jar");
        when(mavenArtifact.getClassifier()).thenReturn("eap");
        coords = tested.getArtifactCoordinates(mavenArtifact);
        assertNotNull(coords);
        assertEquals(coords, "org.kie:artifact:jar:eap:1.0");
        
        

    }

    @Test
    public void testArtifactSnapshotVersions() throws Exception {
        initMockArtifact(artifact1, "org.kie", "artifact", "1.0-20140215", "jar", null);
        String version = tested.toSnaphostVersion(artifact1);
        assertNotNull(version);
        assertEquals(version, "1.0-20140215");

        when(artifact1.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        version = tested.toSnaphostVersion(artifact1);
        assertEquals(version, "1.0-SNAPSHOT");

        org.apache.maven.artifact.Artifact mavenArtifact = mock(org.apache.maven.artifact.Artifact.class);
        when(mavenArtifact.getVersion()).thenReturn("1.0-20140215");
        when(mavenArtifact.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        version = tested.toSnaphostVersion(mavenArtifact);
        assertEquals(version, "1.0-SNAPSHOT");
        when(mavenArtifact.getBaseVersion()).thenReturn(null);
        version = tested.toSnaphostVersion(mavenArtifact);
        assertEquals(version, "1.0-20140215");
    }

    @Test
    public void testMavenModelPropertyValueParser() throws Exception {
        Model mavenModel = mock(Model.class);
        Properties modeProperties = new Properties();
        modeProperties.put("project.name","eap-modules");
        when(mavenModel.getProperties()).thenReturn(modeProperties);

        String result = tested.getPropertyValue(mavenModel, "org-kie-${project.name}");
        assertNotNull(result);
        assertEquals(result, "org-kie-eap-modules");

    }

    @Test
    public void testMavenModelPropertyValueCustomPropsParser() throws Exception {
        Model mavenModel = mock(Model.class);
        Properties modeProperties = new Properties();
        modeProperties.put("drools.name","${project.name}");
        modeProperties.put("project.name","drools");
        when(mavenModel.getProperties()).thenReturn(modeProperties);

        String result = tested.getPropertyValue(mavenModel, "org-kie:${drools.name}");
        assertNotNull(result);
        assertEquals(result, "org-kie:drools");

    }

    @Test
    public void testParseFileName() throws Exception {
        String[] result = tested.parseFileName("slf4j-jboss-logmanager-1.0.2.GA-redhat-1.jar");
        assertNotNull(result);
        assertEquals(result[0], "slf4j-jboss-logmanager");
        assertEquals(result[1], "1.0.2.GA-redhat-1");
    }

    @Test
    public void testGetStaticDependencies() throws Exception {
        Model mavenModel = mock(Model.class);
        Properties modeProperties = new Properties();
        when(mavenModel.getProperties()).thenReturn(modeProperties);
        Collection<EAPStaticModuleDependency> result = tested.getStaticDependencies(null, mavenModel, "org.jbpm:main,org.drools:main");

        assertNotNull(result);
        assertTrue(result.size() == 2);
        EAPStaticModuleDependency dep1 = (EAPStaticModuleDependency) result.toArray()[0];
        assertEquals("org.jbpm", dep1.getName());
        assertEquals("main", dep1.getSlot());
        EAPStaticModuleDependency dep2 = (EAPStaticModuleDependency) result.toArray()[1];
        assertEquals("org.drools", dep2.getName());
        assertEquals("main", dep2.getSlot());
    }

    @Test
    public void testGetUID() throws Exception {
        String uid = tested.getUID("org.drools","main");
        assertEquals(uid, "org.drools:main");
        uid = tested.getUID("org.jbpm","6.1.0");
        assertEquals(uid, "org.jbpm:6.1.0");
    }

    @Test
    public void testCloneArtifact() throws Exception {
        initMockArtifact(artifact1, "org.kie", "artifact", "1.0", "jar", null);
        
        Artifact result = tested.cloneArtifact(artifact1);
        assertNotNull(result);
        assertEquals(result.getGroupId(), "org.kie");
        assertEquals(result.getArtifactId(), "artifact");
        assertEquals(result.getVersion(), "1.0");
        assertEquals(result.getExtension(), "jar");
    }

    @After
    public void tearDown() throws Exception {

    }
}
