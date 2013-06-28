package org.kie.services.remote.cdi;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;

@ApplicationScoped
public class ProcessRequestBean {

    @Inject
    private Logger logger;

    @Inject
    private RuntimeManagerManager runtimeMgrMgr;
    
    @Inject
    private TaskService taskService;

    public Object doKieSessionOperation(Command<?> cmd, String deploymentId) {
        return doKieSessionOperation(cmd, deploymentId, null);
    }
    
    public Object doKieSessionOperation(Command<?> cmd, String deploymentId, Long processInstanceId) {
        KieSession kieSession = getRuntimeEngine(deploymentId, processInstanceId).getKieSession();
        Object result = null;
        try { 
            result = kieSession.execute(cmd);
        } catch( Exception e ) { 
            JaxbExceptionResponse exceptResp = new JaxbExceptionResponse(e, cmd);
            logger.log(Level.WARNING, "Unable to execute " + exceptResp.getCommandName() + " because of " + e.getClass().getSimpleName(), e);
            result = exceptResp;
        }
        return result;
    }
    
    public Object doTaskOperation(Command<?> cmd) {
        Object result = null;
        try { 
            result = ((InternalTaskService) taskService).execute(cmd);
        } catch( Exception e ) { 
            JaxbExceptionResponse exceptResp = new JaxbExceptionResponse(e, cmd);
            logger.log(Level.WARNING, "Unable to execute " + exceptResp.getCommandName() + " because of " + e.getClass().getSimpleName(), e);
            result = exceptResp;
        }
        return result;
    }

    protected RuntimeEngine getRuntimeEngine(String domainName, Long processInstanceId) {
        RuntimeManager runtimeManager = runtimeMgrMgr.getRuntimeManager(domainName);
        Context<?> runtimeContext;
        if (processInstanceId != null) {
            runtimeContext = new ProcessInstanceIdContext(processInstanceId);
        } else {
            runtimeContext = EmptyContext.get();
        }
        return runtimeManager.getRuntimeEngine(runtimeContext);
    }

    public Logger getLogger() { 
        return logger;
    }
}
