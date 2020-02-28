package org.kie.server.services.taskassigning.runtime.query;

import org.dashbuilder.dataset.DataSet;
import org.kie.server.api.model.taskassigning.TaskData;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class TaskAssigningTaskDataSummaryQueryMapper extends TaskAssigningTaskDataQueryMapper {

    public static final String NAME = "TaskAssigningTaskDataSummaryQueryMapper";

    public TaskAssigningTaskDataSummaryQueryMapper() {
        // Dedicated for ServiceLoader to create instance, use <code>get()</code> method instead
    }

    /**
     * Default access to get instance of the mapper
     */
    public static TaskAssigningTaskDataSummaryQueryMapper get() {
        return new TaskAssigningTaskDataSummaryQueryMapper();
    }

    @Override
    protected boolean readPotentialOwners() {
        return false;
    }

    @Override
    protected TaskData buildInstance(DataSet dataSetResult, int index) {
        TaskData row = createInstance();
        final Long taskId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.TASK_ID.columnName(), index);
        final String status = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.STATUS.columnName(), index);

        String actualOwner = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.ACTUAL_OWNER.columnName(), index);
        if (isEmpty(actualOwner)) {
            actualOwner = null;
        }

        final String deploymentId = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.DEPLOYMENT_ID.columnName(), index);

        final Long ptTaskId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_TASK_ID.columnName(), index);
        String assignedUser = null;
        Integer taskIndex = null;
        Integer published = null;
        if (ptTaskId != null) {
            assignedUser = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_ASSIGNED_USER.columnName(), index);
            if (isEmpty(assignedUser)) {
                assignedUser = null;
            }
            taskIndex = getColumnIntValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_INDEX.columnName(), index);
            published = getColumnIntValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_PUBLISHED.columnName(), index);
        }

        setInstanceValues(row, taskId, null, null, null, deploymentId, status, null,
                          null, null, actualOwner, assignedUser, taskIndex, published);
        return row;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
