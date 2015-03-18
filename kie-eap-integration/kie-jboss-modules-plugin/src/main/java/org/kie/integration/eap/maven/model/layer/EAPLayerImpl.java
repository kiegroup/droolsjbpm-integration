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
package org.kie.integration.eap.maven.model.layer;

import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EAPLayerImpl implements EAPLayer {

    private String name;
    private Properties properties;
    private Map<String, EAPModule> modules;

    public EAPLayerImpl(String name) {
        this.name = name;
        this.properties = new Properties();
        this.modules = new HashMap<String, EAPModule>();
    }

    @Override
    public EAPModule addModule(EAPModule module) throws EAPModulesDefinitionException {
        return modules.put(module.getUniqueId(), module);
    }

    @Override
    public EAPModule getModule(String moduleUID) {
        return modules.get(moduleUID);
    }

    @Override
    public EAPModule getModule(Artifact artifact) {
        for (EAPModule module : modules.values()) {
            if (EAPArtifactUtils.equals(artifact, module.getArtifact())) return module;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Collection<EAPModule> getModules() {
        return modules.values();
    }

    public void setModules(Map<String, EAPModule> modules) {
        this.modules = modules;
    }
}
