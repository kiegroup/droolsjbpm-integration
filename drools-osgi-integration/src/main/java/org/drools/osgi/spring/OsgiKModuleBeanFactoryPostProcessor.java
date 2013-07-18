package org.drools.osgi.spring;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.osgi.compiler.OsgiKieModule;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.context.ApplicationContext;

import java.net.URL;

public class OsgiKModuleBeanFactoryPostProcessor extends KModuleBeanFactoryPostProcessor {

    public OsgiKModuleBeanFactoryPostProcessor() {
        setReleaseId(KieRepositoryImpl.INSTANCE.getDefaultReleaseId());
    }

    public OsgiKModuleBeanFactoryPostProcessor(URL configFileURL, ApplicationContext context) {
        super(configFileURL, null, context);
    }

    @Override
    protected void initConfigFilePath() { }

    @Override
    protected InternalKieModule createKieModule(KieModuleModel kieProject) {
        if (!configFileURL.toString().startsWith("bundle:")) {
            return super.createKieModule(kieProject);
        }
        return OsgiKieModule.create(configFileURL, releaseId, kieProject);
    }

    public void setRelease(String release) {
        setReleaseId(new ReleaseIdImpl(release));
    }
}
