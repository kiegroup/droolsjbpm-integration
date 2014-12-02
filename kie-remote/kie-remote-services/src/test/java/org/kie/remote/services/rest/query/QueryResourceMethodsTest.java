package org.kie.remote.services.rest.query;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.remote.services.rest.QueryResourceImpl;

public class QueryResourceMethodsTest {

    @Test
    public void parameterCheckTest() throws Exception  {
        Map<String, String []> params = new HashMap<String, String[]>();
        String [] valArr = { "test" };
        params.put("piid", valArr);
        Set<String> queryParams = QueryResourceData.getQueryParameters(); 
        assertTrue( "No query parameters!", queryParams != null && ! queryParams.isEmpty() );
        assertTrue( "'piid' should be one of the query parameters", queryParams.contains("piid"));
        QueryResourceImpl.checkIfParametersAreAllowed(params, queryParams, "test");
    }
    
}
