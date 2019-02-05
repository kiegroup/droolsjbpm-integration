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

package org.kie.server.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kie.server.api.model.definition.ProcessInstanceField;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;

/**
 * QueryFilterSpecBuilder targeted at filters for Process Instances.
 * <p/>
 * This provides a higher level-api than the Advanced Query API (i.e. {@link QueryFilterSpecBuilder} as the ProcessInstance field-names (column-names)
 * are pre-defined and exposed via a strongly-typed API, and thus not bound to specific column-names in the database. This guards users of
 * this API for potential changes in the jBPM schema.
 */

//TODO: Use the "Curiously Recurring Template Pattern" to create a single builder for tasks and process instances, as the code is the same. Just the method signatures are different.
public class ProcessInstanceQueryFilterSpecBuilder {

    private List<QueryParam> parameters = new ArrayList<QueryParam>();
    private ProcessInstanceQueryFilterSpec filterSpec = new ProcessInstanceQueryFilterSpec();

    public ProcessInstanceQueryFilterSpec get() {
        if (!parameters.isEmpty()) {
            filterSpec.setParameters(parameters.toArray(new QueryParam[parameters.size()]));
        }

        return filterSpec;
    }

    public ProcessInstanceQueryFilterSpecBuilder orderBy(ProcessInstanceField field, boolean isAscending) {
        filterSpec.setOrderBy(field.toString());
        filterSpec.setAscending(isAscending);

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder isNull(ProcessInstanceField field) {
        parameters.add(new QueryParam(field.toString(), "IS_NULL", null));

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder isNotNull(ProcessInstanceField field) {
        parameters.add(new QueryParam(field.toString(), "NOT_NULL", null));

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder equalsTo(ProcessInstanceField field, Comparable<?>... values) {
        parameters.add(new QueryParam(field.toString(), "EQUALS_TO", Arrays.asList(values)));

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder notEqualsTo(ProcessInstanceField field, Comparable<?>... values) {
        parameters.add(new QueryParam(field.toString(), "NOT_EQUALS_TO", Arrays.asList(values)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder likeTo(ProcessInstanceField field, boolean caseSensitive, Comparable<?> value) {
        parameters.add(new QueryParam(field.toString(), "LIKE_TO", Arrays.asList(value, caseSensitive)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder greaterThan(ProcessInstanceField field, Comparable<?> value) {
        parameters.add(new QueryParam(field.toString(), "GREATER_THAN", Arrays.asList(value)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder greaterOrEqualTo(ProcessInstanceField field, Comparable<?> value) {
        parameters.add(new QueryParam(field.toString(), "GREATER_OR_EQUALS_TO", Arrays.asList(value)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder lowerThan(ProcessInstanceField field, Comparable<?> value) {
        parameters.add(new QueryParam(field.toString(), "LOWER_THAN", Arrays.asList(value)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder lowerOrEqualTo(ProcessInstanceField field, Comparable<?> value) {
        parameters.add(new QueryParam(field.toString(), "LOWER_OR_EQUALS_TO", Arrays.asList(value)));

        return this;
    }

    @SuppressWarnings("unchecked")
    public ProcessInstanceQueryFilterSpecBuilder between(ProcessInstanceField field, Comparable<?> start, Comparable<?> end) {
        parameters.add(new QueryParam(field.toString(), "BETWEEN", Arrays.asList(start, end)));

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder in(ProcessInstanceField field, List<?> values) {
        parameters.add(new QueryParam(field.toString(), "IN", values));

        return this;
    }

    public ProcessInstanceQueryFilterSpecBuilder notIn(ProcessInstanceField field, List<?> values) {
        parameters.add(new QueryParam(field.toString(), "NOT_IN", values));

        return this;
    }
}
