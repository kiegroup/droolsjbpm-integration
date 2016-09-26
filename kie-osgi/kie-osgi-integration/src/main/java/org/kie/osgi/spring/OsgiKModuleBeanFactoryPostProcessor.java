/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.osgi.spring;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.kie.osgi.compiler.OsgiKieModule;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;

import java.net.URL;

public class OsgiKModuleBeanFactoryPostProcessor extends KModuleBeanFactoryPostProcessor {

    public OsgiKModuleBeanFactoryPostProcessor() {
        setReleaseId(KieRepositoryImpl.INSTANCE.getDefaultReleaseId());
    }

    public OsgiKModuleBeanFactoryPostProcessor(URL configFileURL, ApplicationContext context) {
        super(configFileURL, context);
    }

    @Override
    protected InternalKieModule createKieModule(KieModuleModel kieProject) {
        if (!OsgiKieModule.isOsgiBundleUrl(kModuleRootUrl.toString())) {
            return super.createKieModule(kieProject);
        }
        return OsgiKieModule.create(kModuleRootUrl, releaseId, kieProject);
    }

    public void setRelease(String release) {
        setReleaseId(new ReleaseIdImpl(release));
    }

}
