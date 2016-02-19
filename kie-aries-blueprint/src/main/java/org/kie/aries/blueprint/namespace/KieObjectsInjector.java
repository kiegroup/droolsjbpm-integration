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

package org.kie.aries.blueprint.namespace;

import org.apache.aries.blueprint.BeanProcessor;
import org.apache.aries.blueprint.ParserContext;
import org.apache.aries.blueprint.PassThroughMetadata;
import org.apache.aries.blueprint.mutable.MutableBeanArgument;
import org.apache.aries.blueprint.mutable.MutablePassThroughMetadata;
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
import org.kie.aries.blueprint.factorybeans.KBaseOptions;
import org.kie.aries.blueprint.factorybeans.KSessionOptions;
import org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ValueMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class KieObjectsInjector implements BeanProcessor {

    private static final Logger log               = LoggerFactory.getLogger(KieObjectsInjector.class);

    private BlueprintContainer blueprintContainer;
    private String contextId;
    private String configFilePath;
    private ReleaseId releaseId;
    private URL configFileURL;
    private ParserContext parserContext;

    /** The list of Aries Blueprint XML files*/
    protected java.util.List<java.net.URL> resources;

    public KieObjectsInjector(List<URL> resources) {
        this.resources = resources;
    }

    public KieObjectsInjector(String contextId) {
        this.contextId = contextId;
    }

    public KieObjectsInjector(String contextId, ParserContext parserContext) {
        this.contextId = contextId;
        this.parserContext = parserContext;
    }

    public KieObjectsInjector(List<URL> resources, String contextId) {
        this.resources = resources;
        this.contextId = contextId;
    }

    public KieObjectsInjector() {

    }


    public void setBlueprintContainer(BlueprintContainer blueprintContainer) {
        this.blueprintContainer = blueprintContainer;
    }

    public void afterPropertiesSet(){
        log.debug(" :: Starting Blueprint KieObjectsInjector for kmodule ("+contextId+") :: ");
        if ( resources == null || resources.size() == 0) {
            configFileURL = getClass().getResource("/");
            if (configFileURL == null) {
                createOsgiKieModule();
                return;
            }
            configFilePath = configFileURL.getPath();
        } else {
            configFileURL = resources.get(0);
            configFilePath = configFileURL.getPath();
            log.debug(" :: Trying to intialize the KieModule from " + configFileURL + " :: ");
        }
        if ( configFilePath == null) {
            throw new RuntimeException("Failure creating a KieModule. Unable to determine the Configuration File Path.");
        }

        String pomProperties = ClasspathKieProject.getPomProperties(configFilePath);
        releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        KieModuleModel kieModuleModel = getKieModuleModel();
        injectKieModule(kieModuleModel);
        addKieModuleToRepo(kieModuleModel);
        log.debug(" :: Completed Injecting KieObjects from the Blueprint Bean Processor ("+contextId+") :: ");
    }

    protected void  injectKieModule(KieModuleModel kieModuleModel) {
        ComponentMetadata componentMetadata = blueprintContainer.getComponentMetadata(contextId);
        if ( componentMetadata instanceof MutablePassThroughMetadata){
            ((MutablePassThroughMetadata)componentMetadata).setObject(kieModuleModel);
        }
    }

    private void createOsgiKieModule() {
        configFileURL = getConfigFileURL();
        if (releaseId == null) {
            releaseId = KieRepositoryImpl.INSTANCE.getDefaultReleaseId();
        }
        KieModuleModel kieModuleModel = getKieModuleModel();
        KieBuilderImpl.setDefaultsforEmptyKieModule(kieModuleModel);
        injectKieModule(kieModuleModel);

        InternalKieModule internalKieModule = createOsgiKModule(kieModuleModel);
        if ( internalKieModule != null ) {
            KieServices ks = KieServices.Factory.get();
            ks.getRepository().addKieModule(internalKieModule);
            log.info(" :: Added KieModule From KieObjectsInjector ::");
        }
    }

    private URL getConfigFileURL() {
        try {
            Method m = Class.forName(blueprintContainer.getClass().getName(),
                                     true,
                                     blueprintContainer.getClass().getClassLoader())
                            .getMethod("getBundle");
            Bundle bundle = (Bundle)m.invoke(blueprintContainer);
            return bundle.getEntry("/");
        } catch (Exception e) { }
        return FrameworkUtil.getBundle(this.getClass()).getEntry("/");
    }

    private InternalKieModule createOsgiKModule(KieModuleModel kieProject) {
        Method m;
        try {
            Class<?> c = Class.forName(ClasspathKieProject.OSGI_KIE_MODULE_CLASS_NAME, true, KieBuilderImpl.class.getClassLoader());
            m = c.getMethod("create", URL.class, ReleaseId.class, KieModuleModel.class);
        } catch (Exception e) {
            throw new RuntimeException("It is necessary to have the drools-osgi-integration module on the path in order to create a KieProject from an osgi bundle", e);
        }
        try {
            return (InternalKieModule) m.invoke(null, configFileURL, releaseId, kieProject);
        } catch (Exception e) {
            throw new RuntimeException("Failure creating a OsgiKieModule caused by: " + e.getMessage(), e);
        }
    }

    protected void addKieModuleToRepo(KieModuleModel kieModuleModel) {
        String rootPath = configFilePath;
        if ( rootPath.lastIndexOf( ':' ) > 0 ) {
            rootPath = configFilePath.substring( rootPath.lastIndexOf( ':' ) + 1 );
        }

        KieBuilderImpl.setDefaultsforEmptyKieModule(kieModuleModel);
        InternalKieModule internalKieModule = ClasspathKieProject.createInternalKieModule(configFileURL, configFilePath, kieModuleModel, releaseId, rootPath);
        if ( internalKieModule != null ) {
            KieServices ks = KieServices.Factory.get();
            ks.getRepository().addKieModule(internalKieModule);
            log.info(" :: Added KieModule From KieObjectsInjector ::");
        }
    }

    protected KieModuleModel getKieModuleModel() {
        KieModuleModelImpl kieModuleModel = new KieModuleModelImpl();

        Set<String> ids = blueprintContainer.getComponentIds();
        for (String id: ids) {
            ComponentMetadata componentMetadata = blueprintContainer.getComponentMetadata(id);
            if ( componentMetadata instanceof BeanMetadata) {
                BeanMetadata metadata = (BeanMetadata)componentMetadata;
                if (KieObjectsFactoryBean.class.getName().equals(metadata.getClassName())) {
                    if ("fetchKBase".equalsIgnoreCase(metadata.getFactoryMethod())) {
                        BeanArgument kbRefArg = metadata.getArguments().get(0);
                        String kBaseName = ((ValueMetadata) kbRefArg.getValue()).getStringValue();
                        KieBaseModelImpl kBase = new KieBaseModelImpl();
                        kBase.setKModule(kieModuleModel);
                        kBase.setName(kBaseName);

                        BeanArgument kbOptionsArg = metadata.getArguments().get(2);
                        PassThroughMetadata passThroughMetadata = (PassThroughMetadata) kbOptionsArg.getValue();
                        KBaseOptions kBaseOptions = (KBaseOptions) passThroughMetadata.getObject();
                        String packages = kBaseOptions.getPackages();
                        if ( !kBaseOptions.getPackages().isEmpty()) {
                            for ( String pkg : packages.split( "," ) ) {
                                kBase.addPackage( pkg.trim() );
                            }
                        }

                        String includes = kBaseOptions.getIncludes();
                        if ( !includes.isEmpty() ) {
                            for ( String include : includes.split( "," ) ) {
                                kBase.addInclude( include.trim() );
                            }
                        }

                        String equalsBehavior = kBaseOptions.getEqualsBehavior();
                        if ( !equalsBehavior.isEmpty() ) {
                            kBase.setEqualsBehavior( EqualityBehaviorOption.determineEqualityBehavior(equalsBehavior) );
                        }

                        String eventProcessingMode = kBaseOptions.getEventProcessingMode();
                        if ( !eventProcessingMode.isEmpty() ) {
                            kBase.setEventProcessingMode( EventProcessingOption.determineEventProcessingMode(eventProcessingMode) );
                        }

                        String declarativeAgenda = kBaseOptions.getDeclarativeAgenda();
                        if ( !declarativeAgenda.isEmpty() ) {
                            kBase.setDeclarativeAgenda( DeclarativeAgendaOption.determineDeclarativeAgenda(declarativeAgenda) );
                        }

                        kieModuleModel.getRawKieBaseModels().put(kBase.getName(), kBase);

                        MutablePassThroughMetadata throughMetadata = parserContext.createMetadata(MutablePassThroughMetadata.class);
                        throughMetadata.setObject(releaseId);
                        ((MutableBeanArgument)metadata.getArguments().get(1)).setValue(throughMetadata);

                        addKieSessionModels(kBase);
                    }
                }
            }
        }
        return kieModuleModel;
    }

    private void addKieSessionModels(KieBaseModelImpl kieBaseModel) {
        Set<String> ids = blueprintContainer.getComponentIds();
        for (String id: ids) {
            ComponentMetadata componentMetadata = blueprintContainer.getComponentMetadata(id);
            if ( componentMetadata instanceof BeanMetadata) {
                BeanMetadata metadata = (BeanMetadata)componentMetadata;
                if (KieObjectsFactoryBean.class.getName().equals(metadata.getClassName())) {
                    if ("createKieSession".equalsIgnoreCase(metadata.getFactoryMethod())){
                        BeanArgument beanArgument = metadata.getArguments().get(0);
                        String ksessionName = ((ValueMetadata)beanArgument.getValue()).getStringValue();

                        BeanArgument kbOptionsArg = metadata.getArguments().get(5);
                        PassThroughMetadata passThroughMetadata = (PassThroughMetadata) kbOptionsArg.getValue();
                        KSessionOptions kSessionOptions = (KSessionOptions) passThroughMetadata.getObject();
                        String type = kSessionOptions.getType();
                        String kbaseRef = kSessionOptions.getkBaseRef();

                        if( kbaseRef.equalsIgnoreCase(kieBaseModel.getName())) {
                            KieSessionModelImpl kSession = new KieSessionModelImpl(kieBaseModel, ksessionName);
                            kSession.setType(type != null ? KieSessionModel.KieSessionType.valueOf(type.toUpperCase()) : KieSessionModel.KieSessionType.STATEFUL);

                            kSession.setDefault( "true".equals( kSessionOptions.getDef() ) );
                            String clockType = kSessionOptions.getClockType();
                            if ( clockType != null && !clockType.isEmpty() ) {
                                kSession.setClockType( ClockTypeOption.get(clockType) );
                            }
                            String scope = kSessionOptions.getScope();
                            if ( scope !=null && !scope.isEmpty() ) {
                                kSession.setScope( scope.trim() );
                            }
                            Map<String, KieSessionModel> rawKieSessionModels = kieBaseModel.getRawKieSessionModels();
                            rawKieSessionModels.put(kSession.getName(), kSession);

                            MutablePassThroughMetadata throughMetadata = parserContext.createMetadata(MutablePassThroughMetadata.class);
                            throughMetadata.setObject(releaseId);
                            ((MutableBeanArgument)metadata.getArguments().get(1)).setValue(throughMetadata);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object beforeInit(Object o, String s, BeanCreator beanCreator, BeanMetadata beanMetadata) {
        return o;
    }

    @Override
    public Object afterInit(Object o, String s, BeanCreator beanCreator, BeanMetadata beanMetadata) {
        try {
            return o instanceof Callable ? ( (Callable<Object>) o ).call() : o;
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void beforeDestroy(Object o, String s) {

    }

    @Override
    public void afterDestroy(Object o, String s) {

    }
}
