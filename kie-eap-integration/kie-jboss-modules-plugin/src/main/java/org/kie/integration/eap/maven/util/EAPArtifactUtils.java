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

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.transformer.NoopDependencyGraphTransformer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains some artifact utils.
 */
public class EAPArtifactUtils {

    /** The pattern for maven propoerties. **/
    protected static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

    protected static final Pattern JAR_NAME_PARSER_PATTERN = Pattern.compile("(.*)-(\\d+[\\.-].*).jar");

    /**
     * Creates an artifact instance.
     *
     * @param groupId The artifact's groupId.
     * @param artifactId The artifact's artifactId.
     * @param version The artifact's version.
     * @param packaging The artifact's packaging.
     * @return The artifact instance.
     */
    public static Artifact createArtifact(String groupId, String artifactId, String version, String packaging) {
        return new DefaultArtifact(groupId, artifactId, packaging, version);
    }

    /**
     * Creates an artifact instance.
     *
     * @param artifactCoordinates The artifact's coordinates..
     * @return The artifact instance.
     */
    public static Artifact createArtifact(String artifactCoordinates) {
        String[] coords = extractArtifactCorrdinates(artifactCoordinates);

        String groupId = coords[0];
        String artifactId = coords[1];
        String type = coords.length > 2 ? coords[2] : "";
        String version = coords.length > 3 ? coords[3] : "";
        
        return new DefaultArtifact(groupId, artifactId, type, version);
    }

    /**
     * Creates an artifact instance.
     *
     * @param groupId The artifact's groupId.
     * @param artifactId The artifact's artifactId.
     * @param version The artifact's version.
     * @param packaging The artifact's packaging.
     * @param classifier The artifact's classifier.
     * @return The artifact instance.
     */
    public static Artifact createArtifact(String groupId, String artifactId, String version, String packaging, String classifier) {
        return new DefaultArtifact(groupId, artifactId, classifier, packaging, version);
    }



    /**
     * Resolves an artifact in remote repositories.
     *
     * @param groupId The artifact's groupId.
     * @param artifactId The artifact's artifactId.
     * @param version The artifact's version.
     * @param packaging The artifact's packaging.
     * @param classifier The artifact's classifier..
     * @return The artifact resolved.
     *
     * @throws org.eclipse.aether.resolution.ArtifactResolutionException
     */
    public static Artifact resolveArtifact(String groupId, String artifactId, String version, String packaging, String classifier, RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) throws ArtifactResolutionException {
        Artifact art = createArtifact(groupId, artifactId, version, packaging, classifier);
        return resolveArtifact(art, repoSystem, repoSession, remoteRepos);
    }

    /**
     * Resolves an artifact in remote repositories.
     *
     * @param groupId The artifact's groupId.
     * @param artifactId The artifact's artifactId.
     * @param version The artifact's version.
     * @param packaging The artifact's packaging..
     * @return The artifact resolved.
     *
     * @throws org.eclipse.aether.resolution.ArtifactResolutionException
     */
    public static Artifact resolveArtifact(String groupId, String artifactId, String version, String packaging, RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) throws ArtifactResolutionException {
        Artifact art = createArtifact(groupId, artifactId, version, packaging);
        return resolveArtifact(art, repoSystem, repoSession, remoteRepos);
    }

    /**
     * Resolves an artifact in remote repositories.
     *
     * @param art The artifact to resolve.
     * @return The artifact resolved.
     *
     * @throws ArtifactResolutionException
     */
    public static Artifact resolveArtifact(Artifact art, RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest(art, remoteRepos, "");
        return repoSystem.resolveArtifact(repoSession, request).getArtifact();
    }

    /**
     * Check if two artifacts are equals.
     * The equality is done by comparing artifact coordinates.
     *
     * @param a1 The artifact.
     * @param a2 The other artifact.
     * @return Are artifact coordinates equals.
     */
    public static boolean equals(Artifact a1, Artifact a2) {
        String a1Coords = getArtifactCoordinates(a1);
        String a2Coords = getArtifactCoordinates(a2);
        return a1Coords.equalsIgnoreCase(a2Coords);
    }

