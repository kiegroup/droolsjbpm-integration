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

package org.kie.remote.services.rest.query.data;

import static org.kie.remote.services.rest.QueryResourceImpl.*;
import static org.kie.remote.services.rest.ResourceBase.PROC_INST_ID_PARAM_NAME;
import static org.kie.remote.services.rest.query.data.QueryResourceData.*;
import static org.jbpm.query.jpa.data.QueryParameterIdentifiersUtil.getQueryParameterIdNameMap;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.data.QueryResourceData;

import com.google.common.collect.MinMaxPriorityQueue;

public class QueryResourceQueryParametersTest {

    @Test
    public void parameterCheckTest() throws Exception  {
        Map<String, String []> params = new HashMap<String, String[]>();
        addParam(params, "piid", "test");
        Set<String> queryParams = QueryResourceData.getQueryParameters(true);
        assertTrue( "No query parameters!", queryParams != null && ! queryParams.isEmpty() );
        assertTrue( "'piid' should be one of the query parameters", queryParams.contains("piid"));
        checkIfParametersAreAllowed(params, queryParams, "test");

        addParam(params, "var_myVar", "strVal");
        addParam(params, "enddate_min", "14-11-20");
        addParam(params, "startdate_min", "10-11-20");
        addParam(params, "varregex_myObj", "str*");
        checkIfParametersAreAllowed(params, queryParams, true, "test");

        addParam(params, "enddate_miny", "14-11-20-y");
        try {
            checkIfParametersAreAllowed(params, queryParams, true, "test");
            fail("This should have failed because 'enddate_miny' is not valid");
        } catch( KieRemoteRestOperationException krroe ) {
            params.remove("enddate_miny");
        }

        addParam(params, "varasdf", "not a var");
        try {
            checkIfParametersAreAllowed(params, queryParams, true, "test");
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
            = makeQueryParametersLowerCase(wrongCaseParams);

        Iterator<String> iter = correctCaseParamList.iterator();
        while( iter.hasNext() ) {
            String correctParam = iter.next();
            assertTrue( "Parameter was not modified/found: " + correctParam,
                    correctCaseParams.containsKey(correctParam) );
            iter.remove();
        }
        assertTrue( "Not all parameters were found!" , correctCaseParamList.isEmpty() ) ;
    }

    @Test
    public void parameterRegressionTest() throws Exception {
        Map<String, String []> queryParams = new HashMap<String, String[]>();
        Set<String> allowedParams = getQueryParameters(true);
        Set<String> allParams = new HashSet<String>();

        for( String [] params : new String [][] {
                generalQueryParams, generalQueryParamsShort,
                taskQueryParams, taskQueryParamsShort,
                procInstQueryParams, procInstQueryParamsShort,
                varInstQueryParams, varInstQueryParamsShort,
        } ) {
            for( String param : params ) {
                if( param == null ) {
                    continue;
                }
                queryParams.clear();
                allParams.add(param);
                addParam(queryParams, param, "1");
                checkIfParametersAreAllowed(queryParams, allowedParams, true, "test");

            }
        }

        for( String param : minMaxParams ) {
            if( param == null ) {
                continue;
            }
            queryParams.clear();
            String minParam = param + "_min";
            allParams.add(minParam);
            addParam(queryParams, minParam, "1");
            checkIfParametersAreAllowed(queryParams, allowedParams, true, "test");
            String maxParam = param + "_max";
            allParams.add(maxParam);
            addParam(queryParams, maxParam, "1");
            checkIfParametersAreAllowed(queryParams, allowedParams, true, "test");
        }

        for( String param : regexParams ) {
            if( param == null ) {
                continue;
            }
            queryParams.clear();
            String reParam = param + "_re";
            allParams.add(reParam);
            addParam(queryParams, reParam, "1*");
            checkIfParametersAreAllowed(queryParams, allowedParams, true, "test");
        }

        for( String param : nameValueParams ) {
            if( param == null ) {
                continue;
            }
            queryParams.clear();
            String nvParam = param + "_nameofvar";
            addParam(queryParams, param + "_name", "value");
            allParams.add(nvParam);
            checkIfParametersAreAllowed(queryParams, allowedParams, true, "test");
        }

    }

    @Test
    public void testUniqueParameters() throws Exception {

        List<Field> paramFields =
                new LinkedList<Field>(Arrays.asList(QueryResourceData.class.getDeclaredFields()));
        Iterator<Field> iter = paramFields.iterator();
        List<String[]> allParams = new ArrayList<String[]>(paramFields.size());
        while( iter.hasNext() ) {
           Field field = iter.next();
           if( field.getName().equals("minMaxParams")
               || field.getName().equals("nameValueParams")
               || field.getName().equals("regexParams") ) {
               continue;
           }
           if( ! Modifier.isStatic(field.getModifiers()) ) {
              iter.remove();
              continue;
           }
           if( ! field.getType().equals(String[].class) ) {
              continue;
           }
           if( ! field.getName().contains("Params") ) {
              continue;
           }
           field.setAccessible(true);
           allParams.add((String []) field.get(null));
        }

        assertFalse( "No params found", allParams.isEmpty() );

        Set<String> params = new HashSet<String>();
        for( String [] paramArr : allParams ) {
            for( String param : paramArr ) {
                if( param == null ) {
                    continue;
                }
                assertTrue( "Param \"" + param + "\" contains uppercase letters",
                        param.toLowerCase().equals(param) );
                assertTrue( "Param \"" + param + "\" is used twice",
                        params.add(param) );
            }
        }

        for( String param : ResourceBase.paginationParams ){
            assertTrue( "Param \"" + param + "\" contains uppercase letters",
                    param.toLowerCase().equals(param) );
            assertTrue( "Param \"" + param + "\" is used twice",
                    params.add(param) );
        }
        assertTrue( "Param \"" +  PROC_INST_ID_PARAM_NAME + "\" is used twice",
                params.add(PROC_INST_ID_PARAM_NAME) );
    }

    // used to print/check the switch logic in the QueryResourceImpl class
    public void debugPrintSwitch() throws Exception {
        TreeMap<Integer, String> sortedActionParamMap = new TreeMap<Integer, String>(actionParamNameMap);
       for( Entry<Integer, String> action : sortedActionParamMap.entrySet() ) {
           int num = action.getKey();
           switch( num ) {
           case 0:
               System.out.println( "\n// general");
               break;
           case GENERAL_END:
               System.out.println( "\n// task");
               break;
           case TASK_END:
               System.out.println( "\n// process instance");
               break;
           case PROCESS_END:
               System.out.println( "\n// variable instance");
               break;
           case VARIABLE_END:
               System.out.println( "\n// meta");
               break;
           }
           System.out.println( "case " + num + ": // " + action.getValue()  + "\nbreak;");
       }
    }


    public void debugPrintQueryParameterIds() throws Exception {
        Map<Integer, String> idMap = getQueryParameterIdNameMap();
        for( Entry<Integer, String> entry : idMap.entrySet() ) {
            int id = entry.getKey();
            String between = ( id < 10 ? " " : "") + " : ";
            System.out.println( id + between + entry.getValue());
        }
    }

}
