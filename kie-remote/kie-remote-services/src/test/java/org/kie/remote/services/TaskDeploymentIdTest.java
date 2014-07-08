package org.kie.remote.services;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.DeploymentInfoBean;

public interface TaskDeploymentIdTest {

    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock);

    public void setProcessServiceMock(ProcessService processServiceMock);
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock);
    
    public void setupTestMocks();
    
}
