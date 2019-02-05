package org.kie.server.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.kie.server.api.model.definition.QueryParam;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;

import static org.junit.Assert.assertEquals;

public class TaskQueryFilterSpecBuilderTest {

    @Test
    public void testGetEqualsTo() {
        TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpecBuilder().equalsTo(TaskField.PROCESSID, "test-process").get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(1, params.length);

        QueryParam param = params[0];
        assertEquals(TaskField.PROCESSID.toString(), param.getColumn());
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

        TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpecBuilder().between(TaskField.CREATEDON, from, to).get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(1, params.length);

        QueryParam param = params[0];
        assertEquals(TaskField.CREATEDON.toString(), param.getColumn());
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

        TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpecBuilder().equalsTo(TaskField.PROCESSID, "test-process").between(TaskField.CREATEDON, from, to).get();

        QueryParam[] params = filterSpec.getParameters();
        assertEquals(2, params.length);

        QueryParam paramEqualsTo = params[0];
        assertEquals(TaskField.PROCESSID.toString(), paramEqualsTo.getColumn());
        assertEquals("EQUALS_TO", paramEqualsTo.getOperator());
        assertEquals("test-process", paramEqualsTo.getValue().stream().findFirst().get());

        QueryParam paramBetween = params[1];
        assertEquals(TaskField.CREATEDON.toString(), paramBetween.getColumn());
        assertEquals("BETWEEN", paramBetween.getOperator());
        List<?> values = paramBetween.getValue();
        assertEquals(2, values.size());
        assertEquals(from, values.get(0));
        assertEquals(to, values.get(1));
    }
}
