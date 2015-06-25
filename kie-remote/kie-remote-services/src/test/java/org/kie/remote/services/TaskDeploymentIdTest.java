package org.kie.remote.services;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;

public interface TaskDeploymentIdTest {

    public void setProcessServiceMock(ProcessService processServiceMock);
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock);
    
    public void setupTestMocks();
   
    public boolean getTasksTest();
}
