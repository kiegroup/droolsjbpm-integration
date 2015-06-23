/*
 * Copyright 2015 JBoss Inc
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

package org.kie.aries.blueprint.factorybeans;

import org.kie.api.builder.ReleaseId;

public class KBaseOptions {

    private String packages;
    private String includes;
    private String eventProcessingMode;
    private String equalsBehavior;
    private String declarativeAgenda;
    private String scope;
    private String def;

    public KBaseOptions() {
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getEventProcessingMode() {
        return eventProcessingMode;
    }

    public void setEventProcessingMode(String eventProcessingMode) {
        this.eventProcessingMode = eventProcessingMode;
    }

    public String getEqualsBehavior() {
        return equalsBehavior;
    }

    public void setEqualsBehavior(String equalsBehavior) {
        this.equalsBehavior = equalsBehavior;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDeclarativeAgenda() {
        return declarativeAgenda;
    }

    public void setDeclarativeAgenda(String declarativeAgenda) {
        this.declarativeAgenda = declarativeAgenda;
    }
}
