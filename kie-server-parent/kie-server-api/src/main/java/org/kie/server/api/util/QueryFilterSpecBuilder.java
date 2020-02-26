/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;

public class QueryFilterSpecBuilder {

    private List<QueryParam> parameters = new ArrayList<QueryParam>();
    private QueryFilterSpec filterSpec = new QueryFilterSpec();
    private Map<String, String> columnMapping = new HashMap<String, String>();

    public QueryFilterSpec get() {
        if (!parameters.isEmpty()) {
            filterSpec.setParameters(parameters.toArray(new QueryParam[parameters.size()]));
        }
        if (!columnMapping.isEmpty()) {
            filterSpec.setColumnMapping(columnMapping);
        }

        return filterSpec;
    }

    public QueryFilterSpecBuilder oderBy(String column, boolean isAscending) {
        filterSpec.setOrderBy(column);
        filterSpec.setAscending(isAscending);

        return this;
    }

    public QueryFilterSpecBuilder addColumnMapping(String column, String type) {
        columnMapping.put(column, type);

        return this;
    }

    public QueryFilterSpecBuilder where(QueryParam param) {
        parameters.add(param);

        return this;
    }

    public QueryFilterSpecBuilder isNull(String column) {
        parameters.add(QueryParamFactory.isNull(column));

        return this;
    }

    public QueryFilterSpecBuilder isNotNull(String column) {
        parameters.add(QueryParamFactory.isNotNull(column));

        return this;
    }

    public QueryFilterSpecBuilder equalsTo(String column, Comparable<?>... values) {
        parameters.add(QueryParamFactory.equalsTo(column, values));

        return this;
    }

    public QueryFilterSpecBuilder notEqualsTo(String column, Comparable<?>... values) {
        parameters.add(QueryParamFactory.notEqualsTo(column, values));

        return this;
    }

    public QueryFilterSpecBuilder likeTo(String column, boolean caseSensitive, Comparable<?> value) {
        parameters.add(QueryParamFactory.likeTo(column, caseSensitive, value));

        return this;
    }

    public QueryFilterSpecBuilder greaterThan(String column, Comparable<?> value) {
        parameters.add(QueryParamFactory.greaterThan(column, value));

        return this;
    }

    public QueryFilterSpecBuilder greaterOrEqualTo(String column, Comparable<?> value) {
        parameters.add(QueryParamFactory.greaterOrEqualsTo(column, value));

        return this;
    }

    public QueryFilterSpecBuilder lowerThan(String column, Comparable<?> value) {
        parameters.add(QueryParamFactory.lowerThan(column, value));

        return this;
    }

    public QueryFilterSpecBuilder lowerOrEqualTo(String column, Comparable<?> value) {
        parameters.add(QueryParamFactory.lowerOrEqualsTo(column, value));

        return this;
    }

    public QueryFilterSpecBuilder between(String column, Comparable<?> start, Comparable<?> end) {
        parameters.add(QueryParamFactory.between(column, start, end));

        return this;
    }

    public QueryFilterSpecBuilder in(String column, List<?> values) {
        parameters.add(QueryParamFactory.in(column, values));

        return this;
    }

    public QueryFilterSpecBuilder notIn(String column, List<?> values) {
        parameters.add(QueryParamFactory.notIn(column, values));

        return this;
    }

}
