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

package org.kie.server.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kie.server.api.model.definition.QueryParam;

public final class QueryParamFactory {

    private QueryParamFactory() {}

    public static QueryParam isNull(String column) {
        return new QueryParam(column, "IS_NULL", null);
    }

    public static QueryParam isNotNull(String column) {
        return new QueryParam(column, "NOT_NULL", null);
    }

    public static QueryParam equalsTo(String column, Comparable<?>... values) {
        return new QueryParam(column, "EQUALS_TO", Arrays.asList(values));
    }

    public static QueryParam notEqualsTo(String column, Comparable<?>... values) {
        return new QueryParam(column, "NOT_EQUALS_TO", Arrays.asList(values));
    }

    public static QueryParam likeTo(String column, boolean caseSensitive, Comparable<?> value) {
        return new QueryParam(column, "LIKE_TO", Arrays.asList(value, caseSensitive));
    }

    public static QueryParam greaterThan(String column, Comparable<?> value) {
        return new QueryParam(column, "GREATER_THAN", Arrays.asList(value));
    }

    public static QueryParam greaterOrEqualsTo(String column, Comparable<?> value) {
        return new QueryParam(column, "GREATER_OR_EQUALS_TO", Arrays.asList(value));
    }

    public static QueryParam lowerThan(String column, Comparable<?> value) {
        return new QueryParam(column, "LOWER_THAN", Arrays.asList(value));
    }

    public static QueryParam lowerOrEqualsTo(String column, Comparable<?> value) {
        return new QueryParam(column, "LOWER_OR_EQUALS_TO", Arrays.asList(value));
    }

    public static QueryParam between(String column, Comparable<?> start, Comparable<?> end) {
        return new QueryParam(column, "BETWEEN", Arrays.asList(start, end));
    }

    public static QueryParam in(String column, Object... values) {
        return new QueryParam(column, "IN", Arrays.asList(values));
    }

    public static QueryParam in(String column, List<?> values) {
        return new QueryParam(column, "IN", values);
    }

    public static QueryParam notIn(String column, List<?> values) {
        return new QueryParam(column, "NOT_IN", values);
    }

    public static QueryParam and(QueryParam... params) {
        return new QueryParam(null, "AND", Arrays.stream(params).collect(Collectors.toList()));
    }

    public static QueryParam or(QueryParam... params) {
        return new QueryParam(null, "OR", Arrays.stream(params).collect(Collectors.toList()));
    }

    public static QueryParam not(QueryParam param) {
        return new QueryParam(null, "NOT", Collections.singletonList(param));
    }

    public static List<QueryParam> list(QueryParam... params) {
        return Arrays.asList(params);
    }

}
