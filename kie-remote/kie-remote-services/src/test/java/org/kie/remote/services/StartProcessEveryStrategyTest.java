package org.kie.remote.services;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;
import org.kie.api.runtime.KieSession;
import org.kie.remote.services.cdi.DeploymentInfoBean;

public interface StartProcessEveryStrategyTest {

    public static final String TEST_PROCESS_DEF_NAME = "org.test.mock.process";
    public static final long TEST_PROCESS_INST_ID = 4;
    
    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock);
    public void setProcessServiceMock(ProcessService processServiceMock);
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock);

    public void setupTestMocks();


}
