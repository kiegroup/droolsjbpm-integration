/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.rest.query.helpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.kie.api.task.model.Status;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder.OrderBy;
import org.kie.remote.services.rest.query.RemoteServicesQueryJPAService;
import org.kie.remote.services.rest.query.data.QueryResourceData;
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
        RemoteServicesQueryJPAService jpaService = resourceBase.getJPAService();
      
        if( onlyRetrieveLastVarLogs ) { 
            if( variableCriteriaInQuery(procInstLogQueryBuilder.getQueryWhere().getCriteria()) ) { 
                procInstLogQueryBuilder.last();
            }
            varInstLogQueryBuilder.last();
        }
       
        // process instance queries
        procInstLogQueryBuilder.ascending(OrderBy.processInstanceId);
        List<ProcessInstanceLog> procLogs = jpaService.doQuery(
                procInstLogQueryBuilder.getQueryWhere(), 
                ProcessInstanceLog.class);

        // variable instance log queries
        // - limit variable logs retrieved to the process instance ids in the proc logs (since only proc logs have been limited by pagination)
        long [] procLogProcInstIds = new long[procLogs.size()];
        for( int i = 0; i < procLogProcInstIds.length; ++i ) { 
            procLogProcInstIds[i] = procLogs.get(i).getProcessInstanceId();
        }
        varInstLogQueryBuilder.processInstanceId(procLogProcInstIds);
        varInstLogQueryBuilder.ascending(OrderBy.processInstanceId);
        List<VariableInstanceLog> varLogs = jpaService.doQuery(
                varInstLogQueryBuilder.getQueryWhere(),
                VariableInstanceLog.class);
        
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
        for( ProcessInstanceLog procLog : procLogs ) {
            JaxbQueryProcessInstanceInfo procInfo = getQueryProcessInstanceInfo(procLog.getProcessInstanceId(), procInstIdProcInstInfoMap);
            procInfo.setProcessInstance(new JaxbProcessInstance(procLog));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            // The reasoning here is that the number of process logs may be constrained by pagination
            JaxbQueryProcessInstanceInfo procInfo = procInstIdProcInstInfoMap.get(varLog.getProcessInstanceId());
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
