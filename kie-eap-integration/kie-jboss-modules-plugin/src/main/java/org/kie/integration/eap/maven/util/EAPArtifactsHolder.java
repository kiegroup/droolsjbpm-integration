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

import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.util.*;

/**
 * This class holds all the module artifacts that the Maven plugin handles when generating a distribution.
 * Provides:
 * - All base / static module artifacts and the module model instance that contains each one.
 * - Artifact resolution based on current maven project repositories.
 * - Artifact duplication validation (an artifact only can be present in a single module)
 */
public class EAPArtifactsHolder {

    /**
     * Contains the relation: <artifact_Coordinates> <-> [ <module> <artifact> ]
     */
    private Map<String, Object[]> artifactAllCoordinatesMap = new HashMap<String, Object[]>();
    /**
     * Contains the relation: <module> <-> <mapped_artifact_coordinates>
     */
    private Map<String, String> artifactCoordinatesMapping = new HashMap<String, String>();

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    protected RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    protected RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     */
    protected List<RemoteRepository> remoteRepos;

    public EAPArtifactsHolder(RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
    }

    public void add(Artifact obj)  {
        String allCoords = EAPArtifactUtils.getArtifactCoordinates(obj);
        Object[] value = new Object[] {null, obj};
        artifactAllCoordinatesMap.put(allCoords, value);
    }

    public void add(Artifact obj, EAPModule module) throws EAPModuleResourceDuplicationException {
        String allCoords = EAPArtifactUtils.getArtifactCoordinates(obj);
        Object[] allCordsObj = artifactAllCoordinatesMap.get(allCoords);
        if (allCordsObj != null && allCordsObj[0] != null && !module.getName().equals(((EAPModule)allCordsObj[0]).getName())) throw new EAPModuleResourceDuplicationException("The artifact has been already added in module " + artifactAllCoordinatesMap.get(allCoords)[0], allCoords);
        Object[] value = new Object[] {module, obj};
        artifactAllCoordinatesMap.put(allCoords, value);

        applyArtifactMapping(allCoords, module);
    }

    public void setModule(String artifactCoordinates, EAPModule module) throws EAPModuleResourceDuplicationException {
        Object[] obj = get(artifactCoordinates);

        if (obj[0] != null) {
            EAPModule m = (EAPModule) obj[0];
            if (!module.getName().equalsIgnoreCase(m.getName())) throw new EAPModuleResourceDuplicationException("The artifact has been already added in module " + obj[0], artifactCoordinates);
        }
        obj[0] = module;

        applyArtifactMapping(artifactCoordinates, module);
    }

    protected void applyArtifactMapping(String artifactCoordinates, EAPModule module) {
        // Check other artifacts with different version or packaging coordinate.
        String shortCoords = EAPArtifactUtils.getArtifactShortCoordinates(artifactCoordinates);
        for (Map.Entry<String, Object[]> entry : artifactAllCoordinatesMap.entrySet()) {
            String entryKey = entry.getKey();
            Object[] entryValue = entry.getValue();

            if (entryKey != null && entryKey.startsWith(shortCoords) && !entryKey.equalsIgnoreCase(artifactCoordinates)) {
                entryValue[0] = module;
                artifactCoordinatesMapping.put(module.getName(), artifactCoordinates);
            }
        }
    }

    public void setModule(Artifact artifact, EAPModule module) throws EAPModuleResourceDuplicationException {
        setModule(EAPArtifactUtils.getArtifactCoordinates(artifact), module);
    }


    public Artifact getArtifact(String artifactCoordinates) {
        if (artifactCoordinates == null) return null;

        Object[] result = get(artifactCoordinates);

        if (result == null) return null;

        return (Artifact) result[1];
    }

    public EAPModule getModule(String artifactCoordinates) {
        if (artifactCoordinates == null) return null;

        Object[] result = get(artifactCoordinates);

        if (result == null) return null;

        return (EAPModule) result[0];
    }

    public Artifact getArtifact(Artifact artifact) {
        return getArtifact(EAPArtifactUtils.getArtifactCoordinates(artifact));
    }

    public EAPModule getModule(Artifact artifact) {
        return getModule(EAPArtifactUtils.getArtifactCoordinates(artifact));
    }

    public boolean contains(Artifact artifact) {
        return getArtifact(artifact) != null;
    }

    protected Object[] get(String artifactCoordinates) {
        if (artifactCoordinates == null) return null;

        Object[] result = artifactAllCoordinatesMap.get(artifactCoordinates);

        return result;
    }

    public Map<String, String> getMappedCoordinates() {
        return Collections.unmodifiableMap(artifactCoordinatesMapping);
    }

    public Artifact resolveArtifact(Artifact artifact) throws ArtifactResolutionException {
        return EAPArtifactUtils.resolveArtifact(artifact, repoSystem, repoSession, remoteRepos);
    }

    public Artifact resolveArtifact(String groupId, String artifactId, String version, String packaging) throws ArtifactResolutionException {
        return EAPArtifactUtils.resolveArtifact(groupId, artifactId, version, packaging, repoSystem, repoSession, remoteRepos);
    }

    public Collection<Artifact> getArtifacts() {
        Collection<Artifact> result = new LinkedList<Artifact>();

        for (Map.Entry<String, Object[]> entry : artifactAllCoordinatesMap.entrySet()) {
            Object[] entryValue = entry.getValue();
            Artifact artifact = (Artifact) entryValue[1];
            if (artifact != null) result.add(artifact);
        }

        return result;
    }
    
    public Artifact contains(String groupId, String artifactId, String type) {
        if (artifactId == null || artifactId.trim().length() == 0 ||
                type == null || type.trim().length() == 0 ||
                groupId == null || groupId.trim().length() == 0) return null;
        
        for (Map.Entry<String, Object[]> entry : artifactAllCoordinatesMap.entrySet()) {
            Object[] entryValue = entry.getValue();
            Artifact artifact = (Artifact) entryValue[1];
            if (groupId.equalsIgnoreCase(artifact.getGroupId()) && artifactId.equalsIgnoreCase(artifact.getArtifactId())
                    && type.equalsIgnoreCase(artifact.getExtension())) return artifact;
        }
        
        return null;
    }
}
