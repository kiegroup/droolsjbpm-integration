/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.client.internal.command;

import static org.kie.remote.client.internal.command.InternalJmsCommandHelper.internalExecuteJmsCommand;
import static org.kie.services.shared.ServicesVersion.VERSION;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Queue;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.internal.command.ProcessInstanceIdCommand;
import org.kie.internal.jaxb.CorrelationKeyXmlAdapter;
import org.kie.internal.jaxb.StringKeyObjectValueMap;
import org.kie.internal.process.CorrelationKey;
import org.kie.remote.client.api.exception.MissingRequiredInfoException;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.api.exception.RemoteTaskException;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.remote.jaxb.gen.AddContentCommand;
import org.kie.remote.jaxb.gen.AddContentFromUserCommand;
import org.kie.remote.jaxb.gen.AddTaskCommand;
import org.kie.remote.jaxb.gen.AuditCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.CompleteWorkItemCommand;
import org.kie.remote.jaxb.gen.Content;
import org.kie.remote.jaxb.gen.ExecuteTaskRulesCommand;
import org.kie.remote.jaxb.gen.FailTaskCommand;
import org.kie.remote.jaxb.gen.InsertObjectCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.ProcessSubTaskCommand;
import org.kie.remote.jaxb.gen.SetGlobalCommand;
import org.kie.remote.jaxb.gen.SetProcessInstanceVariablesCommand;
import org.kie.remote.jaxb.gen.SetTaskPropertyCommand;
import org.kie.remote.jaxb.gen.SignalEventCommand;
import org.kie.remote.jaxb.gen.StartCorrelatedProcessCommand;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.kie.remote.jaxb.gen.UpdateCommand;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.SerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRestRequestException;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic to interact with the REST or JMS api's. It is the basis for all of the remote interface instances.
 */
