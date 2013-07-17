package org.drools.osgi.spring;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.drools.osgi.compiler.OsgiKieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.net.URL;

public class OsgiKModuleBeanFactoryPostProcessor extends KModuleBeanFactoryPostProcessor {

    public OsgiKModuleBeanFactoryPostProcessor(URL configFileURL, ApplicationContext context) {
        super(configFileURL, null, context);
    }

    @Override
    protected InternalKieModule createKieModule(KieModuleModel kieProject) {
        return OsgiKieModule.create(configFileURL, releaseId, kieProject);
    }

}
