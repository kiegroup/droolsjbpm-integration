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

import org.kie.api.builder.ReleaseId;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

import java.net.URL;

public class OsgiApplicationContextFactory {

    public static OsgiBundleXmlApplicationContext getOsgiSpringContext(ReleaseId releaseId, URL kModuleUrl) {
        OsgiBundleXmlApplicationContext context = new OsgiBundleXmlApplicationContext(new String[] { kModuleUrl.toExternalForm() } );
        OsgiKModuleBeanFactoryPostProcessor beanFactoryPostProcessor = new OsgiKModuleBeanFactoryPostProcessor(kModuleUrl, context);
        beanFactoryPostProcessor.setReleaseId(releaseId);
        context.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
        context.registerShutdownHook();
        return context;
    }

}
