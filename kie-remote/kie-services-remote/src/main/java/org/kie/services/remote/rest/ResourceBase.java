package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.spi.BadRequestException;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
public class ResourceBase {

    protected static String checkThatOperationExists(String operation, String[] possibleOperations) {
        for (String oper : possibleOperations) {
            if (oper.equals(operation.trim().toLowerCase())) {
                return oper;
            }
        }
        throw new BadRequestException("Operation '" + operation + "' is not supported on tasks.");
    }

    protected static Map<String, List<String>> getRequestParams(HttpServletRequest request) {
        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            parameters.put(name, Arrays.asList(request.getParameterValues(name)));
        }

        return parameters;
    }

    protected static String getStringParam(String paramName, boolean required, Map<String, List<String>> params, String operation) {
        List<String> paramValues = getStringListParam(paramName, required, params, operation);
        if( ! required && paramValues.isEmpty() ) { 
            return null;
        }
        if (paramValues.size() != 1) {
            throw new BadRequestException("One and only one '" + paramName + "' query parameter required for '" + operation
                    + "' operation (" + paramValues.size() + " passed).");
        }
        return paramValues.get(0);
        
    }

    protected static List<String> getStringListParam(String paramName, boolean required, Map<String, List<String>> params, String operation) {
        List<String> paramValues = null;
        for (String key : params.keySet()) {
            if (key.equalsIgnoreCase(paramName)) {
                paramValues = params.get(key);
                break;
            }
        }
        if (paramValues == null) {
            if (required) {
                throw new BadRequestException("Query parameter '" + paramName + "' required for '" + operation
                        + "' operation.");
            }
            return new ArrayList<String>();
        }
        return paramValues;
    }

    
    protected static Object getObjectParam(String paramName, boolean required, Map<String, List<String>> params, String operation) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if (!required && paramVal == null) {
            return null;
        }
        return getObjectFromString(paramName, paramVal);

    }

    protected static List<Long> getLongListParam(String paramName, boolean required, Map<String, List<String>> params, String operation,
            boolean mustBeLong) {
        List<String> paramValues = getStringListParam(paramName, required, params, operation);
        List<Long> longValues = new ArrayList<Long>();
        for( String strVal : paramValues ) { 
           longValues.add((Long) getNumberFromString(paramName, strVal, mustBeLong));
        }
        return longValues;
    }
    
    protected static Number getNumberParam(String paramName, boolean required, Map<String, List<String>> params, String operation,
            boolean mustBeLong) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if (!required && paramVal == null) {
            return null;
        }
        return getNumberFromString(paramName, paramVal, mustBeLong);
    }

    private static Object getObjectFromString(String key, String mapVal) {
        if (!mapVal.matches("^\\d+[li]?$")) {
            return mapVal;
        } else {
            return getNumberFromString(key, mapVal, false);
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
    private static Number getNumberFromString(String paramName, String paramVal, boolean mustBeLong) {
        if (paramVal.matches("^\\d+[li]?$")) {
            if (paramVal.matches(".*i$")) {
                if (mustBeLong) {
                    throw new BadRequestException( paramName 
                            + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
                            + paramVal + ")");
                }
                paramVal = paramVal.substring(0, paramVal.length() - 1);
                if (paramVal.length() > 9) {
                    throw new BadRequestException(paramName + " parameter is numerical but too large to be an integer ("
                            + paramVal + "i)");
                }
                return Integer.parseInt(paramVal);
            } else {
                if (paramVal.length() > 18) {
                    throw new BadRequestException(paramName + " parameter is numerical but too large to be a long ("
                            + paramVal + ")");
                }
                if (paramVal.matches(".*l$")) {
                    paramVal = paramVal.substring(0, paramVal.length() - 1);
                }
                return Long.parseLong(paramVal);
            }
        }
        throw new BadRequestException(paramName + " parameter does not have a numerical format (" + paramVal + ")");
    }

    protected static Map<String, Object> extractMapFromParams(Map<String, List<String>> params, String operation) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (String key : params.keySet()) {
            if (key.startsWith("map_")) {
                List<String> paramValues = params.get(key);
                if (paramValues.size() != 1) {
                    throw new BadRequestException("Only one map_* (" + key + ") query parameter allowed for '" + operation
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

        throw new UnsupportedOperationException("//TODO: getOrganizationalEntityListFromParams");
    }
    
    protected static TaskSummaryImpl convertTaskToTaskSummary(TaskImpl task) { 
       TaskSummaryImpl taskSummary = new TaskSummaryImpl(
               task.getId().longValue(),
               task.getTaskData().getProcessInstanceId(),
               task.getNames().get(0).getText(),
               task.getSubjects().get(0).getText(),
               task.getDescriptions().get(0).getText(),
               task.getTaskData().getStatus(),
               task.getPriority(),
               task.getTaskData().isSkipable(),
               task.getTaskData().getActualOwner(),
               task.getTaskData().getCreatedBy(),
               task.getTaskData().getCreatedOn(),
               task.getTaskData().getActivationTime(),
               task.getTaskData().getExpirationTime(),
               task.getTaskData().getProcessId(),
               task.getTaskData().getProcessSessionId(),
               task.getSubTaskStrategy(),
               task.getTaskData().getParentId()
               );
       return taskSummary;
    }
    
    protected static List<Status> convertStringListToStatusList( List<String> statusStrList ) { 
        List<Status> statusList = new ArrayList<Status>();
        for( String strStatus : statusStrList ) { 
            statusList.add(Status.valueOf(Status.class, strStatus));
        }
        return statusList;
    }
    
    protected static int [] getPageNumAndPageSize(Map<String, List<String>> params) {
        int [] pageInfo = new int[3];
        Number page = getNumberParam("page", false, params, "query", false);
        Number pageShort = getNumberParam("p", false, params, "query", false);
        Number pageSize = getNumberParam("pageSize", false, params, "query", false);
        Number pageSizeShort = getNumberParam("s", false, params, "query", false);
        
        int p = 1;
        int s = 10;
        if( page != null ) { 
            p = page.intValue();
        } else if( pageShort != null ) { 
            p = pageShort.intValue();
        }
        if( pageSize != null ) { 
            s = pageSize.intValue();
        } else if( pageSizeShort != null ) { 
            s = pageSizeShort.intValue();
        }
        
        pageInfo[0] = p;
        pageInfo[1] = s;
        pageInfo[2] = pageInfo[0] * pageInfo[1];
        
        return pageInfo;
    }
}
