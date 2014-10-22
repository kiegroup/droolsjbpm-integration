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
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.spring.KieObjectsResolver;
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

public class KieImportFactoryBean
        implements
        FactoryBean<KieContainer>,
        InitializingBean, BeanFactoryPostProcessor, ApplicationContextAware {

    protected ReleaseId releaseId;
    protected KieContainer kContainer;
    protected ApplicationContext applicationContext;
    protected KieObjectsResolver kieObjectsResolver;
    protected boolean scannerEnabled = false;
    protected int scannerInterval = 1000;
    protected KieScanner kieScanner;
    protected String releaseIdName;

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public boolean isScannerEnabled() {
        return scannerEnabled;
    }

    public void setScannerEnabled(boolean scannerEnabled) {
        this.scannerEnabled = scannerEnabled;
    }

    public int getScannerInterval() {
        return scannerInterval;
    }

    public String getReleaseIdName() {
        return releaseIdName;
    }

    public void setReleaseIdName(String releaseIdName) {
        this.releaseIdName = releaseIdName;
    }

    public void setScannerInterval(int scannerInterval) {
        this.scannerInterval = scannerInterval;
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
            if (scannerEnabled){
                kieScanner = KieServices.Factory.get().newKieScanner(kContainer);
                kieScanner.start(scannerInterval);
            }
        } else {
            kContainer = KieServices.Factory.get().getKieClasspathContainer();
        }
    }

    protected void registerKieBases(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        for (String kieBaseName : kContainer.getKieBaseNames()) {
            if ( scannerEnabled ) {
                registerKieBeanDef((BeanDefinitionRegistry) configurableListableBeanFactory, kieBaseName);
                configurableListableBeanFactory.registerSingleton(releaseIdName+"#scanner", kieScanner);
            } else {
                KieBase kieBase = kContainer.getKieBase(kieBaseName);
                configurableListableBeanFactory.registerSingleton(kieBaseName, kieBase);
            }
            registerKieSessions(kieBaseName, configurableListableBeanFactory);
        }
    }

    private void registerKieBeanDef(BeanDefinitionRegistry beanDefinitionRegistry, String kieBaseName) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KBaseFactoryBean.class);
        factory.addPropertyValue("kBaseName", kieBaseName);
        factory.addPropertyValue("id", kieBaseName);
        factory.addPropertyValue("singleton", false);
        factory.addPropertyValue("kieContainer", kContainer);
        beanDefinitionRegistry.registerBeanDefinition(kieBaseName, factory.getBeanDefinition());
    }

    protected void registerKieSessions(String kieBaseName, ConfigurableListableBeanFactory configurableListableBeanFactory) {

        for (String kieSessionName : kContainer.getKieSessionNamesInKieBase(kieBaseName)) {
            Object ksession = kieObjectsResolver.resolveKSession(kContainer, kieSessionName);
            configurableListableBeanFactory.registerSingleton(kieSessionName, ksession);
        }

    }
}
