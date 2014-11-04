package org.kie.remote.services.rest.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.data.QueryData;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;

/**
 * This class contains the core logic for processing the query parameters from a 
 * REST task query call to a {@link JaxbQueryTaskResult} instance.
 */
public class InternalTaskQueryHelper extends AbstractInternalQueryHelper<JaxbQueryTaskResult> {

    public InternalTaskQueryHelper(ResourceBase resourceBase) { 
       super(resourceBase);
    }

    /*
     * (non-Javadoc)
     * @see org.kie.remote.services.rest.query.AbstractInternalQueryHelper#createAndSetQueryBuilders(java.lang.String)
     */
    @Override
    protected void createAndSetQueryBuilders(String identity) { 
        if( identity == null || identity.trim().isEmpty() ) { 
            throw KieRemoteRestOperationException.forbidden("Unknown and unauthorized user [" + identity + "] when querying tasks" );
        }
        // setup query builders
        RemoteServicesQueryCommandBuilder taskQueryBuilder = new RemoteServicesQueryCommandBuilder(identity);
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        setQueryBuilders(taskQueryBuilder, varInstLogQueryBuilder); 
    }

    /*
     * (non-Javadoc)
     * @see org.kie.remote.services.rest.query.AbstractInternalQueryHelper#doQueryAndCreateResultObjects(boolean, boolean)
     */
    @Override
    public JaxbQueryTaskResult doQueryAndCreateResultObjects( boolean onlyRetrieveLastVarLogs, boolean workFlowInstanceVariables) { 

        // setup
        RemoteServicesQueryCommandBuilder [] queryBuilders = getQueryBuilders();
        RemoteServicesQueryCommandBuilder taskQueryBuilder = queryBuilders[0];
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = queryBuilders[1];

        if( variableCriteriaInQuery(varInstLogQueryBuilder.getQueryData()) && onlyRetrieveLastVarLogs ) { 
            taskQueryBuilder.last();
            varInstLogQueryBuilder.last();
        }
      
        // copy query data for var query
        QueryData varLogQueryData = new QueryData(varInstLogQueryBuilder.getQueryData());
        
        // task queries
        TaskQueryDataCommand taskCmd = taskQueryBuilder.createTaskQueryDataCommand();
        List<TaskSummary> taskSummaries = resourceBase.doRestTaskOperation(taskCmd);
       
        // variable queries
        List<VariableInstanceLog> varLogs = resourceBase.getAuditLogService().queryVariableInstanceLogs(varLogQueryData);
        
        // UNFINISHED FEATURE: using in-memory/proces instance variabels instead of audit/history logs
        List<JaxbVariableInfo> procVars = null;
        if( workFlowInstanceVariables ) {
            for( VariableInstanceLog varLog : varLogs ) {
                // TODO: retrieve process instance variables instead of log string values
            }
        }

        // create result
        JaxbQueryTaskResult result = createQueryTaskResult(taskSummaries, varLogs, procVars);
        
        return result;
    }

    /**
     * Create a {@link JaxbQueryTaskResult} instance from the given information.
     * @param taskSummaries A list of {@link TaskSummary} instances
     * @param varLogs A list of {@link VariableInstanceLog} instances
     * @param processVariables A list of {@link JaxbVariableInfo} instances
     * @return A {@link JaxbQueryTaskResult}
     */
    private static JaxbQueryTaskResult createQueryTaskResult( List<TaskSummary> taskSummaries, List<VariableInstanceLog> varLogs,
            List<JaxbVariableInfo> processVariables ) {
        JaxbQueryTaskResult result = new JaxbQueryTaskResult();

        Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap = new HashMap<Long, JaxbQueryTaskInfo>();
        for( TaskSummary taskSum : taskSummaries ) {
            long procInstId = taskSum.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = createJaxbQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getTaskSummaries().add(new JaxbTaskSummary(taskSum));
        }
        for( VariableInstanceLog varLog : varLogs ) {
            long procInstId = varLog.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = createJaxbQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getVariables().add(new JaxbVariableInfo(varLog));
        }

        result.getTaskInfoList().addAll(procInstIdTaskInfoMap.values());
        return result;

    }

    private static JaxbQueryTaskInfo createJaxbQueryTaskInfo( long procInstId, Map<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap ) {
        JaxbQueryTaskInfo taskInfo = procInstIdTaskInfoMap.get(procInstId);
        if( taskInfo == null ) {
            taskInfo = new JaxbQueryTaskInfo(procInstId);
            procInstIdTaskInfoMap.put(procInstId, taskInfo);
        }
        return taskInfo;
    }

}
