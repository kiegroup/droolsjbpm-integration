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

public class QueryParamName {

    public static final String FROM_TASK_ID = "fromTaskId";
    public static final String TO_TASK_ID = "toTaskId";
    public static final String FROM_LAST_MODIFICATION_DATE = "fromLastModificationDate";
    public static final String STATUS = "status";
    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "pageSize";
    public static final String TASK_INPUT_VARIABLES_MODE = "inputVariablesMode";

    private QueryParamName() {
    }
}
