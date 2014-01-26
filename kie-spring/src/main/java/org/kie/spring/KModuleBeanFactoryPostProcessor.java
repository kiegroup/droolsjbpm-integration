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

import org.kie.api.conf.DeclarativeAgendaOption;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
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
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class KModuleBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static final Logger log            = LoggerFactory.getLogger(KModuleBeanFactoryPostProcessor.class);

    private static final String WEB_INF_FOLDER =  "WEB-INF" + File.separator + "classes" + File.separator;

    protected URL configFileURL;
    protected ReleaseId releaseId;

    private String configFilePath;
    private ApplicationContext context;

    public KModuleBeanFactoryPostProcessor() {
        initConfigFilePath();
    }

    public KModuleBeanFactoryPostProcessor(URL configFileURL, String configFilePath, ApplicationContext context) {
        this.configFileURL = configFileURL;
        this.configFilePath = configFilePath;
        this.context = context;
    }

    public KModuleBeanFactoryPostProcessor(URL configFileURL, String configFilePath) {
        this.configFileURL = configFileURL;
        this.configFilePath = configFilePath;
    }

    protected void initConfigFilePath() {
        configFilePath = getClass().getResource("/").getPath();
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info(":: BeanFactoryPostProcessor::postProcessBeanFactory called ::");
        if ( releaseId == null && configFilePath != null) {
            fixConfigFilePathForVfs();
            String pomProperties = null;
            if ( configFilePath.endsWith(WEB_INF_FOLDER)){
                String configFilePathForWebApps = configFilePath.substring(0, configFilePath.indexOf(WEB_INF_FOLDER));
                pomProperties = ClasspathKieProject.getPomProperties(configFilePathForWebApps);
            }
            if (pomProperties == null) {
                pomProperties = ClasspathKieProject.getPomProperties(configFilePath);
            }
            if (pomProperties != null) {
                releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
            } else {
                releaseId = new ReleaseIdImpl("org.default", "artifact","1.0.0-SNAPSHOT");
            }
            KieSpringUtils.setDefaultReleaseId(releaseId);
        }

        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().equalsIgnoreCase(KModuleFactoryBean.class.getName())){
                KieModuleModel kieModuleModel = fetchKieModuleModel(beanFactory);
                addKieModuleToRepo(kieModuleModel);
            }
        }
    }

    private void fixConfigFilePathForVfs() {
        if (configFileURL != null && configFileURL.toExternalForm().startsWith("vfs:")) {
            String contextPath = ClasspathKieProject.fixURLFromKProjectPath(configFileURL);
            File contextFile = new File(contextPath);
            if (contextFile.exists()) {
                // the spring context file is 2 folders under the temp folder where the war is unzipped
                contextFile = contextFile.getParentFile().getParentFile();
                File mavenFolder = recurseToMavenFolder(contextFile);
                if (mavenFolder != null) {
                    // remove /META-INF/maven since drools pom.properties lookup adds it back
                    configFilePath = mavenFolder.getParentFile().getParent();
                }
            }
        }
    }

    private File recurseToMavenFolder(File file) {
        if( file.isDirectory() ) {
            for ( java.io.File child : file.listFiles() ) {
                if ( child.isDirectory() ) {
                    if ( child.getName().endsWith( "maven" ) ) {
                        return child;
                    }
                    File returnedFile = recurseToMavenFolder( child );
                    if ( returnedFile != null ) {
                        return returnedFile;
                    }
                }
            }
        }
        return null;
    }

    private void addKieModuleToRepo(KieModuleModel kieProject) {
        KieBuilderImpl.setDefaultsforEmptyKieModule(kieProject);

        InternalKieModule kJar = createKieModule(kieProject);

        if ( kJar != null ) {
            KieServices ks = KieServices.Factory.get();
            log.info("adding KieModule from " + configFileURL.toExternalForm() + " to repository.");
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
            if ( beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().equalsIgnoreCase(KBaseFactoryBean.class.getName())){
                KieBaseModelImpl kBase = new KieBaseModelImpl();
                kBase.setKModule(kieModuleModel);

                kBase.setName( getPropertyValue( beanDefinition, "kBaseName" ));
                kBase.setDefault( "true".equals( getPropertyValue(beanDefinition, "def") ) );

                String packages = getPropertyValue( beanDefinition, "packages" );
                if ( !packages.isEmpty() ) {
                    for ( String pkg : packages.split( "," ) ) {
                        kBase.addPackage( pkg.trim() );
                    }
                }

                String includes = getPropertyValue( beanDefinition, "includes" );
                if ( !includes.isEmpty() ) {
                    for ( String include : includes.split( "," ) ) {
                        kBase.addInclude(include.trim());
                    }
                }

                String eventMode = getPropertyValue(beanDefinition, "eventProcessingMode");
                if ( !eventMode.isEmpty() ) {
                    kBase.setEventProcessingMode( EventProcessingOption.determineEventProcessingMode(eventMode) );
                }

                String equalsBehavior = getPropertyValue(beanDefinition, "equalsBehavior");
                if ( !equalsBehavior.isEmpty() ) {
                    kBase.setEqualsBehavior( EqualityBehaviorOption.determineEqualityBehavior(equalsBehavior) );
                }

                String declarativeAgenda = getPropertyValue(beanDefinition, "declarativeAgenda");
                if ( !declarativeAgenda.isEmpty() ) {
                    kBase.setDeclarativeAgenda(DeclarativeAgendaOption.determineDeclarativeAgenda(declarativeAgenda));
                }

                String scope = getPropertyValue(beanDefinition, "scope");
                if ( !scope.isEmpty() ) {
                    kBase.setScope( scope.trim() );
                }

                kieModuleModel.getRawKieBaseModels().put( kBase.getName(), kBase );
                beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("releaseId", releaseId));
                addKieSessionModels(beanFactory, kBase);
            }
        }
    }

    private String getPropertyValue(BeanDefinition beanDefinition, String propertyName) {
        PropertyValue propertyValue = beanDefinition.getPropertyValues().getPropertyValue(propertyName);
        return propertyValue != null ? (String) propertyValue.getValue() : "";
    }

    private void addKieSessionModels(ConfigurableListableBeanFactory beanFactory, KieBaseModelImpl kBase) {
        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().equalsIgnoreCase(KSessionFactoryBean.class.getName())){
                String kBaseName = getPropertyValue(beanDefinition, "kBaseName");
                if ( kBase.getName().equalsIgnoreCase(kBaseName)) {
                    String name = getPropertyValue(beanDefinition, "name");
                    String type = getPropertyValue(beanDefinition, "type");
                    KieSessionModelImpl kSession = new KieSessionModelImpl(kBase, name);

                    kSession.setType(!type.isEmpty() ? KieSessionModel.KieSessionType.valueOf(type.toUpperCase()) : KieSessionModel.KieSessionType.STATEFUL);
                    Map<String, KieSessionModel> rawKieSessionModels = kBase.getRawKieSessionModels();
                    rawKieSessionModels.put(kSession.getName(), kSession);
                    beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("releaseId", releaseId));

                    kSession.setDefault( "true".equals( getPropertyValue(beanDefinition, "def") ) );

                    String clockType = getPropertyValue(beanDefinition, "clockType");
                    if ( !clockType.isEmpty() ) {
                        kSession.setClockType( ClockTypeOption.get(clockType) );
                    }

                    String scope = getPropertyValue(beanDefinition, "scope");
                    if ( !scope.isEmpty() ) {
                        kSession.setScope( scope.trim() );
                    }
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            configFileURL = applicationContext.getResource("classpath:/").getURL();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (configFileURL.toString().endsWith("service-loader-resources/")) {
            String moduleName = getModuleName();
            if (moduleName != null) {
                if (moduleName.endsWith(".war")) {
                    try {
                        configFileURL = new URL("vfs:/content/" + moduleName + "/WEB-INF/classes/");
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private String getModuleName() {
        String clName = getClass().getClassLoader().toString();
        int start = clName.indexOf("deployment.");
        if (start >= 0) {
            start += "deployment.".length();
            int end = Math.min(clName.indexOf(':', start), clName.indexOf('"', start));
            if (end > start) {
                return clName.substring(start, end);
            }
        }
        return null;
    }
}
