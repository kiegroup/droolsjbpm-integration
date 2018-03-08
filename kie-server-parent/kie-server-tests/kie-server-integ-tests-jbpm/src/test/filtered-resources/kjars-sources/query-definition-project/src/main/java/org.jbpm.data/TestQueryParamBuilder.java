/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.data;

import java.util.Map;
import java.util.List;

import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.jbpm.services.api.query.QueryParamBuilder;


public class TestQueryParamBuilder implements QueryParamBuilder<ColumnFilter> {

    private Map<String, Object> parameters;
    private boolean built = false;
    public TestQueryParamBuilder(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public ColumnFilter build() {
        // return null if it was already invoked
        if (built) {
            return null;
        }
        
        String columnName = "processInstanceId";

        ColumnFilter filter;
        if(parameters.containsKey("customparams")) {
            List customParamsList = (List) parameters.get("customparams");
            CustomParameter paramOne = (CustomParameter) customParamsList.get(0);
            CustomParameter paramTwo = (CustomParameter) customParamsList.get(1);
            filter = FilterFactory.OR(
                    FilterFactory.greaterOrEqualsTo(paramOne.getParamValue()),
                    FilterFactory.lowerOrEqualsTo(paramTwo.getParamValue())
            );
            filter.setColumnId(columnName);
        } else {
            filter = FilterFactory.OR(
                    FilterFactory.greaterOrEqualsTo(((Number)parameters.get("min")).longValue()),
                    FilterFactory.lowerOrEqualsTo(((Number)parameters.get("max")).longValue()));
            filter.setColumnId(columnName);
        }
       
        built = true;
        return filter;
    }
}
