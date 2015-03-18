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
package org.kie.integration.eap.maven.model.dependency;

import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPConstants;

/**
 * A static dependency defined in module definition.
 */
public class EAPStaticDistributionModuleDependency extends EAPCustomModuleDependency {

    public static final String DISTRO_DEPEPDENCY_FOR_ALL_MODULES = new StringBuilder(EAPConstants.ALL).append(EAPConstants.ARTIFACT_SEPARATOR).append(EAPConstants.ALL).toString();
    private String moduleUID;
    
    public EAPStaticDistributionModuleDependency(String moduleUID, String name) {
        super(name);
        this.moduleUID = moduleUID;
    }

    public EAPStaticDistributionModuleDependency(String moduleUID, EAPModuleDependency dep) {
        super(dep.getName());
        setSlot(dep.getSlot());
        setExport(dep.isExport());
        setOptional(dep.isOptional());
        this.moduleUID = moduleUID;
    }

    public String getModuleUID() {
        return moduleUID;
    }
}
