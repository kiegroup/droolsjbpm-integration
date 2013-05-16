package org.kie.services.remote.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ProcessRequestBean {

    private static Logger logger = LoggerFactory.getLogger(ProcessRequestBean.class);

    private static CorrelationKeyFactory keyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    @Inject
    private RuntimeManagerManager runtimeMgrMgr;
    
    @Inject
    private TaskService taskService;

    public Object doKieSessionOperation(Command cmd, String deploymentId) {
        KieSession kieSession = getRuntimeEngine(deploymentId).getKieSession();
        Object result = kieSession.execute(cmd);
        return result;
    }
    
    public Object doTaskOperation(Command cmd) {
        Object result = ((InternalTaskService) taskService).execute(cmd);
        return result;
    }

    /**
     * Retrieves the {@link RuntimeEngine}.
     * 
     * @param domainName
     * @return
     */
    protected RuntimeEngine getRuntimeEngine(String domainName) {
        return getRuntimeEngine(domainName, null);
    }

    protected RuntimeEngine getRuntimeEngine(String domainName, Long processInstanceId) {
        RuntimeManager runtimeManager = runtimeMgrMgr.getRuntimeManager(domainName);
        Context<?> runtimeContext = getRuntimeManagerContext(processInstanceId);
        return runtimeManager.getRuntimeEngine(runtimeContext);
    }

    private Context<?> getRuntimeManagerContext(Long processInstanceId) {
        Context<?> managerContext;

        if (processInstanceId != null) {
            managerContext = new ProcessInstanceIdContext(processInstanceId);
        } else {
            managerContext = EmptyContext.get();
        }

        return managerContext;
    }

}
