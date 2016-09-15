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

package org.kie.spring;

import org.drools.compiler.kie.builder.impl.ClasspathKieProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
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
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

@Component("kiePostProcessor")
public class KModuleBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(KModuleBeanFactoryPostProcessor.class);

    /* URLs only contain forward slashes - '/'
     * See https://docs.oracle.com/javase/6/docs/api/java/net/URL.html for more info. */
    private static final String WEB_INF_CLASSES_URL_SUFFIX = "WEB-INF/classes/";
    /* Paths do contain OS-specific separators */
    private static final String WEB_INF_CLASSES_PATH_SUFFIX = "WEB-INF" + File.separator + "classes";
    /**
     * Root URL of the KieModule which is associated with the Spring app context.
     *
     * After transforming the URL to a filesystem path it is used as base dir for the KieModule.
     *
     * Example: "file:/some-path/target/test-classes".
     */
    protected URL kModuleRootUrl;
    protected ReleaseId releaseId;
    private ApplicationContext context;

    public KModuleBeanFactoryPostProcessor() {
    }

    public KModuleBeanFactoryPostProcessor(URL kModuleRootUrl, ApplicationContext context) {
        this.kModuleRootUrl = kModuleRootUrl;
        this.context = context;
    }

    public KModuleBeanFactoryPostProcessor(URL kModuleRootUrl) {
        this.kModuleRootUrl = kModuleRootUrl;
    }

    public URL getkModuleRootUrl() {
        return kModuleRootUrl;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info(":: BeanFactoryPostProcessor::postProcessBeanFactory called ::");
        String kModuleRootPath = parseKModuleRootPath(kModuleRootUrl);
        if (releaseId == null && kModuleRootPath != null) {
            String pomProperties = null;
            if (kModuleRootPath.endsWith(WEB_INF_CLASSES_PATH_SUFFIX)) {
                String configFilePathForWebApps = kModuleRootPath.substring(0, kModuleRootPath.indexOf(WEB_INF_CLASSES_PATH_SUFFIX));
                pomProperties = ClasspathKieProject.getPomProperties(configFilePathForWebApps);
            }
            if (pomProperties == null) {
                pomProperties = ClasspathKieProject.getPomProperties(kModuleRootPath);
            }
            if (pomProperties != null) {
                releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
            } else {
                releaseId = KieRepositoryImpl.INSTANCE.getDefaultReleaseId();
            }
            log.info("Found project with releaseId: " + releaseId);
        }
        if (releaseId == null) {
            releaseId = KieRepositoryImpl.INSTANCE.getDefaultReleaseId();
        }

        for (String beanDef : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if (beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().equalsIgnoreCase(KModuleFactoryBean.class.getName())) {
                KieModuleModel kieModuleModel = fetchKieModuleModel(beanFactory);
                addKieModuleToRepo(kieModuleModel);
            }
        }
    }

    private String parseKModuleRootPath(URL kModuleRootUrl) {
        return ClasspathKieProject.fixURLFromKProjectPath(kModuleRootUrl);
    }

    private void addKieModuleToRepo(KieModuleModel kieProject) {
        KieBuilderImpl.setDefaultsforEmptyKieModule(kieProject);

        InternalKieModule kJar = createKieModule(kieProject);

        if ( kJar != null ) {
            KieServices ks = KieServices.Factory.get();
            log.info("Adding KieModule from " + parseKModuleRootPath(kModuleRootUrl) + " to repository.");
            ks.getRepository().addKieModule(kJar);
        }
    }

    protected InternalKieModule createKieModule(KieModuleModel kieProject) {
        String rootPath = parseKModuleRootPath(kModuleRootUrl);
        if (rootPath.lastIndexOf(':') >= 2) {
            rootPath = rootPath.substring(rootPath.lastIndexOf(':') + 1);
        }
        return ClasspathKieProject.createInternalKieModule(kieProject, releaseId, rootPath);
    }

    private KieModuleModel fetchKieModuleModel(ConfigurableListableBeanFactory beanFactory) {
        KieModuleModelImpl kieModuleModel = new KieModuleModelImpl();
        addKieBaseModels(beanFactory, kieModuleModel);
        return kieModuleModel;
    }

    private void addKieBaseModels(ConfigurableListableBeanFactory beanFactory, KieModuleModelImpl kieModuleModel) {
        BeanExpressionContext context = new BeanExpressionContext(beanFactory, null);
        for (String beanDef : beanFactory.getBeanDefinitionNames()){
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDef);
            if ( beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().equalsIgnoreCase(KBaseFactoryBean.class.getName())){
                KieBaseModelImpl kBase = new KieBaseModelImpl();
                kBase.setKModule(kieModuleModel);

                kBase.setName( getPropertyValue( beanDefinition, "kBaseName" ));
                kBase.setDefault( "true".equals( getPropertyValue(beanDefinition, "def") ) );

                String packages = getPropertyValue( beanDefinition, "packages" );
                if ( !packages.isEmpty() ) {
                    packages = checkAndResolveSpringExpression(beanFactory, context, packages);
                    for ( String pkg : packages.split( "," ) ) {
                        kBase.addPackage( pkg.trim() );
                    }
                }

                String includes = getPropertyValue( beanDefinition, "includes" );
                if ( !includes.isEmpty() ) {
                    includes = checkAndResolveSpringExpression(beanFactory, context, includes);
                    for ( String include : includes.split( "," ) ) {
                        kBase.addInclude(include.trim());
                    }
                }

                String eventMode = getPropertyValue(beanDefinition, "eventProcessingMode");
                if ( !eventMode.isEmpty() ) {
                    eventMode = checkAndResolveSpringExpression(beanFactory, context, eventMode);
                    kBase.setEventProcessingMode( EventProcessingOption.determineEventProcessingMode(eventMode) );
                }

                String equalsBehavior = getPropertyValue(beanDefinition, "equalsBehavior");
                if ( !equalsBehavior.isEmpty() ) {
                    equalsBehavior = checkAndResolveSpringExpression(beanFactory, context, equalsBehavior);
                    kBase.setEqualsBehavior( EqualityBehaviorOption.determineEqualityBehavior(equalsBehavior) );
                }

                String declarativeAgenda = getPropertyValue(beanDefinition, "declarativeAgenda");
                if ( !declarativeAgenda.isEmpty() ) {
                    declarativeAgenda = checkAndResolveSpringExpression(beanFactory, context, declarativeAgenda);
                    kBase.setDeclarativeAgenda(DeclarativeAgendaOption.determineDeclarativeAgenda(declarativeAgenda));
                }

                String scope = getPropertyValue(beanDefinition, "scope");
                if ( !scope.isEmpty() ) {
                    scope = checkAndResolveSpringExpression(beanFactory, context, scope);
                    kBase.setScope( scope.trim() );
                }

                kieModuleModel.getRawKieBaseModels().put( kBase.getName(), kBase );
                beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("releaseId", releaseId));
                addKieSessionModels(beanFactory, kBase);
            }
        }
    }

    protected String checkAndResolveSpringExpression(ConfigurableListableBeanFactory beanFactory, BeanExpressionContext context, String expression) {
        if ( expression.startsWith("#{") && expression.endsWith("}")) {
            return (String) beanFactory.getBeanExpressionResolver().evaluate(expression, context);
        }
        return expression;
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
        this.context = applicationContext;
        try {
            kModuleRootUrl = tryGetRootUrlForEapContext(applicationContext.getClassLoader().getResources("/"));
            // in case the kModuleRootUrl is still null at this point, the assumption is we are not running on EAP
            // so we just get the url from the classpath
            if (kModuleRootUrl == null) {
                kModuleRootUrl = applicationContext.getResource("classpath:/").getURL();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to get root URL for the application context " +
                    applicationContext.getDisplayName(), e);
        }
        log.debug("KieModule root URL (based on application context {}): {}", applicationContext.getDisplayName(), kModuleRootUrl);
    }

    /**
     * This is a HACK for web applications deployed to EAP/WildFly which are using kie-spring.
     *
     * The method tries to figure out the root URL based on the provided URL enumeration. It covers (at least) the
     * following two use cases:
     *   1) kie-spring deployed together (bundled) with Spring webapp (inside WEB-INF/lib)
     *   2) kie-spring deployed as EAP module + Spring webapp depending on that module
     *
     * First of all it tries to determine if it is running on EAP, based on EAP specific resource URL. If that is the
     * case it looks for the "WEB-INF/classes" dir and return that as an VFS URL (vfs:/...). Later on, this URL
     * needs to be translated to a real filesystem path. This is one by {@link ClasspathKieProject#fixURLFromKProjectPath(URL)}
     *
     * @param rootUrls Classpath root URLs
     * @return NULL is case the code is not running on EAP, otherwise root URL of the webapp context (that is webapp's WEB-INF/classes)
     */
    URL tryGetRootUrlForEapContext(Enumeration<URL> rootUrls) {
        boolean containsEapSpecificUrl = false;
        boolean containsWebInfClassesUrl = false;
        URL webInfClassesUrl = null;
        while (rootUrls.hasMoreElements()) {
            URL url = rootUrls.nextElement();
            if (isEapSpecificUrl(url)) {
                containsEapSpecificUrl = true;
            } else if (url.toString().endsWith(WEB_INF_CLASSES_URL_SUFFIX)) {
                containsWebInfClassesUrl = true;
                webInfClassesUrl = url;
            }
        }
        if (containsEapSpecificUrl && containsWebInfClassesUrl) {
            return webInfClassesUrl;
        } else {
            return null;
        }
    }

    /**
     * Check if the provided URL is EAP specific URL. The method is used to check if the
     * code running inside EAP. Yes, this is a very ugly hack, but there does not seem a better way around.
     *
     * @param url URL to check
     * @return true in case the enumeration contains EAP specific URL, otherwise false
     */
    boolean isEapSpecificUrl(URL url) {
        return url.toString().endsWith("service-loader-resources/");
    }

}
