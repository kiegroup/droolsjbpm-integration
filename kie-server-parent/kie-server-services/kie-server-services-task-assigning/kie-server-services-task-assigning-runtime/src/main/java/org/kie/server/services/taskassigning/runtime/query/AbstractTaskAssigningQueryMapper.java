/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime.query;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dashbuilder.dataset.DataSet;
import org.jbpm.kie.services.impl.query.mapper.AbstractQueryMapper;
import org.jbpm.services.api.query.QueryResultMapper;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class AbstractTaskAssigningQueryMapper<T> extends AbstractQueryMapper<T> implements QueryResultMapper<List<T>> {

    /**
     * Represents the columns returned by the "jbpm-task-assigning-human-task-with-user",
     * "task-assigning-tasks-with-potential-owners-and-planning-task" and task-assigning-tasks-with-planning-task-optimized"
     * queries.
     * This enum must be kept in sync with any change on this queries.
     */
    public enum TASK_QUERY_COLUMN {
        /**
         * A Long value with the taskId. Is never null.
         */
        TASK_ID("TASKID"),

        /**
         * A time stamp without time zone with the task creation date/time. Is never null.
         */
        CREATED_ON("CREATEDON"),

        /**
         * String with the task actual owner id. Can be null.
         */
        ACTUAL_OWNER("ACTUALOWNER"),

        /**
         * A String with the deploymentId (containerId) to where the task belong. Is never null.
         */
        DEPLOYMENT_ID("DEPLOYMENTID"),

        /**
         * A String with the task name. Is never null.
         */
        TASK_NAME("NAME"),

        /**
         * An Integer with the task priority. Is never null.
         */
        PRIORITY("PRIORITY"),

        /**
         * A String with the process identifier. Is never null.
         */
        PROCESS_ID("PROCESSID"),

        /**
         * A Long value with the process instance id. Is never null.
         */
        PROCESS_INSTANCE_ID("PROCESSINSTANCEID"),

        /**
         * A String value with the task status. Is never null.
         */
        STATUS("STATUS"),

        /**
         * A time stamp without time zone with the task last modification date. Is never null.
         */
        LAST_MODIFICATION_DATE("LASTMODIFICATIONDATE"),

        PLANNING_TASK_TASK_ID("PTTASKID"),

        PLANNING_TASK_ASSIGNED_USER("ASSIGNEDUSER"),

        PLANNING_TASK_INDEX("TASKINDEX"),

        PLANNING_TASK_PUBLISHED("PUBLISHED"),

        /**
         * A String with a potential owner identifier for the task. (a task can have many potential owners). Can be null.
         */
        POTENTIAL_OWNER_ID("POTENTIALOWNER"),

        /**
         * A String with the values "User" or "Group" indicating the type of the potential owner.
         * (a task can have many potential owners)
         * If POTENTIAL_OWNER_ID != null => POTENTIAL_OWNER_TYPE != null.
         * If POTENTIAL_OWNER_ID == null => POTENTIAL_OWNER_TYPE == null.
         */
        POTENTIAL_OWNER_TYPE("POTENTIALOWNERTYPE");

        private String columnName;

        TASK_QUERY_COLUMN(String columnName) {
            this.columnName = columnName;
        }

        public String columnName() {
            return columnName;
        }
    }

    /**
     * Default access to get instance of the mapper
     */
    protected abstract T createInstance();

    protected abstract boolean readPotentialOwners();

    @Override
    protected T buildInstance(DataSet dataSetResult, int index) {
        T row = createInstance();
        final Long taskId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.TASK_ID.columnName(), index);
        final Date createdOn = getColumnDateValue(dataSetResult, TASK_QUERY_COLUMN.CREATED_ON.columnName(), index);
        final Long processInstanceId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.PROCESS_INSTANCE_ID.columnName(), index);
        final String processId = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.PROCESS_ID.columnName(), index);
        final String deploymentId = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.DEPLOYMENT_ID.columnName(), index);
        final String status = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.STATUS.columnName(), index);
        final Integer priority = getColumnIntValue(dataSetResult, TASK_QUERY_COLUMN.PRIORITY.columnName(), index);
        final String taskName = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.TASK_NAME.columnName(), index);
        final Date lastModificationDate = getColumnDateValue(dataSetResult, TASK_QUERY_COLUMN.LAST_MODIFICATION_DATE.columnName(), index);
        final String actualOwner = defaultIfEmpty(getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.ACTUAL_OWNER.columnName(), index), null);

        final Long ptTaskId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_TASK_ID.columnName(), index);
        String assignedUser = null;
        Integer taskIndex = null;
        Integer published = null;
        if (ptTaskId != null) {
            assignedUser = defaultIfEmpty(getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_ASSIGNED_USER.columnName(), index), null);
            taskIndex = getColumnIntValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_INDEX.columnName(), index);
            published = getColumnIntValue(dataSetResult, TASK_QUERY_COLUMN.PLANNING_TASK_PUBLISHED.columnName(), index);
        }

        setInstanceValues(row, taskId, createdOn, processInstanceId, processId, deploymentId, status, priority,
                          taskName, lastModificationDate, actualOwner, assignedUser, taskIndex, published);
        return row;
    }

    protected abstract void setInstanceValues(T row, Long taskId, Date createdOn, Long processInstanceId, String processId,
                                              String deploymentId, String status, Integer priority, String taskName,
                                              Date lastModificationDate, String actualOwner,
                                              String assignedUser, Integer taskIndex, Integer published);

    @Override
    public List<T> map(Object result) {
        DataSet dataSetResult = (DataSet) result;
        final List<T> mappedResult = new ArrayList<>();

        long taskId;
        long previousTaskId = -1;
        String potentialOwnerId;
        String potentialOwnerType;
        T taskRow = null;

        for (int i = 0; i < dataSetResult.getRowCount(); i++) {
            taskId = getColumnLongValue(dataSetResult, TASK_QUERY_COLUMN.TASK_ID.columnName(), i);
            if (previousTaskId != taskId) {
                previousTaskId = taskId;
                taskRow = buildInstance(dataSetResult, i);
                mappedResult.add(taskRow);
            }

            if (readPotentialOwners()) {
                potentialOwnerId = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.POTENTIAL_OWNER_ID.columnName(), i);
                if (isNotEmpty(potentialOwnerId)) {
                    potentialOwnerType = getColumnStringValue(dataSetResult, TASK_QUERY_COLUMN.POTENTIAL_OWNER_TYPE.columnName(), i);
                    addPotentialOwner(taskRow, potentialOwnerId, potentialOwnerType);
                }
            }
        }
        return mappedResult;
    }

    protected abstract void addPotentialOwner(T taskRow, String potentialOwnerId, String potentialOwnerType);

    protected static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        throw new DateTimeException(String.format("Unexpected type %s for toLocalDateTime conversion.", value.getClass()));
    }
}
