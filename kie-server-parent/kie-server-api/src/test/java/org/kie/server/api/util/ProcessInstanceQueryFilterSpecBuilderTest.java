/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.kie.server.api.model.definition.ProcessInstanceField;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.model.definition.QueryParam;

import static org.junit.Assert.assertEquals;

public class ProcessInstanceQueryFilterSpecBuilderTest {

    @Test
    public void testGetEqualsTo() {
        ProcessInstanceQueryFilterSpec filterSpec = new ProcessInstanceQueryFilterSpecBuilder().equalsTo(ProcessInstanceField.PROCESSID, "test-process").get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(1, params.length);

        QueryParam param = params[0];
        assertEquals(ProcessInstanceField.PROCESSID.toString(), param.getColumn());
        assertEquals("EQUALS_TO", param.getOperator());
        assertEquals("test-process", param.getValue().stream().findFirst().get());
    }

    @Test
    public void testGetBetween() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date from = null;
        Date to = null;
        try {
            from = sdf.parse("2017-05-10");
            to = sdf.parse("2017-05-14");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ProcessInstanceQueryFilterSpec filterSpec = new ProcessInstanceQueryFilterSpecBuilder().between(ProcessInstanceField.START_DATE, from, to).get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(1, params.length);

        QueryParam param = params[0];
        assertEquals(ProcessInstanceField.START_DATE.toString(), param.getColumn());
        assertEquals("BETWEEN", param.getOperator());
        List<?> values = param.getValue();
        assertEquals(2, values.size());
        assertEquals(from, values.get(0));
        assertEquals(to, values.get(1));
    }

    @Test
    public void testGetEqualsToAndBetween() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date from = null;
        Date to = null;
        try {
            from = sdf.parse("2017-05-10");
            to = sdf.parse("2017-05-14");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ProcessInstanceQueryFilterSpec filterSpec = new ProcessInstanceQueryFilterSpecBuilder().equalsTo(ProcessInstanceField.PROCESSID, "test-process").between(ProcessInstanceField.START_DATE, from, to).get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(2, params.length);

        QueryParam paramEqualsTo = params[0];
        assertEquals(ProcessInstanceField.PROCESSID.toString(), paramEqualsTo.getColumn());
        assertEquals("EQUALS_TO", paramEqualsTo.getOperator());
        assertEquals("test-process", paramEqualsTo.getValue().stream().findFirst().get());

        QueryParam paramBetween = params[1];
        assertEquals(ProcessInstanceField.START_DATE.toString(), paramBetween.getColumn());
        assertEquals("BETWEEN", paramBetween.getOperator());
        List<?> values = paramBetween.getValue();
        assertEquals(2, values.size());
        assertEquals(from, values.get(0));
        assertEquals(to, values.get(1));
    }
}
