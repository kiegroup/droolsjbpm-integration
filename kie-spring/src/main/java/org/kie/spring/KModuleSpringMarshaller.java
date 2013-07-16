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

import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.IOException;

public class KModuleSpringMarshaller {

    public static KieModuleModel fromXML(File kModuleFile){
        return fromXML(kModuleFile, null);
    }

    public static KieModuleModel fromXML(File kModuleFile, ReleaseId releaseId){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(kModuleFile.getAbsolutePath());
        context.setConfigLocation(kModuleFile.getAbsolutePath());
        context.refresh();
        context.registerShutdownHook();
        return null;//kieSpringApplicationListener.getKieModuleModel();
    }

    public static KieModuleModel fromXML(java.net.URL kModuleUrl, String fixedPath, ReleaseId releaseId){
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext();
        KModuleBeanFactoryPostProcessor beanFactoryPostProcessor = new KModuleBeanFactoryPostProcessor(kModuleUrl, fixedPath, context);
        beanFactoryPostProcessor.setReleaseId(releaseId);
        context.addBeanFactoryPostProcessor(beanFactoryPostProcessor);
        context.setConfigLocation(kModuleUrl.toExternalForm());
        context.refresh();
        context.registerShutdownHook();
        return null;//kieSpringApplicationListener.getKieModuleModel();
    }

    public static KieModuleModel fromXML(java.net.URL kModuleUrl, ReleaseId releaseId){
        return fromXML(kModuleUrl, null, releaseId);
    }

    public static KieModuleModel fromXML(java.net.URL kModuleUrl, String fixedPath){
        return fromXML(kModuleUrl, fixedPath, null);
    }

    public static KieModuleModel fromXML(java.net.URL kModuleUrl){
        return fromXML(kModuleUrl, null, null);
    }

    public static KieModuleModel fromXML(String kmoduleXML){
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext();
        //kieSpringApplicationListener = new KieSpringApplicationContext.KieSpringApplicationListener();
        //context.addApplicationListener(kieSpringApplicationListener);
        try {
            File tempFile = File.createTempFile("kmodule","springXML");
            tempFile.deleteOnExit();
            context.setConfigLocation(tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.refresh();
        context.registerShutdownHook();
        return null;//kieSpringApplicationListener.getKieModuleModel();
    }

}