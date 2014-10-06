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

import org.drools.compiler.kie.builder.impl.*;
import org.drools.compiler.kie.builder.impl.event.KieModuleDiscovered;
import org.drools.compiler.kie.builder.impl.event.KieServicesEventListerner;
import org.drools.core.io.impl.ClassPathResource;
import org.drools.persistence.jpa.KnowledgeStoreServiceImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.KieContainer;
import org.kie.spring.KieObjectsResolver;
import org.kie.spring.KieSpringUtils;
import org.kie.spring.annotations.AnnotationsUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

public class KieImportFactoryBean
        implements
        FactoryBean<KieContainer>,
        InitializingBean, BeanFactoryPostProcessor, ApplicationContextAware {

    protected ReleaseId releaseId;
    protected KieContainer kContainer;
    protected ApplicationContext applicationContext;
    protected KieObjectsResolver kieObjectsResolver;

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public KieContainer getObject() throws Exception {
        return kContainer;
    }

    public Class<? extends KieContainer> getObjectType() {
        return KieContainer.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        kieObjectsResolver = new KieObjectsResolver();

        setKContainer();

        registerKieBases(beanFactory);

        AnnotationsUtil.registerAnnotationConfigProcessors((BeanDefinitionRegistry) beanFactory, releaseId);
    }

    protected void setKContainer() {
        if (releaseId != null) {
            kContainer = KieServices.Factory.get().newKieContainer(releaseId);
        } else {
            kContainer = KieServices.Factory.get().getKieClasspathContainer();
        }
    }

    protected void registerKieBases(ConfigurableListableBeanFactory configurableListableBeanFactory) {

        for (String kieBaseName : ((KieContainerImpl)kContainer).getKieBaseNames()) {
            KieBase kieBase = kContainer.getKieBase(kieBaseName);
            configurableListableBeanFactory.registerSingleton(kieBaseName, kieBase);

            registerKieSessions(kieBaseName, configurableListableBeanFactory);
        }
    }

    protected void registerKieSessions(String kieBaseName, ConfigurableListableBeanFactory configurableListableBeanFactory) {

        for (String kieSessionName : ((KieContainerImpl)kContainer).getKieSessionNamesInKieBase(kieBaseName)) {
            Object ksession = kieObjectsResolver.resolveKSession(kContainer, kieSessionName);
            configurableListableBeanFactory.registerSingleton(kieSessionName, ksession);
        }

    }
}
