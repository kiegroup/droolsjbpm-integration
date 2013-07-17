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
package org.kie.spring;

import org.drools.compiler.kie.builder.impl.*;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.compiler.kproject.models.KieSessionModelImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;

import org.kie.spring.factorybeans.KBaseFactoryBean;
import org.kie.spring.factorybeans.KModuleFactoryBean;
import org.kie.spring.factorybeans.KSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

public class KModuleBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger log               = LoggerFactory.getLogger(KModuleBeanFactoryPostProcessor.class);

    protected URL configFileURL;
    protected ReleaseId releaseId;

    private String configFilePath;
    private ApplicationContext context;

    public KModuleBeanFactoryPostProcessor(URL configFileURL, String configFilePath, ApplicationContext context) {
        this.configFileURL = configFileURL;
        this.configFilePath = configFilePath;
        this.context = context;
    }

    public KModuleBeanFactoryPostProcessor(URL configFileURL, String configFilePath) {
        this.configFileURL = configFileURL;
        this.configFilePath = configFilePath;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.debug(":: BeanFactoryPostProcessor::postProcessBeanFactory called ::");
        if ( releaseId == null && configFilePath != null) {
            String pomProperties = ClasspathKieProject.getPomProperties(configFilePath);
            releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
            KieSpringUtils.setDefaultReleaseId(releaseId);
        }

        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName().equalsIgnoreCase(KModuleFactoryBean.class.getName())){
                KieModuleModel kieModuleModel = fetchKieModuleModel(beanFactory);
                addKieModuleToRepo(kieModuleModel);
            }
        }
    }

    private void addKieModuleToRepo(KieModuleModel kieProject) {
        KieBuilderImpl.setDefaultsforEmptyKieModule(kieProject);

        InternalKieModule kJar = createKieModule(kieProject);

        if ( kJar != null ) {
            KieServices ks = KieServices.Factory.get();
            log.info("adding KieModule from "+configFileURL.toExternalForm()+" to repository.");
            ks.getRepository().addKieModule(kJar);
            KieSpringUtils.setReleaseIdForContext(releaseId, context);
        }
    }

    protected InternalKieModule createKieModule(KieModuleModel kieProject) {
        if (configFileURL.toString().startsWith("bundle:")) {
            return createOsgiKModule(kieProject);
        }

        if ( configFilePath == null) {
            configFilePath = getClass().getResource("/").getPath();
        }

        String rootPath = configFilePath;
        if ( rootPath.lastIndexOf( ':' ) > 0 ) {
            rootPath = configFilePath.substring( rootPath.lastIndexOf( ':' ) + 1 );
        }

        return ClasspathKieProject.createInternalKieModule(configFileURL, configFilePath, kieProject, releaseId, rootPath);
    }

    private InternalKieModule createOsgiKModule(KieModuleModel kieProject) {
        Method m;
        try {
            Class<?> c = Class.forName(ClasspathKieProject.OSGI_KIE_MODULE_CLASS_NAME, true, KieBuilderImpl.class.getClassLoader());
            m = c.getMethod("create", URL.class, ReleaseId.class, KieModuleModel.class);
        } catch (Exception e) {
            throw new RuntimeException("It is necessary to have the drools-osgi-integration module on the path in order to create a KieProject from an ogsi bundle", e);
        }
        try {
            return (InternalKieModule) m.invoke(null, configFileURL, releaseId, kieProject);
        } catch (Exception e) {
            throw new RuntimeException("Failure creating a OsgiKieModule caused by: " + e.getMessage(), e);
        }
    }

    private KieModuleModel fetchKieModuleModel(ConfigurableListableBeanFactory beanFactory) {
        KieModuleModelImpl kieModuleModel = new KieModuleModelImpl();
        addKieBaseModels(beanFactory, kieModuleModel);
        return kieModuleModel;
    }

    private void addKieBaseModels(ConfigurableListableBeanFactory beanFactory, KieModuleModelImpl kieModuleModel) {
        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName().equalsIgnoreCase(KBaseFactoryBean.class.getName())){
                KieBaseModelImpl kBase = new KieBaseModelImpl();
                kBase.setKModule(kieModuleModel);
                String kBaseName = (String) beanDefinition.getPropertyValues().getPropertyValue("kBaseName").getValue();
                kBase.setName(kBaseName);
                kieModuleModel.getRawKieBaseModels().put( kBase.getName(), kBase );
                beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("releaseId", releaseId));
                addKieSessionModels(beanFactory, kBase);
            }
        }
    }

    private void addKieSessionModels(ConfigurableListableBeanFactory beanFactory, KieBaseModelImpl kBase) {
        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName().equalsIgnoreCase(KSessionFactoryBean.class.getName())){
                String name = (String) beanDefinition.getPropertyValues().getPropertyValue("name").getValue();
                String type = (String) beanDefinition.getPropertyValues().getPropertyValue("type").getValue();
                String kBaseName = (String) beanDefinition.getPropertyValues().getPropertyValue("kBaseName").getValue();
                if ( kBase.getName().equalsIgnoreCase(kBaseName)) {
                    KieSessionModelImpl kSession = new KieSessionModelImpl(kBase, name);
                    kSession.setType(type != null ? KieSessionModel.KieSessionType.valueOf(type.toUpperCase()) : KieSessionModel.KieSessionType.STATEFUL);
                    Map<String, KieSessionModel> rawKieSessionModels = kBase.getRawKieSessionModels();
                    rawKieSessionModels.put(kSession.getName(), kSession);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("releaseId", releaseId));
                }
            }
        }
    }
}