    /**
     * Check if two artifacts are equals except for the version coordinate.
     * The equality is done by comparing artifact coordinates.
     *
     * @param a1 The artifact.
     * @param a2 The other artifact.
     * @return Are artifact coordinates equals (except for version coordinate).
     */
    public static boolean equalsNoVersion(Artifact a1, Artifact a2) {
        Artifact a1Copy = new DefaultArtifact(a1.getGroupId(), a1.getArtifactId(), a1.getExtension(), null);
        Artifact a2Copy = new DefaultArtifact(a2.getGroupId(), a2.getArtifactId(), a2.getExtension(), null);
        String a1Coords = getArtifactCoordinates(a1Copy);
        String a2Coords = getArtifactCoordinates(a2Copy);
        return a1Coords.equalsIgnoreCase(a2Coords);
    }

    /**
     * Clones an artifact instance.
     * Only clones the coordinates:
     * - groupId
     * - artifactId
     * - version
     * - type
     * @param a The artifact to clone.
     * @return The cloned artifact.
     */
    public static Artifact cloneArtifact(Artifact a) {
        String groupId = a.getGroupId();
        String artifactId = a.getArtifactId();
        String type = a.getExtension();
        String version = a.getVersion();

        return new DefaultArtifact(groupId, artifactId, type, version);
    }

    /**
     * Extract the artifact properties for a given artifact string.
     * TODO: Use maven API?
     *
     * @param artifactCoordinates The artifact string.
     * @return The artifact properties.
     */
    public static String[] extractArtifactCorrdinates(String artifactCoordinates) {
        if (artifactCoordinates == null || artifactCoordinates.trim().length() == 0) throw  new IllegalArgumentException("Artifact string cannot be null or empty.");

        return artifactCoordinates.split(EAPConstants.ARTIFACT_SEPARATOR);
    }


    /**
     * Returns the artifact with all coordinates - groupId:artifactId:type[:classifier]:version
     * TODO: Use maven API?
     *
     * @param artifact The artifact.
     * @return The artifact coordinates.
     */
    public static String getArtifactCoordinates(org.apache.maven.artifact.Artifact artifact) {
        if (artifact == null) return  null;
        StringBuilder result = new StringBuilder();

        if (artifact.getGroupId() != null && artifact.getGroupId().trim().length() > 0) {
            result.append(artifact.getGroupId());
        }

        if (artifact.getArtifactId() != null && artifact.getArtifactId().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getArtifactId());
        }

