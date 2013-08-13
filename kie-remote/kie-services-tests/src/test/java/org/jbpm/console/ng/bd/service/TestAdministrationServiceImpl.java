package org.jbpm.console.ng.bd.service;

import java.util.Set;

import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit;

public class TestAdministrationServiceImpl implements AdministrationService {

    public void bootstrapConfig() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    public void bootstrapDeployments() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    public void bootstrapRepository(String arg0, String arg1, String arg2, String arg3) {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    public boolean getBootstrapDeploymentsDone() {
        return true;
    }

    public DeploymentService getDeploymentService() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

    public Set<DeploymentUnit> produceDeploymentUnits() {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on this test implementation.");
    }

}
