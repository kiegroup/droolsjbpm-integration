package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.services.remote.rest.exception.IncorrectRequestException;

public class ResourceBase {

    protected static String checkThatOperationExists(String operation, String[] possibleOperations) {
        for (String oper : possibleOperations) {
            if (oper.equals(operation.trim().toLowerCase())) {
                return oper;
            }
        }
        throw new IncorrectRequestException("Operation '" + operation + "' is not supported on tasks.");
    }

    protected static Map<String, List<String>> getRequestParams(HttpServletRequest request) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        Enumeration<String> names = request.getParameterNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            parameters.put(name, Arrays.asList(request.getParameterValues(name)));
        }
        
        return parameters;
    }

    protected static String getStringParam(String paramName, boolean required, Map<String, List<String>> params,
            String operation) {
        List<String> paramValues = null;
        for (String key : params.keySet()) {
            if (key.equalsIgnoreCase(paramName)) {
                paramValues = params.get(key);
                break;
            }
        }
        if (paramValues == null) {
            if (required) {
                throw new IncorrectRequestException("Query parameter '" + paramName + "' required for '" + operation
                        + "' operation.");
            }
            return null;
        }
        if (paramValues.size() != 1) {
            throw new IncorrectRequestException("One and only one '" + paramName + "' query parameter required for '" + operation
                    + "' operation (" + paramValues.size() + " passed).");
        }
        return paramValues.get(0);
    }

    protected static Object getObjectParam(String paramName, boolean required, Map<String, List<String>> params,
            String operation) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if( !required && paramVal == null ) { 
            return null;
        }
        return getObjectFromString(paramName, paramVal);
    
    }

    protected static Object getNumberParam(String paramName, boolean required, Map<String, List<String>> params,
            String operation) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if( !required && paramVal == null ) { 
            return null;
        }
        return getNumberFromString(paramName, paramVal);
    }
    
    private static Object getObjectFromString(String key, String mapVal) {
        if (!mapVal.matches("^\\d+[li]?$")) {
            return mapVal;
        } else { 
            return getNumberFromString(key, mapVal); 
        }
    }
    
    private static Number getNumberFromString(String paramName, String paramVal) {
        if ( paramVal.matches("^\\d+[li]?$")) {
            if (paramVal.matches(".*l$")) {
                if (paramVal.length() > 19) {
                    throw new IncorrectRequestException( paramName + " parameter is numerical but too large to be a long (" + paramVal + ")");
                }
                return Long.parseLong(paramVal.substring(0, paramVal.length() - 1));
            } else {
                if (paramVal.matches(".*i$")) {
                    paramVal = paramVal.substring(0, paramVal.length() - 1);
                }
                if (paramVal.length() > 9) {
                    throw new IncorrectRequestException( paramName + " parameter is numerical but too large to be an integer (" + paramVal + ")");
                }
                return Integer.parseInt(paramVal);
            }
        }
        throw new IncorrectRequestException( paramName + " parameter does not have a numerical format (" + paramVal + ")");
    }

    protected static Map<String, Object> extractMapFromParams(Map<String, List<String>> params, String operation) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (String key : params.keySet()) {
            if (key.startsWith("map_")) {
                List<String> paramValues = params.get(key);
                if (paramValues.size() != 1) {
                    throw new IncorrectRequestException("Only one map_* (" + key + ") query parameter allowed for '" + operation
                            + "' operation (" + paramValues.size() + " passed).");
                }
                String mapKey = key.substring("map_".length());
                String mapVal = paramValues.get(0).trim();
    
                map.put(mapKey, getObjectFromString(key, mapVal));
            }
        }
        return map;
    }

    protected static List<OrganizationalEntity> getOrganizationalEntityListFromParams(Map<String, List<String>> params) {
        List<OrganizationalEntity> orgEntList = new ArrayList<OrganizationalEntity>();
    
        throw new UnsupportedOperationException("//TODO: getOrganizationalEntityListFromParams" );
    }
}
