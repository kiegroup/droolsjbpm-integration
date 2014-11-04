package org.kie.remote.services.rest.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.core.util.StringUtils;
import org.jbpm.process.audit.AuditLogService;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstance;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;

public class InternalProcInstQueryHelper extends AbstractInternalQueryHelper<JaxbQueryProcessInstanceResult> {

    public InternalProcInstQueryHelper(ResourceBase resourceBase) { 
        super(resourceBase);
    }

    /*
     * (non-Javadoc)
     * @see org.kie.remote.services.rest.query.AbstractInternalQueryHelper#createAndSetQueryBuilders(java.lang.String)
     */
    @Override
    protected void createAndSetQueryBuilders(String identity) { 
        // setup queries
        RemoteServicesQueryCommandBuilder procInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        setQueryBuilders(procInstLogQueryBuilder, varInstLogQueryBuilder);
    }
   
    /*
     * (non-Javadoc)
     * @see org.kie.remote.services.rest.query.AbstractInternalQueryHelper#doQueryAndCreateResultObjects(boolean, boolean)
     */
    @Override
    public JaxbQueryProcessInstanceResult doQueryAndCreateResultObjects(boolean onlyRetrieveLastVarLogs, boolean workFlowInstanceVariables) { 

        // setup
        RemoteServicesQueryCommandBuilder procInstLogQueryBuilder = getQueryBuilders()[0];
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = getQueryBuilders()[1];
        AuditLogService auditLogService = resourceBase.getAuditLogService();
      
        if( onlyRetrieveLastVarLogs ) { 
            if( variableCriteriaInQuery(procInstLogQueryBuilder.getQueryData()) ) { 
                procInstLogQueryBuilder.last();
            }
            varInstLogQueryBuilder.last();
        }
       
        // process instance queries
        List<ProcessInstanceLog> procLogs = auditLogService.queryProcessInstanceLogs(procInstLogQueryBuilder.getQueryData());
        List<VariableInstanceLog> varLogs = auditLogService.queryVariableInstanceLogs(varInstLogQueryBuilder.getQueryData());
        
        // UNFINISHED FEATURE: using in-memory/proces instance variabels instead of audit/history logs
        List<JaxbVariableInfo> procVars = null;
        if( workFlowInstanceVariables ) {
            for( VariableInstanceLog varLog : varLogs ) {
                // TODO: retrieve process instance variables instead of log string values
            }
        }

        // create result
        JaxbQueryProcessInstanceResult result = createProcessInstanceResult(procLogs, varLogs, procVars);
        return result;
    }


    private JaxbQueryProcessInstanceResult createProcessInstanceResult( 
            List<ProcessInstanceLog> procLogs,
            List<VariableInstanceLog> varLogs, 
            List<JaxbVariableInfo> processVariables ) {
        JaxbQueryProcessInstanceResult result = new JaxbQueryProcessInstanceResult();

        Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap = new HashMap<Long, JaxbQueryProcessInstanceInfo>();
        for( ProcessInstanceLog procLog : procLogs ) {
            long procInstId = procLog.getProcessInstanceId();
            JaxbQueryProcessInstanceInfo procInfo = getQueryProcessInstanceInfo(procInstId, procInstIdProcInstInfoMap);
            procInfo.setProcessInstance(new JaxbProcessInstance(procLog));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            long procInstId = varLog.getProcessInstanceId();
            JaxbQueryProcessInstanceInfo procInfo = procInstIdProcInstInfoMap.get(procInstId);
            if( procInfo == null ) { 
                throwDebugExceptionWithQueryInformation();
            }
            procInfo.getVariables().add(new JaxbVariableInfo(varLog));
        }

        result.getProcessInstanceInfoList().addAll(procInstIdProcInstInfoMap.values());
        return result;
    }

    private void throwDebugExceptionWithQueryInformation() { 
        StringBuilder message = new StringBuilder("Please contact the developers: the following query retrieved variable instance logs without retrieving the associated process instance logs:\n");
        QueryData queryData = getQueryBuilders()[0].getQueryData();
        for( Entry<String,  List<? extends Object>> entry : queryData.getIntersectParameters().entrySet() ) { 
           message.append( "[" + entry.getKey() + ":" + StringUtils.collectionToDelimitedString(entry.getValue(), ",") + "], " );
        }
        for( Entry<String,  List<? extends Object>> entry : queryData.getIntersectRangeParameters().entrySet() ) { 
           message.append( "[ (m/m) " + entry.getKey() + ":" + StringUtils.collectionToDelimitedString(entry.getValue(), ",") + "], " );
        }
        for( Entry<String,  List<String>> entry : queryData.getIntersectRegexParameters().entrySet() ) { 
           message.append( "[ (re) " + entry.getKey() + ":" + StringUtils.collectionToDelimitedString(entry.getValue(), ",") + "], " );
        }
        throw KieRemoteRestOperationException.internalServerError(message.toString()); 
    }
    
    private static JaxbQueryProcessInstanceInfo getQueryProcessInstanceInfo( long procInstId,
            Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap ) {
        JaxbQueryProcessInstanceInfo procInstInfo = procInstIdProcInstInfoMap.get(procInstId);
        if( procInstInfo == null ) {
            procInstInfo = new JaxbQueryProcessInstanceInfo();
            procInstIdProcInstInfoMap.put(procInstId, procInstInfo);
        }
        return procInstInfo;
    }


    @Override
    public void taskId(long[] longData) { 
        badParameter(QueryResourceData.taskQueryParams[0]);
    }
    
    @Override
    public void initiator(String[] data) { 
        badParameter(QueryResourceData.taskQueryParams[1]);
    }
    
    @Override
    public void stakeHolder(String[] data) { 
        badParameter(QueryResourceData.taskQueryParams[2]);
    }
    
    @Override
    public void potentialOwner(String[] data) { 
        badParameter(QueryResourceData.taskQueryParams[3]);
    }
    
    @Override
    public void taskOwner(String[] data) { 
        badParameter(QueryResourceData.taskQueryParams[4]);
    }
    
    @Override
    public void businessAdmin(String[] data) { 
        badParameter(QueryResourceData.taskQueryParams[5]);
    }
    
    @Override
    public void taskStatus(Status[] statuses) { 
        badParameter(QueryResourceData.taskQueryParams[6]);
    }
   
    public static void badParameter(String paramName) { 
       throw KieRemoteRestOperationException.badRequest("'" + paramName 
               + "' is not an accepted parameter for the rich process instance query operation" );
    }
}
