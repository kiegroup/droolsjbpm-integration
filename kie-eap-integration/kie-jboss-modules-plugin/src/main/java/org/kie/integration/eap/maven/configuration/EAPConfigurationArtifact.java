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
package org.kie.integration.eap.maven.configuration;

import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a configuration artifact for the Maven plugin..
 *
 */
public class EAPConfigurationArtifact
{
    /**
     * Group Id of Artifact
     *
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Name of Artifact
     *
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * Version of Artifact
     *
     * @parameter
     */
    private String version = null;

    /**
     * Type of Artifact (War,Jar,etc)
     *
     * @parameter
     * @required
     */
    private String type = "jar";

    /**
     * Classifier of Artifact
     *
     * @parameter
     * @required
     */
    private String classifier =null;

    /**
     * The exclusions list
     * @parameter
    **/
    private List<EAPConfigurationArtifact> exclusions;

    public EAPConfigurationArtifact() {

    }

    public Artifact getArtifact() {
        return EAPArtifactUtils.createArtifact(groupId, artifactId, version, type, classifier);
    }

    public Collection<Artifact> getExclusionArtifacts() {
        Collection<Artifact> result = null;

        if (exclusions != null && !exclusions.isEmpty()) {
            result = new LinkedList<Artifact>();
            for (EAPConfigurationArtifact exclusion : exclusions) {
                Artifact artifact = EAPArtifactUtils.createArtifact(exclusion.groupId, exclusion.artifactId, exclusion.version, exclusion.type, exclusion.classifier);
                result.add(artifact);
            }
        }

        return result;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public List<EAPConfigurationArtifact> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<EAPConfigurationArtifact> excludes) {
        this.exclusions = excludes;
    }
}
