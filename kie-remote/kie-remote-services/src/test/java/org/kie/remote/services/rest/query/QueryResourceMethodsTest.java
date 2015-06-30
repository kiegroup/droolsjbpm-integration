package org.kie.remote.services.rest.query;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;

public class QueryResourceMethodsTest {

    @Test
    public void parameterCheckTest() throws Exception  {
        Map<String, String []> params = new HashMap<String, String[]>();
        addParam(params, "piid", "test");
        Set<String> queryParams = QueryResourceData.getQueryParameters(true); 
        assertTrue( "No query parameters!", queryParams != null && ! queryParams.isEmpty() );
        assertTrue( "'piid' should be one of the query parameters", queryParams.contains("piid"));
        QueryResourceImpl.checkIfParametersAreAllowed(params, queryParams, "test");
       
        addParam(params, "var_myVar", "strVal");
        addParam(params, "enddate_min", "14-11-20");
        addParam(params, "startdate_min", "10-11-20");
        addParam(params, "varregex_myObj", "str*");
        QueryResourceImpl.checkIfParametersAreAllowed(params, queryParams, true, "test");
        
        addParam(params, "enddate_miny", "14-11-20-y");
        try { 
            QueryResourceImpl.checkIfParametersAreAllowed(params, queryParams, true, "test");
            fail("This should have failed because 'enddate_miny' is not valid");
        } catch( KieRemoteRestOperationException krroe ) { 
            params.remove("enddate_miny");
        }

        addParam(params, "varasdf", "not a var");
        try { 
            QueryResourceImpl.checkIfParametersAreAllowed(params, queryParams, true, "test");
            fail("This should have failed because 'varasdf' is not valid");
        } catch( KieRemoteRestOperationException krroe ) { 
            params.remove("varasdf");
        }
        
    }
    
    private void addParam( Map<String, String[]> params, String paramName, String paramValue ) { 
        String [] valArr = { paramValue };
        params.put(paramName, valArr);
    }
    
}
