package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kie.api.task.model.Status;

import static org.junit.Assert.*;

public class ResourceBaseTest extends ResourceBase {

    @Test
    public void statusEnumTest() { 
       for( Status testStatus : Status.values())  { 
          Status roundTripStatus = getEnum(testStatus.toString().toLowerCase());
          assertEquals( testStatus + " incorrectly processed!", testStatus, roundTripStatus);
       }
    }

    @Test
    public void testReadNumberAsString() {
        String numberAsString = "\"10\"";

        Map<String, List<String>> params = new HashMap<String, List<String>>();
        List<String> data = new ArrayList<String>();
        data.add(numberAsString);
        params.put("stringnumber", data);

        Object value = getObjectParam("stringnumber", true, params, "dummy");
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals("10", value);
    }

    @Test
    public void testReadNumberAsNumber() {
        String numberString = "10";

        Map<String, List<String>> params = new HashMap<String, List<String>>();
        List<String> data = new ArrayList<String>();
        data.add(numberString);
        params.put("number", data);

        Object value = getObjectParam("number", true, params, "dummy");
        assertNotNull(value);
        assertTrue(value instanceof Number);
        assertEquals(10L, value);
    }
}
