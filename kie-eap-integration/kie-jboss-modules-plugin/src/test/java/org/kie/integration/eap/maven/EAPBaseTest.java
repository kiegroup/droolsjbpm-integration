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

import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Before;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * This base test provides:
 * - A mocked instance of an ArtifactsHolder
 * - A mocked logger instance
 * - Mocked repository objects for aether handling.
 */
public abstract class EAPBaseTest {

    protected EAPArtifactsHolder artifactsHolder;

    @Mock
    protected Log logger;

    @Mock
    protected org.eclipse.aether.RepositorySystem repoSystem;

    @Mock
    protected RepositorySystemSession repoSession;

    @Mock
    protected List<RemoteRepository> remoteRepos;

    /**
     * Custom  matcher for artifacts.
     * It uses custom EAPArtifactUtils#equals method to compare Artifact instances.
     */
    public static class EAPArtifactMatcher extends ArgumentMatcher<Artifact> {

        private Artifact artifact;

        public EAPArtifactMatcher(Artifact artifact) {
            this.artifact = artifact;
        }

        @Override
        public boolean matches(Object argument) {
            return EAPArtifactUtils.equals((Artifact) argument, artifact);
        }
    }

    @Before
    public void setUp() throws Exception {
        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);

        // Init the artifacts holder mock.
        EAPArtifactsHolder artifactsHolder = new EAPArtifactsHolder(repoSystem, repoSession, remoteRepos);
        this.artifactsHolder = spy(artifactsHolder);
        doNothing().when(this.artifactsHolder).setModule(any(Artifact.class), any(EAPModule.class));

        // Init the logger mock.
        doNothing().when(logger).debug(anyString());
        doNothing().when(logger).debug(anyString(), any(Throwable.class));
        doNothing().when(logger).debug(any(Throwable.class));
        doNothing().when(logger).info(anyString());
        doNothing().when(logger).info(anyString(), any(Throwable.class));
        doNothing().when(logger).info(any(Throwable.class));
        doNothing().when(logger).warn(anyString());
        doNothing().when(logger).warn(anyString(), any(Throwable.class));
        doNothing().when(logger).warn(any(Throwable.class));
        doNothing().when(logger).error(anyString());
        doNothing().when(logger).error(anyString(), any(Throwable.class));
        doNothing().when(logger).error(any(Throwable.class));

    }

    /**
     * *****************************************************************************************************
     * Mock objects helper methods.
     * *****************************************************************************************************
     */

    protected void initMockArtifact(Artifact artifact, String groupId, String artifactId, String version, String type, String classifier) {
        Assert.assertTrue(artifact != null);
        Assert.assertTrue(groupId != null);
        Assert.assertTrue(artifactId != null);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getVersion()).thenReturn(version != null ? version : "");
        when(artifact.getExtension()).thenReturn(type != null ? type : "");
        when(artifact.getClassifier()).thenReturn(classifier != null ? classifier : "");
    }

    protected void initMockArtifact(Artifact pomArtifact, String groupId, String artifactId, String version, String type, String classifier, String fileUri) throws Exception {
        initMockArtifact(pomArtifact, groupId, artifactId, version, type, classifier);
        URL droolsStaticModulePomFileUrl = getClass().getResource(fileUri);
        File droolsStaticModulePomFile = new File(droolsStaticModulePomFileUrl.toURI());
        when(pomArtifact.getFile()).thenReturn(droolsStaticModulePomFile);
    }

    protected void initMockModule(EAPModule module, String moduleName, String moduleSlot, Artifact modulePomArtifact) {
        Assert.assertTrue(module != null);
        Assert.assertTrue(moduleName != null);
        when(module.getName()).thenReturn(moduleName);
        when(module.getSlot()).thenReturn(moduleSlot != null ? moduleSlot : "main");
        when(module.getUniqueId()).thenReturn(moduleSlot != null ? moduleName + ":"  + moduleSlot : moduleName + ":main");
        when(module.getArtifact()).thenReturn(modulePomArtifact);
    }

    protected void initMockModule(EAPModule module, String moduleName, String moduleSlot, Artifact modulePomArtifact, Collection<EAPModuleResource> resources) {
        initMockModule(module, moduleName, moduleSlot, modulePomArtifact);
        when(module.getResources()).thenReturn(resources);
    }

    protected void initMockModuleResource(EAPModuleResource resource, String resourceName, String resourceFileName, Artifact resourceArtifact) {
        Assert.assertTrue(resource != null);
        when(resource.getName()).thenReturn(resourceName);
        when(resource.getFileName()).thenReturn(resourceFileName);
        when(resource.getResource()).thenReturn(resourceArtifact != null);
        when(resource.getExclusions()).thenReturn(null);
    }

    protected void initMockLayer(EAPLayer layer, String layerName, Collection<EAPModule> modules) {
        Assert.assertTrue(layer != null);
        Assert.assertTrue(layerName != null);
        when(layer.getName()).thenReturn(layerName);
        when(layer.getProperties()).thenReturn(null);
        if (modules != null && !modules.isEmpty()) {
            when(layer.getModules()).thenReturn(modules);
            for (EAPModule module : modules) {
                Artifact artifact = module.getArtifact();
                String uniqueId = module.getUniqueId();
                when(layer.getModule(artifact)).thenReturn(module);
                when(layer.getModule(uniqueId)).thenReturn(module);
            }
        }
    }

    /**
     * *****************************************************************************************************
     * EAP Artifacts Holder helper methods.
     * *****************************************************************************************************
     */

    protected void addArtifactIntoHolder(Artifact artifact) throws Exception {
        doReturn(artifact).when(artifactsHolder).getArtifact(argThat(new EAPArtifactMatcher(artifact)));
        doReturn(artifact).when(artifactsHolder).resolveArtifact(argThat(new EAPArtifactMatcher(artifact)));
    }

    protected void addArtifactIntoHolder(Artifact artifact, EAPModule module) throws Exception {
        addArtifactIntoHolder(artifact);
        doReturn(module).when(artifactsHolder).getModule(argThat(new EAPArtifactMatcher(artifact)));
    }

    protected void cleanArtifactsInHolder() throws Exception {
        doReturn(null).when(artifactsHolder).getArtifact(any(Artifact.class));
        doReturn(null).when(artifactsHolder).resolveArtifact(any(Artifact.class));
        doReturn(null).when(artifactsHolder).getModule(any(Artifact.class));
    }

}
