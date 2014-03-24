package org.kie.services.remote;

import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.remote.cdi.DeploymentInfoBean;

public interface TaskDeploymentIdTest {

    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock);
    public void setInjectedTaskServiceMock(InternalTaskService mock);
    public void setRuntimeTaskServiceMock(InternalTaskService mock);
    
    public void setupTestMocks();
    
}
