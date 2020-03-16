/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.taskassigning;

public class TaskAssigningQueries {

    private TaskAssigningQueries() {
    }

    /**
     * Task assigning standard query for facilitating the tasks consumption by third parties using the standard queries API.
     */
    public static final String JBPM_HUMAN_TASKS_QUERY = "task-assigning-jbpmHumanTasks";

    /**
     * Task assigning standard query for facilitating the tasks consumption by third parties using the standard queries API.
     * This query is sensitive to the current logged user in the KieServicesClient and to the query filtering parameters.
     * Results will include the tasks that fulfills the filter criteria and for which the current logged user is a potential owner.
     */
    public static final String JBPM_HUMAN_TASKS_WITH_USER_QUERY = "task-assigning-jbpmHumanTasksWithUser";

    /**
     * Task assigning standard query mapper that can be used with the JBPM_HUMAN_TASKS_QUERY and JBPM_HUMAN_TASKS_WITH_USER_QUERY
     * queries for returning a List<TaskData> when executing the queries.
     */
    public static final String TASK_DATA_QUERY_MAPPER = "TaskAssigningTaskDataQueryMapper";

    /**
     * Task assigning standard query mapper that can be used with the JBPM_HUMAN_TASKS_QUERY and JBPM_HUMAN_TASKS_WITH_USER_QUERY
     * queries for returning the raw version of the TaskData when executing the queries.
     */
    public static final String TASK_DATA_RAW_QUERY_MAPPER = "TaskAssigningTaskDataRawQueryMapper";

    /**
     * Standard query mapper that can be used with the JBPM_HUMAN_TASKS_QUERY and JBPM_HUMAN_TASKS_WITH_USER_QUERY
     * queries for returning the full query results in the raw version.
     */
    public static final String RAW_LIST_QUERY_MAPPER = "RawList";
}
