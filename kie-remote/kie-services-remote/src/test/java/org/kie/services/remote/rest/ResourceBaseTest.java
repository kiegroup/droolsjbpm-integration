package org.kie.services.remote.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kie.api.task.model.Status;


public class ResourceBaseTest extends ResourceBase {

    @Test
    public void statusEnumTest() { 
       for( Status testStatus : Status.values())  { 
          Status roundTripStatus = getEnum(testStatus.toString().toLowerCase());
          assertEquals( testStatus + " incorrectly processed!", testStatus, roundTripStatus);
       }
    }
}
