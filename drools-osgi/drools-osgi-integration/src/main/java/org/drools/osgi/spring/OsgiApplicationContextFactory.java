package org.drools.osgi.spring;

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
