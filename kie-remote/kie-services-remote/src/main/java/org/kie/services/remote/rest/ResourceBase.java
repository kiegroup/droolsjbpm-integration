package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.ExitTaskCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.SkipTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(ResourceBase.class);
    
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
                    if (cmd instanceof TaskCommand<?>) {
                        String errorMsg = "Unable to execute command " + cmd.getClass().getSimpleName();
                        TaskCommand<?> taskCmd = (TaskCommand<?>) cmd;
                        if( cmd instanceof CompleteTaskCommand
                            || cmd instanceof ExitTaskCommand
                            || cmd instanceof FailTaskCommand
                            || cmd instanceof SkipTaskCommand ) { 
                            cmdResult = requestBean.doTaskOperationOnDeployment(
                                    taskCmd, 
                                    errorMsg, 
                                    request.getDeploymentId());
                        } else { 
                            cmdResult = requestBean.doTaskOperation(taskCmd, errorMsg);
                        }
                    } else {
                        cmdResult = requestBean.doKieSessionOperation(
                                cmd, 
                                request.getDeploymentId(), 
                                request.getProcessInstanceId(),
                                "Unable to execute command " + cmd.getClass().getSimpleName());
                    }
                } catch(Exception e) { 
                    jaxbResponse.addException(e, i, cmd);
                    logger.warn("Unable to execute " + cmd.getClass().getSimpleName() 
                            + " because of " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    logger.trace("Stack trace: \n", e);
                }
                if (cmdResult != null) {
                    try {
                        // addResult could possibly throw an exception, which is why it's here and not above
                        jaxbResponse.addResult(cmdResult, i, cmd);
                    } catch (Exception e) {
                        logger.error("Unable to add result from " + cmd.getClass().getSimpleName() + "/" + i + " because of "
                                + e.getClass().getSimpleName(), e);
                        logger.trace("Stack trace: \n", e);
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
        = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).build().get(0);
    
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
        return createCorrectVariant(responseObj, headers, true);
    }
    
    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers, boolean useDefault) { 
        Variant v = getVariant(headers);
        if( v != null ) { 
            return Response.ok(responseObj, v).build();
        } else if( useDefault ) { 
            return Response.ok(responseObj, defaultVariant).build();
        } else {
            return Response.notAcceptable(variants).build();
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

        List<String> users = getStringListParam("user", true, params, "nominate");
        List<String> groups = getStringListParam("group", true, params, "nominate");

        for( String user : users ) { 
            orgEntList.add(new UserImpl(user));
        }
        for( String group : groups ) { 
            orgEntList.add(new GroupImpl(group));
        }
        
        return orgEntList;
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
