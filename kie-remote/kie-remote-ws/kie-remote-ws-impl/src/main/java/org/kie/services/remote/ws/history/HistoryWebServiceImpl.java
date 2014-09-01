package org.kie.services.remote.ws.history;

import javax.inject.Inject;
import javax.jws.WebService;

import org.jbpm.process.audit.AuditLogService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.deployment.generated.ProcessIdsResponse;
import org.kie.remote.services.ws.history.generated.HistoryInstanceLogRequest;
import org.kie.remote.services.ws.history.generated.HistoryWebService;
import org.kie.remote.services.ws.history.generated.HistoryWebServiceException;
import org.kie.remote.services.ws.history.generated.NodeInstanceLogResponse;
import org.kie.remote.services.ws.history.generated.ProcessInstanceLogResponse;
import org.kie.remote.services.ws.history.generated.VariableInstanceLogResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.shared.ServicesVersion;


@WebService(
        serviceName = "HistoryService", 
        portName = "HistoryServicePort", 
        name = "HistoryService", 
        endpointInterface = "org.kie.remote.services.ws.deployment.generated.HistoryWebService",
        targetNamespace = HistoryWebServiceImpl.NAMESPACE)
public class HistoryWebServiceImpl extends ResourceBase implements HistoryWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";

    @Inject
    private ProcessRequestBean processRequestBean;

    private AuditLogService getAuditLogService() { 
        return processRequestBean.getAuditLogService();
    }
        
    @Override
    public ProcessInstanceLogResponse findProcessInstanceLogs( HistoryInstanceLogRequest request ) throws HistoryWebServiceException {
        
        return null;
    }

    @Override
    public NodeInstanceLogResponse findNodeInstanceLogs( HistoryInstanceLogRequest request ) throws HistoryWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

    @Override
    public VariableInstanceLogResponse findVariableInstanceLogs( HistoryInstanceLogRequest request ) throws HistoryWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

}