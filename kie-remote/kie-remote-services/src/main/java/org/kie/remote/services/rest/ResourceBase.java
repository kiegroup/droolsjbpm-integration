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

import static org.kie.remote.common.rest.RestEasy960Util.defaultVariant;
import static org.kie.remote.common.rest.RestEasy960Util.getVariant;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.remote.services.rest.query.RemoteServicesQueryJPAService;
import org.kie.services.client.serialization.jaxb.impl.JaxbPaginatedList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbArray;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbBoolean;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbByte;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbCharacter;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbDouble;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbFloat;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbInteger;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbList;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbLong;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbMap;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbSet;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbShort;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceBase {

    protected static final Logger logger = LoggerFactory.getLogger(ResourceBase.class);

    @Inject
    protected ProcessRequestBean processRequestBean;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpServletRequest httpRequest;

    @Inject
    protected Instance<UserGroupCallback> userGroupCallbackInstance;

    private UserGroupCallback userGroupCallback;


    // for use in tests

    public void setProcessRequestBean( ProcessRequestBean processRequestBean ) {
        this.processRequestBean = processRequestBean;
    }

    void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * In order to be able to inject a mock instance for tests.
     * @param httpRequest
     */
    public void setHttpServletRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public void setUserGroupCallback(UserGroupCallback userGroupCallback) {
        this.userGroupCallback = userGroupCallback;
    }

    // Any query parameters used in REST calls (besides the query operations), should be added here

    static String PAGE_LONG_PARAM = "page";
    static String PAGE_SHORT_PARAM = "p";
    static String SIZE_LONG_PARAM = "pagesize";
    static String SIZE_SHORT_PARAM = "s";

    public static Set<String> paginationParams = new HashSet<String>();
    static {
        paginationParams.add(PAGE_LONG_PARAM);
        paginationParams.add(PAGE_SHORT_PARAM);
        paginationParams.add(SIZE_LONG_PARAM);
        paginationParams.add(SIZE_SHORT_PARAM);
    };

    protected static Map<Class, Class> wrapperPrimitives = new HashMap<Class, Class>();
    static {
        wrapperPrimitives.put(Boolean.class, JaxbBoolean.class);
        wrapperPrimitives.put(Byte.class, JaxbByte.class);
        wrapperPrimitives.put(Character.class, JaxbCharacter.class);
        wrapperPrimitives.put(Short.class, JaxbShort.class);
        wrapperPrimitives.put(Integer.class, JaxbInteger.class);
        wrapperPrimitives.put(Long.class, JaxbLong.class);
        wrapperPrimitives.put(Double.class, JaxbDouble.class);
        wrapperPrimitives.put(Float.class, JaxbFloat.class);
        if (Boolean.getBoolean("org.kie.remote.wrap.string")) {
            wrapperPrimitives.put(String.class, JaxbString.class);
        }
        wrapperPrimitives.put(List.class, JaxbList.class);
        wrapperPrimitives.put(Set.class, JaxbSet.class);
        wrapperPrimitives.put(Map.class, JaxbMap.class);
    }

    public static final String PROC_INST_ID_PARAM_NAME = "runtimeProcInstId";

    public AuditLogService getAuditLogService() {
        return processRequestBean.getAuditLogService();
    }

    public RemoteServicesQueryJPAService getJPAService() {
        return processRequestBean.getJPAService();
    }

    public UserGroupCallback getUserGroupCallback() {
        if (userGroupCallback == null) {
            userGroupCallback = safeGet(userGroupCallbackInstance);
        }

        return userGroupCallback;
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
         Map<String, String[]> params = httpRequest.getParameterMap();
         if( params == null ) {
            params = new HashMap<String, String[]>(0);
         }
         else {
             params = new HashMap<String, String[]>(params);
         }
         return params;
    }

    protected static String getStringParam(String paramName, boolean required, Map<String, String[]> params, String operation) {
        String [] paramValues = getStringListParam(paramName, required, params, operation);
        if( ! required && (paramValues.length == 0) ) {
            return null;
        }
        if (paramValues.length != 1) {
            throw KieRemoteRestOperationException.badRequest("One and only one '" + paramName + "' query parameter required for '" + operation
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
                throw KieRemoteRestOperationException.badRequest("Query parameter '" + paramName + "' required for '" + operation
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

    private final static int MAX_LENGTH_INT = 9;
    private final static int MAX_LENGTH_LONG = 18;
    private final static int MAX_LENGTH_FLOAT = 10;

    public static String LONG_INTEGER_REGEX ="^\\d+[li]?$";
    public static String FLOAT_REGEX = "^\\d[\\d\\.]{1,9}(E-?\\d{1,2})?f?$";

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
        if (paramVal.matches(LONG_INTEGER_REGEX)) {
            if (paramVal.matches(".*i$")) {
                if (mustBeLong) {
                    throw KieRemoteRestOperationException.badRequest( paramName
                            + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
                            + paramVal + ")");
                }
                paramVal = paramVal.substring(0, paramVal.length() - 1);
                if (paramVal.length() > MAX_LENGTH_INT) {
                    throw KieRemoteRestOperationException.badRequest(paramName + " parameter is numerical but too large to be an integer ("
                            + paramVal + "i)");
                }
                return Integer.parseInt(paramVal);
            } else {
                if (paramVal.length() > MAX_LENGTH_LONG) {
                    throw KieRemoteRestOperationException.badRequest(paramName + " parameter is numerical but too large to be a long ("
                            + paramVal + ")");
                }
                if (paramVal.matches(".*l$")) {
                    paramVal = paramVal.substring(0, paramVal.length() - 1);
                }
                return Long.parseLong(paramVal);
            }
        } else if(paramVal.matches(FLOAT_REGEX)) {
            if (mustBeLong) {
                throw KieRemoteRestOperationException.badRequest( paramName
                        + " parameter is numerical but contains the \"Integer\" suffix 'i' and must have no suffix or \"Long\" suffix 'l' ("
                        + paramVal + ")");
            }
            if (paramVal.matches(".*f$")) {
                paramVal = paramVal.substring(0, paramVal.length() - 1);
            }
            return Float.parseFloat(paramVal);
        }
        throw KieRemoteRestOperationException.badRequest(paramName + " parameter does not have a numerical format (" + paramVal + ")");
    }

    public static final String CORR_KEY_SHORT_QUERY_PARAM_PREFIX = "corrProp";

    protected static List<String> getCorrelationKeyProperties(Map<String, String[]> params) {
        List<String> correlationKeyProperties = null;

        for (Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] paramValues = entry.getValue();
            if (key.equals(CORR_KEY_SHORT_QUERY_PARAM_PREFIX) ) {
                if( correlationKeyProperties == null ) {
                    correlationKeyProperties = new ArrayList<String>(Arrays.asList(paramValues));
                } else {
                    correlationKeyProperties.addAll(Arrays.asList(paramValues));
                }
            }
        }
        return correlationKeyProperties;
    }

    public static final String MAP_QUERY_PARAM_PREFIX = "map_";

    protected static Map<String, Object> extractMapFromParams(Map<String, String[]> params, String operation) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Entry<String, String[]> entry : params.entrySet()) {
            if (entry.getKey().startsWith(MAP_QUERY_PARAM_PREFIX)) {
                String key = entry.getKey();
                String[] paramValues = entry.getValue();
                if (paramValues.length != 1) {
                    throw KieRemoteRestOperationException.badRequest("Only one map_* (" + key + ") query parameter allowed for '" + operation
                            + "' operation (" + paramValues.length + " passed).");
                }
                String mapKey = key.substring(MAP_QUERY_PARAM_PREFIX.length());
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
            throw KieRemoteRestOperationException.badRequest("At least 1 query parameter (either 'user' or 'group') is required for the '" + operation + "' operation.");
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
                    throw KieRemoteRestOperationException.badRequest(statusStr + " is not a valid status type for a task." );
                }
            }
        }
        return statuses;
    }

    // Pagination ----------------------------------------------------------------------------------------------------------------

    static int PAGE_NUM = 0;
    static int PAGE_SIZE = 1;

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

        // if the page size is 0, we ignore the page number
        if( s == 0 ) {
            p = 0;
        }

        pageInfo[PAGE_NUM] = p;
        pageInfo[PAGE_SIZE] = s;

        return pageInfo;
    }

    protected static <T> List<T> paginate(int[] pageInfo, List<T> results) {
        List<T> pagedResults = new ArrayList<T>();
        assert pageInfo[0] >= 0;
        if( pageInfo[0] == 0 && pageInfo[1] > 0 ) {
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

    public static int getMaxNumResultsNeeded(int [] pageInfo) {
        int numResults = pageInfo[PAGE_NUM]*pageInfo[PAGE_SIZE];
        if( numResults == 0 ) {
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
        url = url.replaceAll( ".*/rest", "");
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
           throw new KieRemoteServicesInternalError("Unable to determine Status for value '"  + value + "'", iae);
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

    // TODO: shouldn't this also take a process runtime id for per-process runtimes?
    public <T> T doRestTaskOperation(TaskCommand<T> cmd) {
        return processRequestBean.doRestTaskOperation(null, null, null, null, cmd);
    }

    protected <T> T doRestTaskOperationWithTaskId(Long taskId, TaskCommand<T> cmd) {
        return processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
    }

    protected <T> T doRestTaskOperationWithDeploymentId(String deploymentId, TaskCommand<T> cmd) {
        return processRequestBean.doRestTaskOperation(null, deploymentId, null, null, cmd);
    }

    public static boolean isPrimitiveOrWrapper(final Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive()
                || wrapperPrimitives.containsKey(type)
                || Map.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type)
                || type.isArray();
    }

    static Object wrapObjectIfNeeded(final Object processVariableObject) {
        // handle primitives and their wrappers
        if (processVariableObject != null && isPrimitiveOrWrapper(processVariableObject.getClass())) {
            return wrapPrimitive(processVariableObject);
        }
        return processVariableObject;
    }

    private static Object wrapPrimitive(final Object value) {
        // TODO: null check?
        Class wrapperTypeClass = value.getClass();
        if( value instanceof Set ) {
            wrapperTypeClass = Set.class;
        } else if( value instanceof List ) {
            wrapperTypeClass = List.class;
        } else if( value instanceof Map ) {
            wrapperTypeClass = Map.class;
        }
        Class<?> wrapperClass = wrapperPrimitives.get(wrapperTypeClass);
        if( wrapperTypeClass.isArray() ) {
            wrapperClass = JaxbArray.class;
        }
        try {
            Constructor [] cntrs = wrapperClass.getConstructors();
            Constructor argCntr = null;
            for( Constructor cntr : cntrs ) {
                if( cntr.getParameterTypes().length == 1 ) {
                    argCntr = cntr;
                    break;
                }
            }
            if( argCntr == null ) {
                throw new RuntimeException("Could not find 1 argument constructor for " + wrapperClass.getSimpleName());
            }
            return argCntr.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create " + wrapperClass.getSimpleName() + " for type " + value.getClass() + " with value " + value, e);
        }
    }

    protected <T> T safeGet( Instance<T> instance ) {
        try {
            T object = instance.get();
            logger.debug( "About to set object {} on task service", object );
            return object;
        } catch ( AmbiguousResolutionException e ) {
            // special handling in case cdi discovered multiple injections
            // that are actually same instances - e.g. weld on tomcat
            HashSet<T> available = new HashSet<T>();

            for ( T object : instance ) {
                available.add( object );
            }

            if ( available.size() == 1 ) {
                return available.iterator().next();
            } else {
                throw e;
            }
        } catch ( Throwable e ) {
            logger.debug( "Cannot get value of of instance {} due to {}", instance, e.getMessage() );
        }

        return null;
    }
}
