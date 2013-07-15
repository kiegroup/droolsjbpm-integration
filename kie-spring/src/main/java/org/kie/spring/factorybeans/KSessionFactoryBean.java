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

import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.compiler.kproject.models.KieSessionModelImpl;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.ListenerModel;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.KieObjectsResolver;
import org.kie.spring.factorybeans.helper.KSessionFactoryBeanHelper;
import org.kie.spring.factorybeans.helper.StatefulKSessionFactoryBeanHelper;
import org.kie.spring.factorybeans.helper.StatelessKSessionFactoryBeanHelper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KSessionFactoryBean
        implements
        FactoryBean,
        InitializingBean {

    private Object kSession;
    private String id;
    private String type;
    private KieBase kBase;
    private String kBaseName;
    private String name;
    private List<Command<?>> batch;
    private KieSessionConfiguration conf;
    private StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration;
    protected KSessionFactoryBeanHelper helper;

    private ReleaseId releaseId;

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public KieSessionConfiguration getConf() {
        return conf;
    }

    public void setConf(KieSessionConfiguration conf) {
        this.conf = conf;
    }

    public String getKBaseName() {
        return kBaseName;
    }

    public void setKBaseName(String kBaseName) {
        this.kBaseName = kBaseName;
    }

    public List<Command<?>> getBatch() {
        return batch;
    }

    public void setBatch(List<Command<?>> commands) {
        this.batch = commands;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KieBase getKBase() {
        return kBase;
    }

    public void setKBase(KieBase kBase) {
        this.kBase = kBase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getObject() throws Exception {
        return helper.internalGetObject();
    }

    public Class<? extends KieRuntime> getObjectType() {
        return KieRuntime.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        KieObjectsResolver kieObjectsResolver = new KieObjectsResolver();

        kSession = kieObjectsResolver.resolveKSession(name, releaseId);
        if (kSession instanceof StatelessKieSession) {
            helper = new StatelessKSessionFactoryBeanHelper(this, (StatelessKieSession) kSession);
        } else if (kSession instanceof KieSession) {
            helper = new StatefulKSessionFactoryBeanHelper(this, (KieSession) kSession);
        }
        helper.internalAfterPropertiesSet();
    }

    public StatefulKSessionFactoryBeanHelper.JpaConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public void setJpaConfiguration(StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

}