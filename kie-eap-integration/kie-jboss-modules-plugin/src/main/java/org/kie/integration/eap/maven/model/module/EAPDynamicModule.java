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
package org.kie.integration.eap.maven.model.module;

import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class EAPDynamicModule implements EAPModule {

    private String name;
    private Artifact artifact;
    private Collection<EAPModuleDependency> dependencies;
    private Artifact warFile;
    private boolean addJbossAll;
    private Properties properties;

    public EAPDynamicModule(String name) {
        this.name = name;
        dependencies = new LinkedList<EAPModuleDependency>();
        properties = new Properties();
    }

    @Override
    public EAPLayer getLayer() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public String getSlot() {
        return null;
    }

    @Override
    public String getUniqueId() {
        return name;
    }

    @Override
    public Collection<EAPModuleResource> getResources() {
        return null;
    }

    @Override
    public boolean addResource(EAPModuleResource resource) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<EAPModuleDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean addDependency(EAPModuleDependency dependency) {
        return dependencies.add(dependency);
    }

    @Override
    public EAPModuleDependency getDependency(String uid) {
        if (uid == null || uid.trim().length() == 0) return null;

        for (EAPModuleDependency dependency : dependencies) {
            if (uid.equalsIgnoreCase(EAPArtifactUtils.getUID(dependency.getName(), dependency.getSlot()))) return dependency;
        }
        return null;
    }

    @Override
    public EAPModuleDependency createDependency() {
        // TODO
        return null;
    }

    @Override
    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Artifact getWarFile() {
        return warFile;
    }

    public void setWarFile(Artifact warFile) {
        this.warFile = warFile;
    }

    public boolean isAddJbossAll() {
        return addJbossAll;
    }

    public void setAddJbossAll(boolean addJbossAll) {
        this.addJbossAll = addJbossAll;
    }
}
