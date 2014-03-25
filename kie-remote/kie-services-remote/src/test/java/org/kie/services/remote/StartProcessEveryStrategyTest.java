package org.kie.services.remote;

import org.kie.api.runtime.KieSession;
import org.kie.services.remote.cdi.DeploymentInfoBean;

public interface StartProcessEveryStrategyTest {

    public final static String TEST_PROCESS_DEF_NAME = "org.test.mock.process";
    public final static long TEST_PROCESS_INST_ID = 4;
    
    public void setRuntimeMgrMgrMock(DeploymentInfoBean mock);
    public void setKieSessionMock(KieSession kieSessionMock);
    
    public void setupTestMocks();

    
}
