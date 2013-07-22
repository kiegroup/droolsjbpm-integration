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

package org.kie.aries.blueprint.namespace;

import org.apache.aries.blueprint.BeanProcessor;
import org.apache.aries.blueprint.PassThroughMetadata;
import org.apache.aries.blueprint.mutable.MutableValueMetadata;
import org.apache.aries.blueprint.reflect.BeanArgumentImpl;
import org.apache.aries.blueprint.reflect.MetadataUtil;
import org.apache.aries.blueprint.reflect.PassThroughMetadataImpl;
import org.drools.compiler.kie.builder.impl.ClasspathKieProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.compiler.kproject.models.KieSessionModelImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.aries.blueprint.factorybeans.KieObjectsFactoryBean;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanArgument;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KieObjectsInjector implements BeanProcessor {

    private static final Logger log               = LoggerFactory.getLogger(KieObjectsInjector.class);

    private BlueprintContainer blueprintContainer;
    private String contextId;
    private String configFilePath;
    private ReleaseId releaseId;
    private URL configFileURL;
    /** The list of Aries Blueprint XML files*/
    protected java.util.List<java.net.URL> resources;

    public KieObjectsInjector(List<URL> resources) {
        this.resources = resources;
    }

    public KieObjectsInjector(String contextId) {
        this.contextId = contextId;
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
            configFilePath = configFileURL.getPath();
        } else {
            configFileURL = resources.get(0);
            configFilePath = configFileURL.getPath();
            log.debug(" :: Trying to intialize the KieModule from "+configFileURL+" :: ");
        }
        if ( configFilePath == null) {
            throw new RuntimeException("Failure creating a KieModule. Unable to determine the Configuration File Path.");
        }

        String pomProperties = ClasspathKieProject.getPomProperties(configFilePath);
        releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        KieModuleModel kieModuleModel = getKieModuleModel();
        addKieModuleToRepo(kieModuleModel);
        log.debug(" :: Completed Injecting KieObjects from the Blueprint Bean Processor ("+contextId+") :: ");
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
                        String kBaseName = ((MutableValueMetadata) kbRefArg.getValue()).getStringValue();

                        KieBaseModelImpl kBase = new KieBaseModelImpl();
                        kBase.setKModule(kieModuleModel);
                        kBase.setName(kBaseName);
                        kieModuleModel.getRawKieBaseModels().put(kBase.getName(), kBase);

                        PassThroughMetadataImpl throughMetadata = (PassThroughMetadataImpl) MetadataUtil.createMetadata(PassThroughMetadata.class);
                        throughMetadata.setObject(releaseId);
                        ((BeanArgumentImpl)metadata.getArguments().get(1)).setValue(throughMetadata);

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
                        BeanArgument beanArgument = metadata.getArguments().get(5);
                        String kbaseRef = ((MutableValueMetadata) beanArgument.getValue()).getStringValue();
                        beanArgument = metadata.getArguments().get(0);
                        String ksessionName = ((MutableValueMetadata)beanArgument.getValue()).getStringValue();
                        beanArgument = metadata.getArguments().get(6);
                        String type = ((MutableValueMetadata) beanArgument.getValue()).getStringValue();
                        if( kbaseRef.equalsIgnoreCase(kieBaseModel.getName())) {
                            KieSessionModelImpl kSession = new KieSessionModelImpl(kieBaseModel, ksessionName);
                            kSession.setType(type != null ? KieSessionModel.KieSessionType.valueOf(type.toUpperCase()) : KieSessionModel.KieSessionType.STATEFUL);
                            Map<String, KieSessionModel> rawKieSessionModels = kieBaseModel.getRawKieSessionModels();
                            rawKieSessionModels.put(kSession.getName(), kSession);

                            PassThroughMetadataImpl throughMetadata = (PassThroughMetadataImpl) MetadataUtil.createMetadata(PassThroughMetadata.class);
                            throughMetadata.setObject(releaseId);
                            ((BeanArgumentImpl)metadata.getArguments().get(1)).setValue(throughMetadata);
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
        return o;
    }

    @Override
    public void beforeDestroy(Object o, String s) {

    }

    @Override
    public void afterDestroy(Object o, String s) {

    }
}
