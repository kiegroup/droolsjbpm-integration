package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.ResourceBase.getMaxNumResultsNeeded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.drools.core.util.StringUtils;
import org.jbpm.process.audit.AuditLogService;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.internal.query.data.QueryData;
import org.kie.internal.runtime.manager.audit.query.ProcessInstanceLogQueryBuilder;
import org.kie.internal.runtime.manager.audit.query.VariableInstanceLogQueryBuilder;
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
    public JaxbQueryProcessInstanceResult doQueryAndCreateResultObjects(boolean onlyRetrieveLastVarLogs, boolean workFlowInstanceVariables, int [] pageInfo) { 

        // setup
        RemoteServicesQueryCommandBuilder procInstLogQueryBuilder = getQueryBuilders()[0];
        setPaginationParameters(pageInfo, procInstLogQueryBuilder);
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = getQueryBuilders()[1];
        AuditLogService auditLogService = resourceBase.getAuditLogService();
      
        if( onlyRetrieveLastVarLogs ) { 
            if( variableCriteriaInQuery(procInstLogQueryBuilder.getQueryData()) ) { 
                procInstLogQueryBuilder.last();
            }
            varInstLogQueryBuilder.last();
        }
       
        // process instance queries
        procInstLogQueryBuilder.orderBy(ProcessInstanceLogQueryBuilder.OrderBy.processInstanceId);
        List<ProcessInstanceLog> procLogs = auditLogService.queryProcessInstanceLogs(procInstLogQueryBuilder.getQueryData());

        // variable instance log queries
        // - limit variable logs retrieved to the process instance ids in the proc logs (since only proc logs have been limited by pagination)
        long [] procLogProcInstIds = new long[procLogs.size()];
        for( int i = 0; i < procLogProcInstIds.length; ++i ) { 
            procLogProcInstIds[i] = procLogs.get(i).getProcessInstanceId();
        }
        varInstLogQueryBuilder.processInstanceId(procLogProcInstIds);
        varInstLogQueryBuilder.orderBy(VariableInstanceLogQueryBuilder.OrderBy.processInstanceId);
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

    /**
     * Set the pagination parameters on the query builder that determines the number of results (the task query builder, in this case)
     * @param pageInfo Pagination information
     * @param determiningQueryBuilder The query builder that determines the number of results
     */
    protected static void setPaginationParameters(int [] pageInfo, RemoteServicesQueryCommandBuilder determiningQueryBuilder) {
        int offset = getOffset(pageInfo);
        if( offset > 0 ) { 
            determiningQueryBuilder.offset(offset);
        } 
        if( pageInfo[1] > 0 ) { 
            determiningQueryBuilder.maxResults(pageInfo[1]); // page size
        }
    }

    private JaxbQueryProcessInstanceResult createProcessInstanceResult( 
            List<ProcessInstanceLog> procLogs,
            List<VariableInstanceLog> varLogs, 
            List<JaxbVariableInfo> processVariables ) {
        JaxbQueryProcessInstanceResult result = new JaxbQueryProcessInstanceResult();

        Map<Long, JaxbQueryProcessInstanceInfo> procInstIdProcInstInfoMap = new LinkedHashMap<Long, JaxbQueryProcessInstanceInfo>();
        long procInstId = -1;
        for( ProcessInstanceLog procLog : procLogs ) {
            assert procInstId <= procLog.getProcessInstanceId() : procInstId + " not <= " + procLog.getProcessInstanceId();
            procInstId = procLog.getProcessInstanceId();
            JaxbQueryProcessInstanceInfo procInfo = getQueryProcessInstanceInfo(procInstId, procInstIdProcInstInfoMap);
            procInfo.setProcessInstance(new JaxbProcessInstance(procLog));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            procInstId = varLog.getProcessInstanceId();
            // The reasoning here is that the number of process logs may be constrained by pagination
            JaxbQueryProcessInstanceInfo procInfo = procInstIdProcInstInfoMap.get(procInstId);
            if( procInfo != null ) { 
                procInfo.getVariables().add(new JaxbVariableInfo(varLog));
            }
        }

        result.getProcessInstanceInfoList().addAll(procInstIdProcInstInfoMap.values());
        return result;
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
