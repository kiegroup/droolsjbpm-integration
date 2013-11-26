package org.kie.services.client.deployment;

public class KieModuleDeploymentHelper {

    public static final FluentKieModuleDeploymentHelper newFluentInstance() { 
        return new KieModuleDeploymentHelperImpl();
    }
    
    public static final SingleKieModuleDeploymentHelper newSingleInstance() { 
        return new KieModuleDeploymentHelperImpl();
    }
}
