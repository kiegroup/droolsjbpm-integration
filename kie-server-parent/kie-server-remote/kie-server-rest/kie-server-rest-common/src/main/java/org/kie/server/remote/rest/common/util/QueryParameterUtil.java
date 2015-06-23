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

package org.kie.server.remote.rest.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Variant;

import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;

public class QueryParameterUtil {

    public static Map<String, Object> extractMapFromParams(Map<String, String[]> params, String operation, Variant v) {
        Map<String, Object> map = new HashMap<String, Object>();
    
        for (Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] paramValues = entry.getValue();
            if (paramValues.length != 1) {
                throw ExecutionServerRestOperationException.badRequest(
                        "Only one (" + key + ") query parameter allowed for the '" + operation
                        + "' operation (" + paramValues.length + " passed).", 
                        v);
            }
            String mapVal = paramValues[0].trim();

            map.put(key, getObjectFromString(key, mapVal, v));
        }
        return map;
    }


    protected static String getStringParam(String paramName, boolean required, Map<String, String[]> params, String operation, Variant v) {
        String [] paramValues = getStringListParam(paramName, required, params, operation, v);
        if( ! required && (paramValues.length == 0) ) { 
            return null;
        }
        if (paramValues.length != 1) {
            throw ExecutionServerRestOperationException.badRequest(
                    "One and only one '" + paramName + "' query parameter required for '" + operation
                    + "' operation (" + paramValues.length + " passed).", 
                    v);
        }
        return paramValues[0];
    }

    private static final String [] EMPTY_STRING_ARR = new String[0];
    
    protected static List<String> getStringListParamAsList(String paramName, boolean required, Map<String, String[]> params, String operation, Variant v) {
        String [] strList = getStringListParam(paramName, required, params, operation, v);
        if( strList.length == 0 ) { 
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(strList);
    }
    
    protected static String[] getStringListParam(String paramName, boolean required, Map<String, String[]> params, String operation, Variant v) {
        String[] paramValues = null;
        for (Entry<String, String[]> entry : params.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(paramName)) {
                paramValues = entry.getValue();
                break;
            }
        }
        if (paramValues == null) {
            if (required) {
                throw ExecutionServerRestOperationException
                    .badRequest("Query parameter '" + paramName + "' required for '" + operation + "' operation.", 
                            v);
            }
            return EMPTY_STRING_ARR;
        }
        return paramValues;
    }

    
    protected static Object getObjectParam(String paramName, boolean required, Map<String, String[]> params, String operation, Variant v) {
        String paramVal = getStringParam(paramName, required, params, operation, v);
        if (!required && paramVal == null) {
            return null;
        }
        return getObjectFromString(paramName, paramVal, v);

    }

    protected static List<Long> getLongListParam(String paramName, boolean required, Map<String, String[]> params, String operation,
            boolean mustBeLong, Variant v) {
        String [] paramValues = getStringListParam(paramName, required, params, operation, v);
        List<Long> longValues = new ArrayList<Long>();
        for( String strVal : paramValues ) { 
           longValues.add((Long) getNumberFromString(paramName, strVal, mustBeLong, v));
        }
        return longValues;
    }
    
    protected static Number getNumberParam(String paramName, boolean required, Map<String, String[]> params, String operation,
            boolean mustBeLong, Variant v) {
        String paramVal = getStringParam(paramName, required, params, operation, v);
        if (!required && paramVal == null) {
            return null;
        }
        return getNumberFromString(paramName, paramVal, mustBeLong, v);
    }

    private static Object getObjectFromString(String key, String mapVal, Variant v) {
        if (mapVal.matches("^\".*\"$")) {
            return mapVal.substring(1, mapVal.length()-1);
            // TODO: float support
        } else if (!mapVal.matches("^\\d+[li]?$")) {
            return mapVal;
            
            // TODO: boolean support
        } else {
            return getNumberFromString(key, mapVal, false, v);
        }
    }

    /**
     * Returns a Long if no suffix is present.
     * Otherwise, possible suffixes are:
     * <ul>
     * <li>i : returns an Integer</li>
     * <li>l : returns an Long</li>
     * </ul>
     * 
     * @param paramName
     * @param paramVal
     * @return
     */
    private static Number getNumberFromString(String paramName, String paramVal, boolean mustBeLong, Variant v) {
        if (paramVal.matches("^\\d+[li]?$")) {
            if (paramVal.matches(".*i$")) {
                if (mustBeLong) {
                    throw ExecutionServerRestOperationException.badRequest( paramName 
                            + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
                            + paramVal + ")", v);
                }
                paramVal = paramVal.substring(0, paramVal.length() - 1);
                if (paramVal.length() > 9) {
                    throw ExecutionServerRestOperationException.badRequest(paramName + " parameter is numerical but too large to be an integer ("
                            + paramVal + "i)", v);
                }
                return Integer.parseInt(paramVal);
                // TODO: support for floats
//            } else if (paramVal.matches(".*f$")) {
//                if (mustBeLong) {
//                    throw ExecutionServerRestOperationException.badRequest( paramName 
//                            + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
//                            + paramVal + ")", v);
//                }
//                paramVal = paramVal.substring(0, paramVal.length() - 1);
//                if (paramVal.length() > 9) {
//                    throw ExecutionServerRestOperationException.badRequest(paramName + " parameter is numerical but too large to be an integer ("
//                            + paramVal + "i)", v);
//                }
//                return Integer.parseInt(paramVal);
            } else {
                if (paramVal.length() > 18) {
                    throw ExecutionServerRestOperationException.badRequest(paramName + " parameter is numerical but too large to be a long ("
                            + paramVal + ")", v);
                }
                if (paramVal.matches(".*l$")) {
                    paramVal = paramVal.substring(0, paramVal.length() - 1);
                }
                return Long.parseLong(paramVal);
            }
        }
        throw ExecutionServerRestOperationException.badRequest(paramName + " parameter does not have a numerical format (" + paramVal + ")", v);
    }
}
