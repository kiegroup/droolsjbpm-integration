/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


import java.util.List;

public class KModuleFactoryBean
        implements
        FactoryBean<KieModuleModelImpl>,
        InitializingBean {

    KieModuleModelImpl kModule = null;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public KieModuleModelImpl getKModule() {
        return kModule;
    }

    public void setKModule(KieModuleModelImpl kModule) {
        this.kModule = kModule;
    }

    public KieModuleModelImpl getObject() throws Exception {
        return kModule;
    }

    public Class<? extends KieModuleModelImpl> getObjectType() {
        return KieModuleModelImpl.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        kModule = new KieModuleModelImpl();
    }
}