public abstract class AbstractRemoteCommandObject {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRemoteCommandObject.class);

    protected final RemoteConfiguration config;
    protected boolean isTaskService = false;

    protected AbstractRemoteCommandObject(RemoteConfiguration config) {
        this.config = config;
        if( config.isJms() && config.getResponseQueue() == null ) { 
            throw new MissingRequiredInfoException("A Response queue is necessary in order to create a Remote JMS Client instance.");
        }
        this.config.initializeJaxbSerializationProvider();
    }

    RemoteConfiguration getConfig() { 
       return config; 
    }

    // Client object helper methods -----------------------------------------------------------------------------------------------
  
    protected String convertCorrelationKeyToString(CorrelationKey correlationKey) { 
        try {
            return CorrelationKeyXmlAdapter.marshalCorrelationKey(correlationKey);
        } catch( Exception e ) {
            throw new RemoteApiException("Unable to marshal correlation key to a string value", e);
        }
    }
        
    // Compatibility methods -----------------------------------------------------------------------------------------------------

    public void readExternal( ObjectInput arg0 ) throws IOException, ClassNotFoundException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + Task.class.getSimpleName()
                + " implementation.");
    }

    public void writeExternal( ObjectOutput arg0 ) throws IOException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + Task.class.getSimpleName()
                + " implementation.");
    }

    // Execute methods -----------------------------------------------------------------------------------------------------

    protected <T> T executeCommand( Command cmd ) {
        if( AcceptedClientCommands.isSendObjectParameterCommandClass(cmd.getClass()) ) {
            List<Object> extraClassInstanceList = new ArrayList<Object>();
            preprocessParameterCommand(cmd, extraClassInstanceList);

            if( !extraClassInstanceList.isEmpty() ) {
                Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
                for( Object jaxbObject : extraClassInstanceList ) {
                    Class<?> jaxbClass = jaxbObject.getClass();
                    if( jaxbClass.isLocalClass() || jaxbClass.isAnonymousClass() ) {
                        throw new SerializationException(
                                "Only proper classes are allowed as parameters for the remote API: neither local nor anonymous classes are accepted: "
                                        + jaxbClass.getName());
                    }
                    extraJaxbClasses.add(jaxbClass);
                }
                if( config.addJaxbClasses(extraJaxbClasses) ) { 
                    for( Class<?> extraClass : extraJaxbClasses ) { 
                        logger.debug( "Adding {} to the JAXBContext instance in this client instance.", extraClass.getName() );
                    }
                    config.initializeJaxbSerializationProvider();
                }
            }
        }

        preprocessCommand(cmd);
       
        if( config.isRest() ) {
            return executeRestCommand(cmd);
        } else {
            return executeJmsCommand(cmd);
        }
    }
    
    void preprocessCommand( Command cmd ) {
        String cmdName = cmd.getClass().getSimpleName();
        if( cmd instanceof TaskCommand && cmdName.startsWith("GetTask") ) {
           TaskCommand taskCmd = (TaskCommand) cmd;
           String cmdUserId = taskCmd.getUserId();
           String authUserId = config.getUserName();
           if( cmdUserId == null ) { 
               taskCmd.setUserId(authUserId);
               logger.debug("Using user id '" + authUserId + "' for '" + cmdName + "'.");
           } else if( ! cmdUserId.equals(authUserId) ) {
              throw new RemoteApiException("The user id used when retrieving task information (" + cmdUserId + ")"
                      + " must match the authenticating user (" + authUserId + ")!");
           }
        } 
    }

    void preprocessParameterCommand( Object cmdObj, List<Object> extraClassInstanceList ) {
        if( cmdObj instanceof CompleteWorkItemCommand ) {
            addPossiblyNullObject(((CompleteWorkItemCommand) cmdObj).getResult(), extraClassInstanceList);
        } else if( cmdObj instanceof SignalEventCommand ) {
            addPossiblyNullObject(((SignalEventCommand) cmdObj).getEvent(), extraClassInstanceList);
        } else if( cmdObj instanceof StartCorrelatedProcessCommand ) {
            StartCorrelatedProcessCommand cmd = (StartCorrelatedProcessCommand) cmdObj;
            if( cmd.getData() != null ) { 
                addPossiblyNullObject(cmd.getData().getDatas(), extraClassInstanceList);
            }
            addPossiblyNullObject(cmd.getParameter(), extraClassInstanceList);
        } else if( cmdObj instanceof StartProcessCommand ) {
            StartProcessCommand startProcCmd = (StartProcessCommand) cmdObj;
            if( startProcCmd.getData() != null ) { 
                addPossiblyNullObject(startProcCmd.getData().getDatas(), extraClassInstanceList);
            } 
            addPossiblyNullObject(((StartProcessCommand) cmdObj).getParameter(), extraClassInstanceList);
        } else if( cmdObj instanceof SetGlobalCommand ) {
            addPossiblyNullObject(((SetGlobalCommand) cmdObj).getObject(), extraClassInstanceList);
        } else if( cmdObj instanceof InsertObjectCommand ) {
            addPossiblyNullObject(((InsertObjectCommand) cmdObj).getObject(), extraClassInstanceList);
        } else if( cmdObj instanceof UpdateCommand ) {
            addPossiblyNullObject(((UpdateCommand) cmdObj).getObject(), extraClassInstanceList);
        } else if( cmdObj instanceof AddTaskCommand ) {
            addPossiblyNullObject(((AddTaskCommand) cmdObj).getParameter(), extraClassInstanceList);
        } else if( cmdObj instanceof CompleteTaskCommand ) {
            addPossiblyNullObject(((CompleteTaskCommand) cmdObj).getData(), extraClassInstanceList);
        } else if( cmdObj instanceof FailTaskCommand ) {
            addPossiblyNullObject(((FailTaskCommand) cmdObj).getData(), extraClassInstanceList);
        } else if( cmdObj instanceof AddContentFromUserCommand ) {
            addPossiblyNullObject(((AddContentFromUserCommand) cmdObj).getOutputContentMap(), extraClassInstanceList);
        } else if( cmdObj instanceof AddContentCommand ) {
            AddContentCommand cmd = (AddContentCommand) cmdObj;
            addPossiblyNullObject(cmd.getParameter(), extraClassInstanceList);
            Content jaxbContent = cmd.getJaxbContent();
            if( jaxbContent != null ) { 
                addPossiblyNullObject(jaxbContent.getContentMap(), extraClassInstanceList);
            }
        } else if( cmdObj instanceof SetTaskPropertyCommand ) {
            addPossiblyNullObject(((SetTaskPropertyCommand) cmdObj).getOutput(), extraClassInstanceList);
        } else if( cmdObj instanceof ExecuteTaskRulesCommand ) {
            addPossiblyNullObject(((ExecuteTaskRulesCommand) cmdObj).getData(), extraClassInstanceList);
        } else if( cmdObj instanceof ProcessSubTaskCommand ) {
            addPossiblyNullObject(((ProcessSubTaskCommand) cmdObj).getData(), extraClassInstanceList);
        } else if( cmdObj instanceof SetProcessInstanceVariablesCommand ) {
            addPossiblyNullObject(((SetProcessInstanceVariablesCommand) cmdObj).getVariables(), extraClassInstanceList);
        }
    }

    void addPossiblyNullObject( Object inputObject, List<Object> objectList ) {
        internalAddPossiblyNullObject(inputObject, objectList, new IdentityHashMap<Object, Object>());
    }
   
    private static final Object PRESENT = new Object();
    
    void internalAddPossiblyNullObject( Object inputObject, List<Object> objectList, Map<Object, Object> seenObjectsMap ) {
        if( inputObject != null ) {
            if( seenObjectsMap.put(inputObject, PRESENT) != null ) { 
                return; 
            }
            if( inputObject instanceof List )  {
                for( Object obj : (List<?>)inputObject ) {
                    internalAddPossiblyNullObject(obj, objectList, seenObjectsMap);
                }
            } else if( inputObject instanceof JaxbStringObjectPairArray ) { 
                for( JaxbStringObjectPair stringObjectPair : ((JaxbStringObjectPairArray) inputObject).getItems() ) {
                    if( stringObjectPair != null ) {
                        internalAddPossiblyNullObject(stringObjectPair.getValue(), objectList, seenObjectsMap);
                    }
                }
            } else if( inputObject instanceof StringKeyObjectValueMap ) { 
                for( Object obj : ((StringKeyObjectValueMap) inputObject).values() ) {
                    internalAddPossiblyNullObject(obj, objectList, seenObjectsMap);
                } 
            } else {  
                objectList.add(inputObject);
            }
        }
    }

    static JaxbCommandsRequest prepareCommandRequest( 
            Command command, 
            String userName, 
            String deploymentId, 
            Long processInstanceId,
            Collection<String> correlationKeyProps) {
        
        if( deploymentId == null && !(command instanceof TaskCommand || command instanceof AuditCommand) ) {
            throw new MissingRequiredInfoException("A deployment id is required when sending commands involving the KieSession.");
        }
        JaxbCommandsRequest req;
        if( command instanceof AuditCommand ) {
            req = new JaxbCommandsRequest(command);
        } else {
            req = new JaxbCommandsRequest(deploymentId, command);
        }
        if( command instanceof TaskCommand ) { 
           TaskCommand taskCmd = (TaskCommand) command; 
           if( taskCmd.getUserId() == null ) { 
               taskCmd.setUserId(userName);
           }
        }

        if( processInstanceId != null ) { 
            if (command instanceof ProcessInstanceIdCommand) {
                processInstanceId = ((ProcessInstanceIdCommand) command).getProcessInstanceId();
            } 
        }
      
        if( correlationKeyProps != null && ! correlationKeyProps.isEmpty() ) {
            StringBuffer correlationKeyString = new StringBuffer();
            Iterator<String> iter = correlationKeyProps.iterator();
            correlationKeyString.append(iter.next());
            while( iter.hasNext() ) { 
                correlationKeyString.append(":").append(iter.next());
            }
            req.setCorrelationKeyString(correlationKeyString.toString());
        }
        
        req.setProcessInstanceId(processInstanceId);
        req.setUser(userName);
        req.setVersion(VERSION);

        return req;
    }

    /**
     * Method to communicate with the backend via JMS.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private <T> T executeJmsCommand( Command command ) {
       
        Queue sendQueue;
        boolean isTaskCommand = (command instanceof TaskCommand);
        if( isTaskCommand ) {
            sendQueue = config.getTaskQueue();
            if( ! config.getUseUssl() && ! config.getDisableTaskSecurity() ) {
                throw new RemoteCommunicationException("Task operation requests can only be sent via JMS if SSL is used.");
            }
        } else {
            sendQueue = config.getKsessionQueue();
        }
        
        return internalExecuteJmsCommand(command,
                config.getConnectionUserName(), config.getConnectionPassword(), 
                config.getUserName(), config.getPassword(), 
                config.getDeploymentId(), config.getProcessInstanceId(), config.getCorrelationProperties(),
                config.getConnectionFactory(), sendQueue, config.getResponseQueue(),
                (SerializationProvider) config.getJaxbSerializationProvider(), config.getExtraJaxbClasses(),
                config.getSerializationType(), config.getTimeout());

    }
   
    /**
     * Method to communicate with the backend via REST.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private <T> T executeRestCommand( Command command ) {
        String cmdName = command.getClass().getSimpleName();
        
        JaxbCommandsRequest jaxbRequest = prepareCommandRequest(
                command, 
                config.getUserName(), 
                config.getDeploymentId(), 
                config.getProcessInstanceId(), 
                config.getCorrelationProperties());
        KieRemoteHttpRequest httpRequest = config.createHttpRequest().relativeRequest("/execute");
        
        // necessary for deserialization
        String deploymentId = config.getDeploymentId();
        if( ! emptyDeploymentId(deploymentId) ) { 
            httpRequest.header(JaxbSerializationProvider.EXECUTE_DEPLOYMENT_ID_HEADER, deploymentId);
        }

        String jaxbRequestString = config.getJaxbSerializationProvider().serialize(jaxbRequest);
        if( logger.isTraceEnabled() ) {
            try {
                logger.trace("Sending {} via POST to {}", command.getClass().getSimpleName(), httpRequest.getUri());
            } catch( Exception e ) {
                // do nothing because this should never happen..
            }
            logger.trace("Serialized JaxbCommandsRequest:\n {}", jaxbRequestString);
        }

        KieRemoteHttpResponse httpResponse = null;
        try {
            logger.debug("Sending POST request with " + command.getClass().getSimpleName() + " to " + httpRequest.getUri());
            httpRequest.contentType(MediaType.APPLICATION_XML);
            httpRequest.accept(MediaType.APPLICATION_XML);
            httpRequest.body(jaxbRequestString);
            httpRequest.post();
            httpResponse = httpRequest.response();
        } catch( Exception e ) {
            httpRequest.disconnect();
            throw new RemoteCommunicationException("Unable to send HTTP POST request", e);
        } 
        
        // Get response
        boolean htmlException = false;
        JaxbExceptionResponse exceptionResponse = null;
        JaxbCommandsResponse cmdResponse = null;
        int responseStatus;
        try {
            responseStatus = httpResponse.code();
            if( responseStatus < 300 ) {
                String content = httpResponse.body();
                cmdResponse = deserializeResponseContent(content, JaxbCommandsResponse.class);

                // check version
                String version = cmdResponse.getVersion();
                if( version == null ) {
                    version = "pre-6.0.3";
                }
                if( !version.equals(VERSION) ) {
                    logger.info("Response received from server version [{}] while client is version [{}]! This may cause problems.",
                            version, VERSION);
                }
            } else {
                String contentType = httpResponse.contentType();
                if( contentType.equals(MediaType.APPLICATION_XML) ) { 
                    String content = httpResponse.body();
                    Object response = deserializeResponseContent(content, JaxbExceptionResponse.class);
                    if( response instanceof JaxbRestRequestException ) { 
                        JaxbRestRequestException exception = (JaxbRestRequestException) response;
                        exceptionResponse = new JaxbExceptionResponse(
                                exception.getUrl(),
                                exception.getCause(),
                                exception.getStatus());
                        exceptionResponse.setCommandName(cmdName);
                        exceptionResponse.setIndex(0);
                        exceptionResponse.setMessage(exception.getMessage());
                    } else if( response instanceof JaxbExceptionResponse ) {
                        exceptionResponse = (JaxbExceptionResponse) response;
                    }
                } else if( contentType.startsWith(MediaType.TEXT_HTML) ) { 
                    String content = httpResponse.body();
                    htmlException = true;
                    exceptionResponse = new JaxbExceptionResponse();
                    Document doc = Jsoup.parse(content);
                    String body = doc.body().text();
                    exceptionResponse.setMessage(body);
                    exceptionResponse.setUrl(httpRequest.getUri().toString());
                    exceptionResponse.setStackTrace("");
                } else { 
                    if( responseStatus == 401 ) { 
                        String user = config.getUserName();
                        throw new RemoteApiException("User '" + user + "'is not authorized for the /rest/execute operation: does '" + user + "' have the 'rest-client' or 'rest-all' role?");
                    }
                    throw new RemoteCommunicationException("Unable to deserialize response with content type '" + contentType + "'");
                }
            }
        } catch( Exception e ) {
            logger.error("Unable to retrieve response content from request with status {}: {}", e.getMessage(), e);
            throw new RemoteCommunicationException("Unable to retrieve content from response", e);
        } finally { 
            httpRequest.disconnect();
        }
        
        if( cmdResponse != null ) {
            List<JaxbCommandResponse<?>> responses = cmdResponse.getResponses();
            if( responses.size() == 0 ) {
                return null;
            } else if( responses.size() == 1 ) {
                // The type information *should* come from the Command class -- but it's a jaxb-gen class, 
                // which means that it has lost it's type information.. 
                // TODO: fix this?
                JaxbCommandResponse<T> responseObject = (JaxbCommandResponse<T>) responses.get(0);
                if( responseObject instanceof JaxbExceptionResponse ) {
                    exceptionResponse = (JaxbExceptionResponse) responseObject;
                } else {
                    return responseObject.getResult();
                }
            } else {
                throw new RemoteCommunicationException("Unexpected number of results from " + command.getClass().getSimpleName()
                        + ":" + responses.size() + " results instead of only 1");
            }
        }

        logger.error("Response with status {} returned.", responseStatus);
        // Process exception response
        switch ( responseStatus ) {
        case 409:
            throw new RemoteTaskException(exceptionResponse.getMessage(), exceptionResponse.getStackTrace());
        default:
            if( exceptionResponse != null ) { 
                if( ! htmlException ) { 
                    throw new RemoteApiException(exceptionResponse.getMessage(), exceptionResponse.getStackTrace());
                } else { 
                    throw new RemoteCommunicationException(exceptionResponse.getMessage(), exceptionResponse.getStackTrace());
                } 
            } else { 
                throw new RemoteCommunicationException("Unable to communicate with remote API via URL " 
                        + "'" + httpRequest.getUri().toString() + "'");
            }
        }
    }
    
    private <T> T deserializeResponseContent(String responseBody, Class<T> entityClass) { 
       JaxbSerializationProvider jaxbSerializationProvider = config.getJaxbSerializationProvider();
       T responseEntity = null;
       try { 
           responseEntity = (T) jaxbSerializationProvider.deserialize(responseBody);
       } catch( ClassCastException cce ) { 
           throw new RemoteApiException("Unexpected entity in response body, expected " + entityClass.getName() + " instance.", cce);
       }
       return responseEntity;
    }

    // Command Object helper methods --

    protected static <T> T getField( String fieldName, Class objClass, Object obj, Class<T> fieldClass ) throws Exception {
        Field field = objClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    public static <T> T unsupported( Class<?> realClass, Class<T> returnClass ) {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException("The " + realClass.getSimpleName() + "." + methodName + "(..) method is not supported on the Remote Client instance.");
    }

    public static boolean emptyDeploymentId(String deploymentId) { 
        return deploymentId == null || deploymentId.trim().isEmpty();
    }
}
