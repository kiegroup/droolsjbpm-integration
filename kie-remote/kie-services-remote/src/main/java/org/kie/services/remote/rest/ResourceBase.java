package org.kie.services.remote.rest;

import static org.kie.services.client.api.command.AcceptedCommands.TASK_COMMANDS_THAT_INFLUENCE_KIESESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jboss.resteasy.core.request.ServerDrivenNegotiation;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBase {

    protected static final Logger logger = LoggerFactory.getLogger(ResourceBase.class);
    
    protected static final String PROC_INST_ID_PARAM_NAME = "runtimeProcInstId";
    
    // Seam-Transaction ----------------------------------------------------------------------------------------------------------
    
    public static JaxbCommandsResponse restProcessJaxbCommandsRequest(JaxbCommandsRequest request, RestProcessRequestBean requestBean) {
        // If exceptions are happening here, then there is something REALLY wrong and they should be thrown.
        JaxbCommandsResponse jaxbResponse = new JaxbCommandsResponse(request);
        List<Command<?>> commands = request.getCommands();

        if (commands != null) {
            int cmdListSize = commands.size(); 
            for (int i = 0; i < cmdListSize; ++i) {
                Command<?> cmd = commands.get(i);
                if (!AcceptedCommands.getSet().contains(cmd.getClass())) {
                    throw new NotAcceptableException("The execute REST operation does not accept " + cmd.getClass().getName() + " instances.");
                }
                logger.debug("Processing command " + cmd.getClass().getSimpleName());
                Object cmdResult = null;
                try { 
                    String errorMsg = "Unable to execute " + cmd.getClass().getSimpleName() + "/" + i;
                    if (cmd instanceof TaskCommand<?>) {
                        TaskCommand<?> taskCmd = (TaskCommand<?>) cmd;
                        if( TASK_COMMANDS_THAT_INFLUENCE_KIESESSION.contains(cmd.getClass()) ) { 
                            cmdResult = requestBean.doTaskOperationOnDeployment(
                                    taskCmd, 
                                    request.getDeploymentId(),
                                    request.getProcessInstanceId(),
                                    errorMsg);
                        } else { 
                            cmdResult = requestBean.doTaskOperationAndSerializeResult(taskCmd, errorMsg);
                        }
                    } else {
                        cmdResult = requestBean.doKieSessionOperation(
                                cmd, 
                                request.getDeploymentId(), 
                                request.getProcessInstanceId(),
                                errorMsg);
                    }
                } catch(Exception e) { 
                    jaxbResponse.addException(e, i, cmd);
                    logger.warn("Unable to execute " + cmd.getClass().getSimpleName() + "/" + i
                            + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                }
                if (cmdResult != null) {
                    try {
                        // addResult could possibly throw an exception, which is why it's here and not above
                        jaxbResponse.addResult(cmdResult, i, cmd);
                    } catch (Exception e) {
                        logger.error("Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i 
                                + " because of " + e.getClass().getSimpleName(), e);
                        jaxbResponse.addException(e, i, cmd);
                    }
                }
            }
        }

        if (commands == null || commands.isEmpty()) {
            logger.info("Commands request object with no commands sent!");
        }

        return jaxbResponse;
    }
    
    // JSON / JAXB ---------------------------------------------------------------------------------------------------------------
    
    private static List<Variant> variants 
        = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE, MediaType.APPLICATION_JSON_TYPE).build();
    private static Variant defaultVariant 
        = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE).build().get(0);
    
    public static Variant getVariant(HttpHeaders headers) { 
        // copied (except for the acceptHeaders fix) from RestEasy's RequestImpl class
        ServerDrivenNegotiation negotiation = new ServerDrivenNegotiation();
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        List<String> acceptHeaders = requestHeaders.get(HttpHeaderNames.ACCEPT);
        if( acceptHeaders != null && ! acceptHeaders.isEmpty() ) { 
            List<String> fixedAcceptHeaders = new ArrayList<String>();
            for(String header : acceptHeaders ) { 
                fixedAcceptHeaders.add(header.replaceAll("q=\\.", "q=0.")); 
            }
            acceptHeaders = fixedAcceptHeaders;
        }
        negotiation.setAcceptHeaders(acceptHeaders);
        negotiation.setAcceptCharsetHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_CHARSET));
        negotiation.setAcceptEncodingHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_ENCODING));
        negotiation.setAcceptLanguageHeaders(requestHeaders.get(HttpHeaderNames.ACCEPT_LANGUAGE));

        return negotiation.getBestMatch(variants);
        // ** use below instead of above when RESTEASY-960 is fixed **
        // return restRequest.selectVariant(variants); 
    }
    
    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers) { 
        Variant v = getVariant(headers);
        if( v != null ) { 
            return Response.ok(responseObj, v).build();
        } else {
            return Response.ok(responseObj, defaultVariant).build();
        } 
    }
    
    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status) { 
        Variant v = getVariant(headers);
        if( v != null ) { 
            return Response.status(status).entity(responseObj).variant(v).build();
        } else {
            return Response.ok(responseObj, defaultVariant).build();
        } 
    }
    

    // Request Params -------------------------------------------------------------------------------------------------------------
    
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
        for (Entry<String, List<String>> entry : params.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(paramName)) {
                paramValues = entry.getValue();
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

        for (Entry<String, List<String>> entry : params.entrySet()) {
            if (entry.getKey().startsWith("map_")) {
                String key = entry.getKey();
                List<String> paramValues = entry.getValue();
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

    protected static List<OrganizationalEntity> getOrganizationalEntityListFromParams(Map<String, List<String>> params, boolean required, String operation) {
        List<OrganizationalEntity> orgEntList = new ArrayList<OrganizationalEntity>();

        List<String> users = getStringListParam("user", false, params, "nominate");
        List<String> groups = getStringListParam("group", false, params, "nominate");
        if (required && (users.isEmpty() && groups.isEmpty()) ) {
            throw new BadRequestException("At least 1 query parameter (either 'user' or 'group') is required for the '" + operation + "' operation.");
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
        List<Status> statuses = null;
        if( statusStrList != null && ! statusStrList.isEmpty() ) { 
            statuses = new ArrayList<Status>();
            for( String statusStr : statusStrList ) { 
                String goodStatusStr = statusStr.substring(0, 1).toUpperCase()
                        + statusStr.substring(1).toLowerCase();
                try { 
                    statuses.add(Status.valueOf(goodStatusStr));
                } catch(IllegalArgumentException iae) { 
                    throw new BadRequestException(goodStatusStr + " is not a valid status type for a task." );
                }
            }
        }
        return statuses;
    }
    
    // Pagination ----------------------------------------------------------------------------------------------------------------
    
    static int PAGE_NUM = 0;
    static int PAGE_SIZE = 1;
    
    protected static int [] getPageNumAndPageSize(Map<String, List<String>> params, String oper) {
        int [] pageInfo = new int[2];
        
        int p = 0;
        Number page = getNumberParam("page", false, params, oper, false);
        if( page != null ) { 
            p = page.intValue();
        } else { 
            Number pageShort = getNumberParam("p", false, params, oper, false);
            if( pageShort != null ) { 
                p = pageShort.intValue();
            }
        }
        if( p < 0 ) { 
            p = 0;
        }
        
        int s = 10;
        Number pageSize = getNumberParam("pageSize", false, params, oper, false);
        if( pageSize != null ) { 
            s = pageSize.intValue();
        } else { 
            Number pageSizeShort = getNumberParam("s", false, params, oper, false);
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
        if( pageInfo[0] == 0 ) { 
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
            numResults = Integer.MAX_VALUE;
        } 
        return numResults;
    }
    
    // Other helper methods ------------------------------------------------------------------------------------------------------
    
    protected String getRelativePath(HttpServletRequest request) { 
        String path = request.getRequestURL().toString();
        path = path.replaceAll( ".*" + request.getServletContext().getContextPath(), "");
        return path;
    }
    
}
