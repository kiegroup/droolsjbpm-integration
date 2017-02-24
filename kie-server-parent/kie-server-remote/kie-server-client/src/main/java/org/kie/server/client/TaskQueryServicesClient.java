/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import java.util.List;

import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.jbpm.taskqueries.api.model.definition.TaskQueryFilterSpec;

/**
 * KIE-Server Client API for the advanced Task Queries provided by the <code>kie-server-services-jbpm-taskqueries</code> extension.
 * 
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface TaskQueryServicesClient {

	//ddoyle
    /*
     * We don't require a query string, this should be defined in the REST URL (i.e. higher level api).
     * We do need a TaskQueryFilterSpec, which is a variant of QueryFilterSpec, but with pre-defined column-names and no column-mapping.
     * The column-names and column-mapping is defined server-side, to decouple the client from the database schema.
     * Server-side, we need to define a mapping-strategy based on the hibernateDialect that has been defined.
     * 
     * We do still support things like paging in the URL QueryParams.
     * 
     */
    //TODO Should the return value be TaskInstance or TaskSummary
    //TODO Shouldn't this method be in UserTaskServicesClient?
    List<TaskInstance> findHumanTasksWithFilters(TaskQueryFilterSpec filterSpec, Integer page, Integer pageSize);
	
}
