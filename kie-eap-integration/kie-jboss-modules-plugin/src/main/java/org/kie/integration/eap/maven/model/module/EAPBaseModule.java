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

import org.kie.integration.eap.maven.model.dependency.EAPBaseModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;

/**
 * Module implementation for JBoss EAP base modules (pre-installed ones)
 */
public class EAPBaseModule extends EAPAbstractModule {

    public EAPBaseModule(String name, String slot) {
        super(name, slot);
    }

    @Override
    public EAPModuleDependency createDependency() {
        return new EAPBaseModuleDependency(getName());
    }
}
