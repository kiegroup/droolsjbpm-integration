/*
/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring.factorybeans;

import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.spring.KieObjectsResolver;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class KBaseFactoryBean
        implements
        FactoryBean<KieBase>,
        InitializingBean {

    private String id;
    private String kBaseName;
    private String packages;
    private String includes;
    private String eventProcessingMode;
    private String equalsBehavior;
    private String declarativeAgenda;
    private String scope;
    private String def;

    private KieBase kBase;
    private ReleaseId releaseId;
    protected boolean singleton = true;
    protected KieContainer kieContainer;

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public String getKBaseName() {
        return kBaseName;
    }

    public void setKBaseName(String name) {
        this.kBaseName = name;
    }
    
    /**
     * Additional Setter to satisfy Spring Eclipse support (avoiding "No setter found" errors).
     * @param name
     *            The name.
     */
    public void setkBaseName(String name) {
        this.kBaseName = name;
    }    

    /**
     * Additional Setter to satisfy Spring Eclipse support (avoiding "No setter found" errors).
     */
    public void setkBaseName(String name) {
        this.kBaseName = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEqualsBehavior() {
        return equalsBehavior;
    }

    public void setEqualsBehavior(String equalsBehavior) {
        this.equalsBehavior = equalsBehavior;
    }

    public String getEventProcessingMode() {
        return eventProcessingMode;
    }

    public void setEventProcessingMode(String eventProcessingMode) {
        this.eventProcessingMode = eventProcessingMode;
    }

    public String getDeclarativeAgenda() {
        return declarativeAgenda;
    }

    public void setDeclarativeAgenda(String declarativeAgenda) {
        this.declarativeAgenda = declarativeAgenda;
    }

    public KieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public KieBase getObject() throws Exception {
        if ( singleton) {
            return kBase;
        } else {
            return kieContainer.newKieBase(kBaseName, null);
        }
    }

    public Class<? extends KieBase> getObjectType() {
        return KieBase.class;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public void setKieBase(KieBase kBase) {
        this.kBase = kBase;
    }

    public void afterPropertiesSet() throws Exception {
        if ( singleton ) {
            KieObjectsResolver kieObjectsResolver = new KieObjectsResolver();
            kBase = kieObjectsResolver.resolveKBase(kBaseName, releaseId);
        }
    }
}
