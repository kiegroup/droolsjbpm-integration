/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import static org.kie.remote.services.rest.ResourceBase.getMaxNumResultsNeeded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.instance.command.GetProcessInstanceVariableCommand;
import org.jbpm.query.jpa.impl.QueryCriteriaUtil;
import org.jbpm.services.task.commands.TaskSummaryQueryCommand;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder;
import org.kie.remote.services.rest.query.RemoteServicesQueryCommandBuilder.OrderBy;
import org.kie.remote.services.rest.query.RemoteServicesQueryJPAService;
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
    public JaxbQueryTaskResult doQueryAndCreateResultObjects( boolean onlyRetrieveLastVarLogs, boolean workFlowInstanceVariables, int [] pageInfo) {

        // setup
        RemoteServicesQueryCommandBuilder [] queryBuilders = getQueryBuilders();
        RemoteServicesQueryCommandBuilder taskQueryBuilder = queryBuilders[0];
        RemoteServicesQueryCommandBuilder varInstLogQueryBuilder = queryBuilders[1];

        if( variableCriteriaInQuery(varInstLogQueryBuilder.getQueryWhere().getCriteria()) && onlyRetrieveLastVarLogs ) {
            taskQueryBuilder.last();
            varInstLogQueryBuilder.last();
        }

        /**
         * TODO: Add pagination information to task query:
         * The problem:
         * A. Because paging is *by process instance id* and we're querying tasks, there is no normal way to set the offset or max results
         * B. for big data sets, the serialization + transport costs (of retrieving the entire set and paging in memory) will always be far larger
         * than any complex queries we do to limit the output.
         *
         * So, to solve this, we need to do 2 things:
         * 1. Add a WHERE or JOIN subquery to the task (JPQL) query that limits the results to the max results (ignoring the offset if necessary)
         *
         * Something like:
         * ---
         * SELECT ..
         * FROM Task t
         * ..
         * WHERE ..
         * AND (select count(distinct p) from t.taskData.processInstanceId p ) <= :maxResultsParam
         * ..
         * ---
         *
         * 2. If possible, add a WHERE subquery to the task (JPQL) query that limits the results to the offset
         * Something like:
         * ---
         * SELECT ..
         * FROM Task t
         * ..
         * WHERE <ORIGINAL QUERY>
         * AND t.taskData.processInstance in (
         *    SELECT DISTINCT t.taskData.processInstance
         *    FROM Task t
         *    WHERE <ORIGINAL QUERY>
         *    GROUP BY t
         *    OFFSET :offset)
         * ---
         *
         * 3. Writing the above by hand is a little ridiculous (and all of the string builder stuff in the query builders).
         * Realistically, the query builder implementations should be using the JPA CriteriaBuilder API. I can't remember why
         * I chose not to that at the beginning, but I'm pretty sure it was a mistake not to do that!
         */

        // task queries
        taskQueryBuilder.ascending(OrderBy.processInstanceId);
        RemoteServicesQueryJPAService jpaService = resourceBase.getJPAService();

        List<TaskSummary> taskSummaries = jpaService.doTaskSummaryQuery(
                taskQueryBuilder.getTaskUserId(),
                resourceBase.getUserGroupCallback(),
                taskQueryBuilder.getQueryWhere());

        if( pageInfo[1] > 0 ) {
            long [] procInstIds = getIncludedProcessInstanceIdsAndRemoveUnneededTaskSummaries(taskSummaries, pageInfo);
            varInstLogQueryBuilder.processInstanceId(procInstIds);
        }

        // variable queries
        varInstLogQueryBuilder.ascending(OrderBy.processInstanceId);
        List<VariableInstanceLog> varLogs = jpaService.doQuery(
                varInstLogQueryBuilder.getQueryWhere(),
                VariableInstanceLog.class);

        // UNFINISHED FEATURE: using in-memory/proces instance variabels instead of audit/history logs
        List<JaxbVariableInfo> procVars = null;
        if( workFlowInstanceVariables ) {
            // TODO: you need the user to specify a deployment id
            for( VariableInstanceLog varLog : varLogs ) {
                String deploymentId = varLog.getExternalId();
                Long processInstId = varLog.getProcessInstanceId();
                GetProcessInstanceVariableCommand cmd = new GetProcessInstanceVariableCommand();
                cmd.setProcessInstanceId(processInstId);
                cmd.setVariableId(varLog.getVariableId());
            }
        }

        // create result
        JaxbQueryTaskResult result = createQueryTaskResult(taskSummaries, varLogs, procVars, pageInfo );

        return result;
    }

    private long [] getIncludedProcessInstanceIdsAndRemoveUnneededTaskSummaries(List<TaskSummary> taskSummaries, int [] pageInfo) {
        // manage offset (will increase performance with large sets, slow performance with small sets)
        int offset = getOffset(pageInfo);
        Set<Long> exclProcInstids = new HashSet<Long>();

        // 1. Remove task summaries before offset (offset is per proc inst id)
        Iterator<TaskSummary> iter = taskSummaries.iterator();
        while( iter.hasNext() && exclProcInstids.size() < offset ) {
            TaskSummary taskSum = iter.next();
            exclProcInstids.add(taskSum.getProcessInstanceId());
            iter.remove();
        }

        // 2a. Add process instance ids from included task summaries
        // 2b. Remove unneeded task summaries
        Set<Long> inclProcInstids = new HashSet<Long>();
        iter = taskSummaries.iterator();
        while( iter.hasNext() ) {
            Long procInstId = iter.next().getProcessInstanceId();
            if( ! exclProcInstids.contains(procInstId) && inclProcInstids.size() < pageInfo[1] ) {
                inclProcInstids.add(procInstId);
                continue;
            }
            if( ! inclProcInstids.contains(procInstId) ) {
                iter.remove();
            }
        }

        // 3. create process instance ids array (for criteria to variable log queriy)
        long [] procInstIdArr = new long[inclProcInstids.size()];
        int i = 0;
        for( Long boxedId : inclProcInstids )  {
            procInstIdArr[i++] = boxedId;
        }
        return procInstIdArr;
    }

    /**
     * Create a {@link JaxbQueryTaskResult} instance from the given information.
     * @param taskSummaries A list of {@link TaskSummary} instances
     * @param varLogs A list of {@link VariableInstanceLog} instances
     * @param processVariables A list of {@link JaxbVariableInfo} instances
     * @return A {@link JaxbQueryTaskResult}
     */
    private static JaxbQueryTaskResult createQueryTaskResult( List<TaskSummary> taskSummaries, List<VariableInstanceLog> varLogs,
            List<JaxbVariableInfo> processVariables, int [] pageInfo ) {
        JaxbQueryTaskResult result = new JaxbQueryTaskResult();

        LinkedHashMap<Long, JaxbQueryTaskInfo> procInstIdTaskInfoMap = new LinkedHashMap<Long, JaxbQueryTaskInfo>();
        int maxNumResultsNeeded = getMaxNumResultsNeeded(pageInfo);
        int i = 0;
        Set<Long> uniqueTaskSummaryIds = new HashSet<Long>(0);
        while( procInstIdTaskInfoMap.size() < maxNumResultsNeeded && i < taskSummaries.size() ) {
            TaskSummary taskSum  = taskSummaries.get(i++);
            assert uniqueTaskSummaryIds.add(taskSum.getId()) : "Duplicate task summary found with id " + taskSum.getId();
            long procInstId = taskSum.getProcessInstanceId();
            JaxbQueryTaskInfo taskInfo = createJaxbQueryTaskInfo(procInstId, procInstIdTaskInfoMap);
            taskInfo.getTaskSummaries().add(new JaxbTaskSummary(taskSum));
        }

        Set<Long> uniqueVarLogIds = new HashSet<Long>(0);
        for( VariableInstanceLog varLog : varLogs ) {
            assert uniqueVarLogIds.add(varLog.getId()) : "Duplicate variable instance log found with id " + varLog.getId();
            long procInstId = varLog.getProcessInstanceId();
            // the reasoning here is that the list of task summaries may be constricted by pagination
            JaxbQueryTaskInfo taskInfo = procInstIdTaskInfoMap.get(procInstId);
            if( taskInfo != null ) {
                taskInfo.getVariables().add(new JaxbVariableInfo(varLog));
            }
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
