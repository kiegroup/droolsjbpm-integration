package org.kie.remote.services.rest;

import static org.kie.remote.common.rest.RestEasy960Util.defaultVariant;
import static org.kie.remote.common.rest.RestEasy960Util.getVariant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.QueryContextImpl;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.remote.common.exception.RestOperationException;
import org.kie.remote.services.AcceptedCommands;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBase {

    protected static final Logger logger = LoggerFactory.getLogger(ResourceBase.class);
    
    protected static final String PROC_INST_ID_PARAM_NAME = "runtimeProcInstId";
   
    @Inject
    protected ProcessRequestBean processRequestBean;
    
    @Context
    private UriInfo uriInfo;
    
    @Context
    private HttpServletRequest httpRequest;
  
    /**
     * In order to be able to inject a mock instance for tests.
     * @param httpRequest 
     */
    protected void setHttpServletRequest(HttpServletRequest httpRequest) { 
        this.httpRequest = httpRequest;
    }
    
     /**
      * In order to be able to inject a mock instance for tests.
      * @param uriInfo
      */
    protected void setUriInfo(UriInfo uriInfo) { 
        this.uriInfo = uriInfo;
    }
    
    // execute --------------------------------------------------------------------------------------------------------------------
    
    @SuppressWarnings("rawtypes")
    protected JaxbCommandsResponse restProcessJaxbCommandsRequest(JaxbCommandsRequest request) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command> commands = request.getCommands();

        if (commands != null) {
            int cmdListSize = commands.size(); 
           
            // First check to make sure that all commands will be processed
            for (int i = 0; i < cmdListSize; ++i) {
                Command<?> cmd = commands.get(i);
                if (!AcceptedCommands.getSet().contains(cmd.getClass())) {
                    throw RestOperationException.forbidden("The execute REST operation does not accept " + cmd.getClass().getName() + " instances.");
                }
            }
           
            // Execute commands
            for (int i = 0; i < cmdListSize; ++i) {
                Command<?> cmd = commands.get(i);
                processRequestBean.processCommand(cmd, request, i, jaxbResponse);
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }
    
    // JSON / JAXB ---------------------------------------------------------------------------------------------------------------
    
    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers) { 
        return createCorrectVariant(responseObj, headers, null);
    }
    
    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status) { 
        ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);
        if( v == null ) { 
            v = defaultVariant;
        }
        if( status != null ) { 
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else { 
            responseBuilder = Response.ok(responseObj, v);
        }
        return responseBuilder.build();
    }
    

    // Request Params -------------------------------------------------------------------------------------------------------------
    
    protected Map<String, String[]> getRequestParams() {
        return httpRequest.getParameterMap();
    }

    protected static String getStringParam(String paramName, boolean required, Map<String, String[]> params, String operation) {
        String [] paramValues = getStringListParam(paramName, required, params, operation);
        if( ! required && (paramValues.length == 0) ) { 
            return null;
        }
        if (paramValues.length != 1) {
            throw RestOperationException.badRequest("One and only one '" + paramName + "' query parameter required for '" + operation
                    + "' operation (" + paramValues.length + " passed).");
        }
        return paramValues[0];
    }

    private static final String [] EMPTY_STRING_ARR = new String[0];
    
    protected static List<String> getStringListParamAsList(String paramName, boolean required, Map<String, String[]> params, String operation) {
        String [] strList = getStringListParam(paramName, required, params, operation);
        if( strList.length == 0 ) { 
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(strList);
    }
    
    protected static String[] getStringListParam(String paramName, boolean required, Map<String, String[]> params, String operation) {
        String[] paramValues = null;
        for (Entry<String, String[]> entry : params.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(paramName)) {
                paramValues = entry.getValue();
                break;
            }
        }
        if (paramValues == null) {
            if (required) {
                throw RestOperationException.badRequest("Query parameter '" + paramName + "' required for '" + operation
                        + "' operation.");
            }
            return EMPTY_STRING_ARR;
        }
        return paramValues;
    }

    
    protected static Object getObjectParam(String paramName, boolean required, Map<String, String[]> params, String operation) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if (!required && paramVal == null) {
            return null;
        }
        return getObjectFromString(paramName, paramVal);

    }

    protected static List<Long> getLongListParam(String paramName, boolean required, Map<String, String[]> params, String operation,
            boolean mustBeLong) {
        String [] paramValues = getStringListParam(paramName, required, params, operation);
        List<Long> longValues = new ArrayList<Long>();
        for( String strVal : paramValues ) { 
           longValues.add((Long) getNumberFromString(paramName, strVal, mustBeLong));
        }
        return longValues;
    }
    
    protected static Number getNumberParam(String paramName, boolean required, Map<String, String[]> params, String operation,
            boolean mustBeLong) {
        String paramVal = getStringParam(paramName, required, params, operation);
        if (!required && paramVal == null) {
            return null;
        }
        return getNumberFromString(paramName, paramVal, mustBeLong);
    }

    private static Object getObjectFromString(String key, String mapVal) {
        if (mapVal.matches("^\".*\"$")) {
            return mapVal.substring(1, mapVal.length()-1);
        } else if (!mapVal.matches("^\\d+[li]?$")) {
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
                    throw RestOperationException.badRequest( paramName 
                            + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
                            + paramVal + ")");
                }
                paramVal = paramVal.substring(0, paramVal.length() - 1);
                if (paramVal.length() > 9) {
                    throw RestOperationException.badRequest(paramName + " parameter is numerical but too large to be an integer ("
                            + paramVal + "i)");
                }
                return Integer.parseInt(paramVal);
            } else {
                if (paramVal.length() > 18) {
                    throw RestOperationException.badRequest(paramName + " parameter is numerical but too large to be a long ("
                            + paramVal + ")");
                }
                if (paramVal.matches(".*l$")) {
                    paramVal = paramVal.substring(0, paramVal.length() - 1);
                }
                return Long.parseLong(paramVal);
            }
        }
        throw RestOperationException.badRequest(paramName + " parameter does not have a numerical format (" + paramVal + ")");
    }

    protected static Map<String, Object> extractMapFromParams(Map<String, String[]> params, String operation) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Entry<String, String[]> entry : params.entrySet()) {
            if (entry.getKey().startsWith("map_")) {
                String key = entry.getKey();
                String[] paramValues = entry.getValue();
                if (paramValues.length != 1) {
                    throw RestOperationException.badRequest("Only one map_* (" + key + ") query parameter allowed for '" + operation
                            + "' operation (" + paramValues.length + " passed).");
                }
                String mapKey = key.substring("map_".length());
                String mapVal = paramValues[0].trim();

                map.put(mapKey, getObjectFromString(key, mapVal));
            }
        }
        return map;
    }

    protected static List<OrganizationalEntity> getOrganizationalEntityListFromParams(Map<String, String[]> params, boolean required, String operation) {
        List<OrganizationalEntity> orgEntList = new ArrayList<OrganizationalEntity>();

        String [] users = getStringListParam("user", false, params, operation);
        String [] groups = getStringListParam("group", false, params, operation);
        if (required && (users.length == 0) && (groups.length == 0)) {
            throw RestOperationException.badRequest("At least 1 query parameter (either 'user' or 'group') is required for the '" + operation + "' operation.");
        }
        
        for( String user : users ) {
            User newuser = TaskModelProvider.getFactory().newUser();
            ((InternalOrganizationalEntity) newuser).setId(user);
            orgEntList.add(newuser);
        }
        for( String group : groups ) {
            Group newuser = TaskModelProvider.getFactory().newGroup();
            ((InternalOrganizationalEntity) newuser).setId(group);
            orgEntList.add(newuser);
        }
        
        return orgEntList;
    }
    
    protected static TaskSummaryImpl convertTaskToTaskSummary(InternalTask task) {
       TaskSummaryImpl taskSummary = new TaskSummaryImpl(
               task.getId().longValue(),
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
               task.getTaskData().getProcessInstanceId(),
               task.getTaskData().getDeploymentId(),
               task.getSubTaskStrategy(),
               task.getTaskData().getParentId()
               );
       return taskSummary;
    }
    
    protected static List<Status> convertStringListToStatusList( List<String> statusStrList ) { 
        List<Status> statuses = null;
        if( statusStrList != null && ! statusStrList.isEmpty() ) { 
            statuses = new ArrayList<Status>();
            for( String statusStr : statusStrList ) { 
                try { 
                    statuses.add(getEnum(statusStr));
                } catch(IllegalArgumentException iae) { 
                    throw RestOperationException.badRequest(statusStr + " is not a valid status type for a task." );
                }
            }
        }
        return statuses;
    }
    
    // Pagination ----------------------------------------------------------------------------------------------------------------
    
    static int PAGE_NUM = 0;
    static int PAGE_SIZE = 1;
   
    static String PAGE_LONG_PARAM = "page";
    static String PAGE_SHORT_PARAM = "p";
    static String SIZE_LONG_PARAM = "pageSize";
    static String SIZE_SHORT_PARAM = "s";
   
    static Set<String> paginationParams = new HashSet<String>();
    static { 
        paginationParams.add(PAGE_LONG_PARAM);
        paginationParams.add(PAGE_SHORT_PARAM);
        paginationParams.add(SIZE_LONG_PARAM);
        paginationParams.add(SIZE_SHORT_PARAM);
    };
    
    protected static int [] getPageNumAndPageSize(Map<String, String[]> params, String oper) {
        int [] pageInfo = new int[2];
        
        int p = 0;
        Number page = getNumberParam(PAGE_LONG_PARAM, false, params, oper, false);
        if( page != null ) { 
            p = page.intValue();
        } else { 
            Number pageShort = getNumberParam(PAGE_SHORT_PARAM, false, params, oper, false);
            if( pageShort != null ) { 
                p = pageShort.intValue();
            }
        }
        if( p < 0 ) { 
            p = 0;
        }
        
        int s = 0;
        Number pageSize = getNumberParam(SIZE_LONG_PARAM, false, params, oper, false);
        if( pageSize != null ) { 
            s = pageSize.intValue();
        } else { 
            Number pageSizeShort = getNumberParam(SIZE_SHORT_PARAM, false, params, oper, false);
            if( pageSizeShort != null ) { 
                s = pageSizeShort.intValue();
            }
        }
        if( s < 0 ) { 
            s = 0;
        }
        
        pageInfo[PAGE_NUM] = p;
        pageInfo[PAGE_SIZE] = s;
        
        return pageInfo;
    }
   
    protected static <T> List<T> paginate(int[] pageInfo, List<T> results) { 
        List<T> pagedResults = new ArrayList<T>();
        assert pageInfo[0] >= 0;
        if( pageInfo[1] > 0 && pageInfo[0] == 0 ) { 
            pageInfo[0] = 1;
        }
        if( pageInfo[0] == 0 && pageInfo[1] == 0) { 
            return results;
        }  else if( pageInfo[0] > 0 ) { 
            // for( i  = start of page; i < start of next page && i < num results; ++i ) 
            for( int i = (pageInfo[0]-1)*pageInfo[1]; i < pageInfo[0]*pageInfo[1] && i < results.size(); ++i ) { 
                pagedResults.add(results.get(i));
            }
        }
        return pagedResults;
    }
    
    protected static int getMaxNumResultsNeeded(int [] pageInfo) { 
        int numResults = pageInfo[PAGE_NUM]*pageInfo[PAGE_SIZE];
        if( pageInfo[PAGE_NUM] == 0 ) { 
            numResults = 1000;
        } 
        return numResults;
    }
   
    protected static <T, R extends JaxbPaginatedList<T>> R 
        paginateAndCreateResult(Map<String, String[]> params, String oper, List<T> results, R resultList) { 
        
        // paginate
        int [] pageInfo = getPageNumAndPageSize(params, oper); 
        return paginateAndCreateResult(pageInfo, results, resultList);
    }
        
    protected static <T, R extends JaxbPaginatedList<T>> R 
        paginateAndCreateResult(int [] pageInfo, List<T> results, R resultList) { 
        
        if( pageInfo[0] == 0 && pageInfo[1] == 0 ) { 
            // no pagination
            resultList.addContents(results);
            return resultList;
        }
        
        results = paginate(pageInfo, results);
       
        // create result
        resultList.addContents(results);
        resultList.setPageNumber(pageInfo[PAGE_NUM]);
        resultList.setPageSize(pageInfo[PAGE_SIZE]);
        
        return resultList;
    }
    // URL/Context helper methods -------------------------------------------------------------------------------------------------
    
    protected String getBaseUri() { 
        return uriInfo.getBaseUri().toString();
    }
    
    protected String getRequestUri() { 
        return httpRequest.getRequestURI();
    }
    
    protected String getRelativePath() { 
        String url =  httpRequest.getRequestURI();
        url.replaceAll( ".*/rest", "");
        return url;
    }
    
    // Other helper methods ------------------------------------------------------------------------------------------------------
    
    protected static Status getEnum(String value) {
        value = value.substring(0,1).toUpperCase() + value.substring(1).toLowerCase();

        try { 
            return Status.valueOf(value);
        } catch( IllegalArgumentException iae ) { 
           if( value.equalsIgnoreCase("inprogress") )  { 
               return Status.InProgress;
           }
           throw iae;
        }
    }
  
    protected JaxbProcessDefinition convertProcAssetDescToJaxbProcDef(ProcessDefinition procAssetDesc) {
        JaxbProcessDefinition jaxbProcDef = new JaxbProcessDefinition(); 
        jaxbProcDef.setDeploymentId(((ProcessAssetDesc)procAssetDesc).getDeploymentId());
        jaxbProcDef.setForms(((ProcessAssetDesc)procAssetDesc).getForms());
        jaxbProcDef.setId(procAssetDesc.getId());
        jaxbProcDef.setName(procAssetDesc.getName());
        jaxbProcDef.setPackageName(procAssetDesc.getPackageName());
        jaxbProcDef.setVersion(procAssetDesc.getVersion());
        
        return jaxbProcDef;
    }
   
}
