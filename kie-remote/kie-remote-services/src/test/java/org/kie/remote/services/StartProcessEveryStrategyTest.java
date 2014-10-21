package org.kie.remote.services;

import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.UserTaskService;

public interface StartProcessEveryStrategyTest {

    public static final String TEST_PROCESS_DEF_NAME = "org.test.mock.process";
    public static final long TEST_PROCESS_INST_ID = 4;
    
    public void setProcessServiceMock(ProcessService processServiceMock);
    public void setUserTaskServiceMock(UserTaskService userTaskServiceMock);

    public void setupTestMocks();


}
