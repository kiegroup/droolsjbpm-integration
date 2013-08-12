package org.kie.services.remote.war;

import java.util.Set;

import org.jbpm.console.ng.bd.service.AdministrationService;
import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;

public class TestAdministrationService implements AdministrationService {

    @Override
    public void bootstrapConfig() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    @Override
    public void bootstrapDeployments() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    @Override
    public void bootstrapRepository(String arg0, String arg1, String arg2, String arg3) {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    @Override
    public boolean getBootstrapDeploymentsDone() {
        return true;
    }

    @Override
    public DeploymentService getDeploymentService() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    @Override
    public Set<DeploymentUnit> produceDeploymentUnits() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

}