        if (artifact.getType() != null && artifact.getType().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getType());
        }

        if (artifact.getClassifier() != null && artifact.getClassifier().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getClassifier());
        }

        String version = toSnaphostVersion(artifact);
        if (version != null) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(version);
        }

        return result.toString();
    }

    /**
     * Returns the artifact with all coordinates - groupId:artifactId:type[:classifier]:version
     * TODO: Use maven API?
     *
     * @param artifact The artifact.
     * @return The artifact coordinates.
     */
    public static String getArtifactCoordinates(Artifact artifact) {
        if (artifact == null) return  null;
        StringBuilder result = new StringBuilder();

        if (artifact.getGroupId() != null && artifact.getGroupId().trim().length() > 0) {
            result.append(artifact.getGroupId());
        }

        if (artifact.getArtifactId() != null && artifact.getArtifactId().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getArtifactId());
        }

        if (artifact.getExtension() != null && artifact.getExtension().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getExtension());
        }

        if (artifact.getClassifier() != null && artifact.getClassifier().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getClassifier());
        }

        String version = toSnaphostVersion(artifact);
        if (version != null) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(version);
        }

        return result.toString();
    }

    /**
     * Returns the artifact with all coordinates exception the version one - groupId:artifactId:type[:classifier]
     *
     * @param artifact The artifact.
     * @return The artifact coordinates without the version one.
     */
    public static String getArtifactCoordinatesWithoutVersion(Artifact artifact) {
        if (artifact == null) return  null;
        StringBuilder result = new StringBuilder();

        if (artifact.getGroupId() != null && artifact.getGroupId().trim().length() > 0) {
            result.append(artifact.getGroupId());
        }

        if (artifact.getArtifactId() != null && artifact.getArtifactId().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getArtifactId());
        }

        if (artifact.getExtension() != null && artifact.getExtension().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getExtension());
        }

        if (artifact.getClassifier() != null && artifact.getClassifier().trim().length() > 0) {
            if (result.length() > 0) result.append(EAPConstants.ARTIFACT_SEPARATOR);
            result.append(artifact.getClassifier());
        }

        return result.toString();
    }

    public static String toSnaphostVersion(Artifact artifact) {
        if (artifact == null) return null;

        if (artifact.getBaseVersion() != null && artifact.getBaseVersion().trim().length() > 0) {
            // Avoid snapshot timestamped versions.
            return artifact.getBaseVersion();
        } else if (artifact.getVersion() != null && artifact.getVersion().trim().length() > 0) {
            return artifact.getVersion();
        }

        return null;
    }

    public static String toSnaphostVersion(org.apache.maven.artifact.Artifact artifact) {
        if (artifact == null) return null;

        // TODO: Use ArtifactUtils.toSnapshotVersion()?
        if (artifact.getBaseVersion() != null && artifact.getBaseVersion().trim().length() > 0) {
            // Avoid snapshot timestamped versions.
            return artifact.getBaseVersion();
        } else if (artifact.getVersion() != null && artifact.getVersion().trim().length() > 0) {
            return artifact.getVersion();
        }

        return null;
    }

    /**
     * Returns the artifact with gropupId and artifactId coordinates - groupId:artifactId
     * TODO: Use maven API?
     *
     * @param artifactCoords The artifact coordinates..
     * @return The artifact short coordinates.
     */
    public static String getArtifactShortCoordinates(String artifactCoords) {
        if (artifactCoords == null) return  null;

        String[] coords = extractArtifactCorrdinates(artifactCoords);

        return new StringBuilder().append(coords[0]).append(EAPConstants.ARTIFACT_SEPARATOR).append(coords[1]).toString();
    }

    /**
     * Generates a model for a given artifact.
     * @param artifact The artifact.
     * @return The model for this artifact's pom file.
     *
     * @throws java.io.IOException Pom file cannot be readed.
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException Pom file cannot be parsed.
     */
    public static Model generateModel(Artifact artifact) throws IOException, XmlPullParserException {
        File pomFile = artifact.getFile();
        Reader reader = new FileReader(pomFile);
        try {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            return model;
        } finally {
            reader.close();
        }
    }

    /**
     * Obtain the property value when using a property as a value.
     * TODO: Use Maven API?
     *
     * @param model
     * @param propertyValue
     * @return
     */
    public static String getPropertyValue(Model model, String propertyValue) {
        if (model == null && propertyValue != null) return propertyValue;
        if (model == null || propertyValue == null) return null;
        StringBuffer sb = new StringBuffer(propertyValue.length());

        // Check if the property value is a variable.
        Matcher m = PROPERTY_PATTERN.matcher(propertyValue);
        while (m.find()) {
            String pName = m.group(1);
            String pValue = null;
            if (EAPConstants.PROPERTY_PROJECT_VERSION.equalsIgnoreCase(pName)) pValue = model.getParent().getVersion();
            else pValue = (String) model.getProperties().get(pName);
            
            if (pValue == null) throw new IllegalArgumentException("Cannot resolve the property " + pName + " in project properties.");

            // Custom pom properties.
            if (PROPERTY_PATTERN.matcher(pValue).matches()) pValue = getPropertyValue(model, pValue);
                
            m.appendReplacement(sb, Matcher.quoteReplacement(pValue));
        }
        m.appendTail(sb);
        return sb.toString();
    }


    public static void toArtifacts( EAPArtifactsHolder holder,
                                    Collection<? extends DependencyNode> nodes,
                                    DependencyFilter filter )
    {
        // Setting depth as 0 means that it will scan the whole tree.
        toArtifacts(holder, nodes, filter, 0);
    }

    public static void toArtifacts( EAPArtifactsHolder holder,
                                    Collection<? extends DependencyNode> nodes,
                                    DependencyFilter filter, int depth )
    {
        if (nodes == null || nodes.isEmpty()) return;
        int currentDepth = 0;

        toArtifacts(holder, nodes, filter, depth, currentDepth);

    }

    public static void toArtifacts( EAPArtifactsHolder holder,
                                    Collection<? extends DependencyNode> nodes,
                                    DependencyFilter filter, int depth, int currentDepth )
    {
        if (depth > 0 && depth >= currentDepth) return;
        if (nodes == null || nodes.isEmpty()) return;


        for ( DependencyNode node : nodes )
        {
            // Artifact artifact = toArtifact( node.getDependency() );
            Artifact artifact  = node.getDependency().getArtifact();

            if (holder.contains(artifact)) continue;

            if ( filter == null || filter.accept( node, Collections.<DependencyNode> emptyList() ) )
            {
                // System.out.println("Adding artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId());
                holder.add( artifact );
            }

            currentDepth++;
            toArtifacts( holder, node.getChildren(), filter, depth, currentDepth );
        }
    }

    public static Artifact toArtifact( org.eclipse.aether.graph.Dependency dependency )
    {
        if ( dependency == null )
        {
            return null;
        }

        String artifactId = dependency.getArtifact().getArtifactId();
        String groupId = dependency.getArtifact().getGroupId();
        String version = dependency.getArtifact().getVersion();
        String classifier = dependency.getArtifact().getClassifier();
        String packaging = dependency.getArtifact().getExtension();

        if (classifier != null && classifier.trim().length() > 0) return createArtifact(groupId, artifactId, version, packaging, classifier);
        else return createArtifact(groupId, artifactId, version, packaging);
    }

    /**
     * Generates the dependency graph for an artifact.
     *
     * @param rootArtifact The artifact to generate the dependency graph.
     * @return The artifact dependency graph.
     *
     * @throws org.eclipse.aether.collection.DependencyCollectionException
     */
    public static DependencyNode getDependencyGraph(Artifact rootArtifact, RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos, boolean includeOptionalDependencies)
            throws DependencyCollectionException, DependencyResolutionException {
        Dependency rootDependency = new Dependency(rootArtifact, EAPConstants.SCOPE_COMPILE);
        CollectRequest collectRequest = new CollectRequest(rootDependency, remoteRepos);

        // Collect all the nodes using a NoopDependencyGraphTransformer transformer to have the whole unmanaged tree.
        DefaultRepositorySystemSession newSession = new DefaultRepositorySystemSession(repoSession);
        // Do not transform the tree. We don't any any dependency resolution applied.
        newSession.setDependencyGraphTransformer(NoopDependencyGraphTransformer.INSTANCE);
        // Exclude only test scope dependencies, include other ones (provided).
        ScopeDependencySelector scopeDependencySelector = new ScopeDependencySelector(EAPConstants.SCOPE_TEST);

        // Create the dependency selector.
        DependencySelector selector = null;
        if (includeOptionalDependencies) selector = new AndDependencySelector(scopeDependencySelector, new ExclusionDependencySelector());
        else selector = new AndDependencySelector(scopeDependencySelector, new ExclusionDependencySelector(), new OptionalDependencySelector());
        newSession.setDependencySelector(selector);

        // DependencyRequest depRequest = new DependencyRequest(collectRequest, null);
        CollectResult result = repoSystem.collectDependencies(newSession, collectRequest);

        return result.getRoot();
    }

    public static Artifact createProjectArtifact(MavenProject project) {
        return EAPArtifactUtils.createArtifact(project.getGroupId(),
                project.getArtifactId(), project.getVersion(),
                project.getPackaging());
    }
    
    public static String getUID(String name, String slot) {
        if (name == null) return null;
        StringBuilder result = new StringBuilder(name);
        if (slot != null && slot.trim().length() > 0) {
            result.append(EAPConstants.ARTIFACT_SEPARATOR).append(slot);
        }
        
        return result.toString();
    }


    public static EAPModuleGraphNode getNodeWithResource(Artifact artifact, EAPModulesGraph graph) {
        if (artifact != null && graph != null) {
            String artifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(artifact);
            Collection<EAPModuleGraphNode> nodes = graph.getNodes();
            if (nodes != null && !nodes.isEmpty()) {
                for (EAPModuleGraphNode node : nodes) {
                    Collection<EAPModuleGraphNodeResource> resources = node.getResources();
                    if (resources != null && !resources.isEmpty()) {
                        for (EAPModuleGraphNodeResource resource : resources) {
                            String resourceCoordinates = EAPArtifactUtils.getArtifactCoordinates((Artifact) resource.getResource());
                            if (artifactCoordinates.equalsIgnoreCase(resourceCoordinates)) return node;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses a jar resource name.
     * Extract artifactId and version coordinates.
     *
     * @param fileName the jar file name.
     * @return
     */
    public static String[] parseFileName(String fileName) {
        String[] result = new String[2];

        Matcher m1 = JAR_NAME_PARSER_PATTERN.matcher(fileName);
        boolean matches = m1.matches();

        if (!matches) {
            result[0] = fileName.substring(0,fileName.length() - 4);
            result[1] = "";
        } else {
            result[0] = m1.group(1);
            result[1] = m1.group(2);
        }

        return result;
    }

    public static boolean isArtifactExcludedInModule(EAPModule module, Artifact artifact) {
        if (module == null || artifact == null) return false;

        Collection<EAPModuleResource> resources = module.getResources();
        if (resources != null && !resources.isEmpty()) {
            for (EAPModuleResource resource : resources) {
                Collection<Exclusion> exclusions = resource.getExclusions();
                if (exclusions != null && !exclusions.isEmpty()) {
                    for (Exclusion exclusion : exclusions) {
                        // TODO: Check classfifer and type too for exclusion artifacts?.
                        if (exclusion.getGroupId().equalsIgnoreCase(artifact.getGroupId()) && exclusion.getArtifactId().equalsIgnoreCase(artifact.getArtifactId())) return true;
                    }
                }
            }
        }

        return false;
    }
    
    public static Collection<EAPStaticModuleDependency> getStaticDependencies(Artifact moduleArtifact, Model moduleModel ,String moduleDependenciesRaw) throws EAPModuleDefinitionException {
        Collection<EAPStaticModuleDependency> result = null;
        if (moduleDependenciesRaw != null && moduleDependenciesRaw.trim().length() > 0) {
            String moduleArtifactCoordinates = getArtifactCoordinates(moduleArtifact);
            result = new LinkedList<EAPStaticModuleDependency>();
            
            // If module pom descriptor file contains dependencies, add these ones.
            String[] _moduleDependenciesRaw = moduleDependenciesRaw.split(",");
            for (String moduleDep : _moduleDependenciesRaw) {
                String[] _moduleDep = moduleDep.split(":");
                if (_moduleDep == null || _moduleDep.length < 2)
                    throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The static dependency '" + _moduleDep + "' syntax is not correct.");
                String moduleName = EAPArtifactUtils.getPropertyValue(moduleModel, _moduleDep[0]);
                String moduleSlot = EAPArtifactUtils.getPropertyValue(moduleModel, _moduleDep[1]);
                boolean export = false;
                if (_moduleDep.length == 3) export = Boolean.valueOf(_moduleDep[2]);
                EAPStaticModuleDependency dep = new EAPStaticModuleDependency(moduleName);
                dep.setSlot(moduleSlot);
                dep.setOptional(false);
                dep.setExport(export);
                result.add(dep);
            }
        }
        
        return result;
    }

    public static boolean isVersionEqualsThan(ComparableVersion version1, ComparableVersion version2) {
        return version1.compareTo(version2) == 0;
    }
    
    public static boolean isVersionGreaterThan(ComparableVersion version1, ComparableVersion version2) {
        return version1.compareTo(version2) == 1;
    }

    public static boolean isVersionLowerThan(ComparableVersion version1, ComparableVersion version2) {
        return version1.compareTo(version2) == -1;
    }
}
