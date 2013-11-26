package org.kie.services.client.deployment;

import java.util.List;
import java.util.Map;

import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;

public abstract class FluentKieModuleDeploymentHelper extends KieModuleDeploymentHelper {

    /**
     * Fluent API
     */

    public abstract FluentKieModuleDeploymentHelper setGroupId(String groupId);

    public abstract FluentKieModuleDeploymentHelper setArtifactId(String artifactId);

    public abstract FluentKieModuleDeploymentHelper setVersion(String version);

    public abstract FluentKieModuleDeploymentHelper setKBaseName(String kbaseName);
    
    public abstract FluentKieModuleDeploymentHelper setKieSessionname(String ksessionName);

    public abstract FluentKieModuleDeploymentHelper setResourceFilePaths(List<String> resourceFilePaths);

    public abstract FluentKieModuleDeploymentHelper addResourceFilePath(String... resourceFilePath);

    public abstract FluentKieModuleDeploymentHelper setClasses(List<Class<?>> classesForKjar);

    public abstract FluentKieModuleDeploymentHelper addClass(Class<?>... classForKjar);
    
    public abstract FluentKieModuleDeploymentHelper setDependencies(List<String> dependencies);

    public abstract FluentKieModuleDeploymentHelper addDependencies(String... dependency);
    
    public abstract KieModuleModel getKieModuleModel();

    public abstract FluentKieModuleDeploymentHelper resetHelper();
    
    public abstract KieModule createKieJar();
    
    public abstract void createKieJarAndDeployToMaven();
    
}    
