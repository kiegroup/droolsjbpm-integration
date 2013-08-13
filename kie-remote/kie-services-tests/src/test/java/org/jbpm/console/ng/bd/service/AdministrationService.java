package org.jbpm.console.ng.bd.service;

import java.util.Set;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;

public interface AdministrationService {

    public void bootstrapRepository(String repoAlias, String repoUrl, String userName, String password);

    public void bootstrapConfig();

    public void bootstrapDeployments();

    public boolean getBootstrapDeploymentsDone();

    public Set<DeploymentUnit> produceDeploymentUnits();

    public DeploymentService getDeploymentService();

}