package org.kie.services.remote.ejb;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.OperationMessageExecutor;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.remote.cdi.RuntimeManagerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ProcessRequestBean extends OperationMessageExecutor {

    private static Logger logger = LoggerFactory.getLogger(ProcessRequestBean.class);

    private static CorrelationKeyFactory keyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    @Inject
    private RuntimeManagerManager runtimeMgrMgr;

    public OperationMessage doOperation(ServiceMessage request, OperationMessage operation) {
        Object result;
        switch( operation.getServiceType() ) { 
        case ServiceMessage.KIE_SESSION_REQUEST: 
            KieSession kieSession = getRuntimeEngine(request.getDomainName()).getKieSession();
            result = executeOperation(operation, kieSession);
            break;
        case ServiceMessage.TASK_SERVICE_REQUEST:
            TaskService taskService = getRuntimeEngine(request.getDomainName()).getTaskService();
            result = executeOperation(operation, taskService);
            break;
        default:
            throw new UnsupportedOperationException("Unknown service type: " + operation.getServiceType());
        }
        
        // TODO: convert return objects (like a ProcessInstance) to a JAXB arg
        // TODO: also, evaluate whether or not user even *wants* a return value? (== .out() in fluent api)
        return new OperationMessage(operation, null);
    }
    
    protected void handleException(OperationMessage request, Exception e) {
        String serviceClassName = null;
        switch(request.getServiceType()) { 
        case ServiceMessage.KIE_SESSION_REQUEST:
            serviceClassName = KieSession.class.getName();
            break;
        case ServiceMessage.TASK_SERVICE_REQUEST:
            serviceClassName = TaskService.class.getName();
            break;
        }
        logger.error("Failed to invoke method " + serviceClassName + "." + request.getMethodName(), e);

        // TODO: how to handle the exception? "FAIL" OperationMessage back to sender/requester? 
    }

    /**
     * Retrieves the {@link RuntimeEngine}.
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
        
        if( processInstanceId != null ) { 
            managerContext = new ProcessInstanceIdContext(processInstanceId);
        } else {
            managerContext = EmptyContext.get();
        } 
        
        return managerContext;
    }

}
