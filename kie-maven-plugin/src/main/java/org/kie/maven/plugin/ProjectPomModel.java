/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.maven.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.AFReleaseIdImpl;
import org.appformer.maven.support.DependencyFilter;
import org.appformer.maven.support.PomModel;

public class ProjectPomModel implements PomModel {

    private final AFReleaseId releaseId;
    private final AFReleaseId parentReleaseId;
    private final Map<String, Set<AFReleaseId>> dependenciesByScope;

    public ProjectPomModel(final MavenSession mavenSession) {
        this.releaseId = getReleaseIdFromMavenProject(mavenSession.getCurrentProject());
        final MavenProject parentProject = mavenSession.getCurrentProject().getParent();
        if (parentProject != null) {
            this.parentReleaseId = getReleaseIdFromMavenProject(parentProject);
        } else {
            this.parentReleaseId = null;
        }
        this.dependenciesByScope = getDirectDependenciesFromMavenSession(mavenSession);
    }

    @Override
    public AFReleaseId getReleaseId() {
        return releaseId;
    }

    @Override
    public AFReleaseId getParentReleaseId() {
        return parentReleaseId;
    }

    @Override
    public Collection<AFReleaseId> getDependencies() {
        return dependenciesByScope.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AFReleaseId> getDependencies(final DependencyFilter filter) {
        final Set<AFReleaseId> filteredDependencies = new HashSet<>();
        for (Map.Entry<String, Set<AFReleaseId>> entry : dependenciesByScope.entrySet()) {
            for (AFReleaseId dependencyReleaseId : entry.getValue()) {
                if (filter.accept( dependencyReleaseId, entry.getKey() )) {
                    filteredDependencies.add(dependencyReleaseId);
                }
            }
        }
        return filteredDependencies;
    }

    private AFReleaseId getReleaseIdFromMavenProject(final MavenProject mavenProject) {
        return new AFReleaseIdImpl(mavenProject.getGroupId(),
                                   mavenProject.getArtifactId(),
                                   mavenProject.getVersion(),
                                   mavenProject.getPackaging());
    }

    private AFReleaseId getReleaseIdFromDependency(final Dependency dependency) {
        return new AFReleaseIdImpl(dependency.getGroupId(),
                                   dependency.getArtifactId(),
                                   dependency.getVersion(),
                                   dependency.getType());
    }

    private Map<String, Set<AFReleaseId>> getDirectDependenciesFromMavenSession(final MavenSession mavenSession) {
        final List<Dependency> dependencies = mavenSession.getCurrentProject().getDependencies();
        final Map<String, Set<AFReleaseId>> result = new HashMap<>();
        for (Dependency dependency : dependencies) {
            final Set<AFReleaseId> scopeDependencies = result.computeIfAbsent(dependency.getScope(), s -> new HashSet<>());
            scopeDependencies.add(getReleaseIdFromDependency(dependency));
        }
        return result;
    }
}
