/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.rest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.data.QueryResourceData;

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
   
    @Test
    public void lowerCaseParameterTest() throws Exception  {
        Map<String, String[]> wrongCaseParams = new LinkedHashMap<String, String[]>();
        List<String> correctCaseParamList = new ArrayList<String>();
       
        String [] varValue = { "value1", "valTwo" };
        wrongCaseParams.put("VaR_ParamName", varValue);
        correctCaseParamList.add("var_ParamName");
        
        wrongCaseParams.put("VR_ParamName", varValue);
        correctCaseParamList.add("var_ParamName");
        
        wrongCaseParams.put("VarRegex_ParamNamePlus", varValue);
        correctCaseParamList.add("varregex_ParamNamePlus");
        
        wrongCaseParams.put("PiId_MaX", varValue);
        correctCaseParamList.add("piid_max");
        
        wrongCaseParams.put("DeploymendId_RE", varValue);
        correctCaseParamList.add("deploymendid_re");
       
        Map<String, String[]> correctCaseParams 
            = QueryResourceImpl.makeQueryParametersLowerCase(wrongCaseParams);
     
        Iterator<String> iter = correctCaseParamList.iterator();
        while( iter.hasNext() ) { 
            String correctParam = iter.next();
            assertTrue( "Parameter was not modified/found: " + correctParam, 
                    correctCaseParams.containsKey(correctParam) );
            iter.remove();
        }
        assertTrue( "Not all parameters were found!" , correctCaseParamList.isEmpty() ) ;
    } 
}
