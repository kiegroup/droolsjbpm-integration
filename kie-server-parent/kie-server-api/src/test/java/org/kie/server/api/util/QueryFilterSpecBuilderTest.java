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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.util.QueryParamFactory.and;
import static org.kie.server.api.util.QueryParamFactory.equalsTo;
import static org.kie.server.api.util.QueryParamFactory.isNull;

public class QueryFilterSpecBuilderTest {

    @Test
    public void testExpression() {
        QueryFilterSpec filter = QueryFilterSpec.builder()
                                                .where(and(isNull("column1"), equalsTo("column2", 1234)))
                                                .get();
        Assert.assertNotNull(filter.getParameters());
        QueryParam param = filter.getParameters()[0];
        assertEquals("AND", param.getOperator());
        List<QueryParam> params = (List<QueryParam>) param.getValue();
        assertTrue(params.size() == 2);
    }

}
