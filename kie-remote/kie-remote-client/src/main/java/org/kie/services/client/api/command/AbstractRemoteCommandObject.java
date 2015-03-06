package org.kie.services.client.api.command;

import static org.kie.services.client.api.command.InternalJmsCommandHelper.internalExecuteJmsCommand;
import static org.kie.services.shared.ServicesVersion.VERSION;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Queue;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.remote.client.api.exception.MissingRequiredInfoException;
import org.kie.remote.client.api.exception.RemoteApiException;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.api.exception.RemoteTaskException;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.remote.jaxb.gen.AddTaskCommand;
import org.kie.remote.jaxb.gen.AuditCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.CompleteWorkItemCommand;
import org.kie.remote.jaxb.gen.FailTaskCommand;
import org.kie.remote.jaxb.gen.InsertObjectCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.SetGlobalCommand;
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

    protected void dispose() { 
        
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

    protected Object executeCommand( Command cmd ) {
        if( AcceptedClientCommands.isSendObjectParameterCommandClass(cmd.getClass()) ) {
            List<Object> extraClassInstanceList = new ArrayList<Object>();
            preprocessCommand(cmd, extraClassInstanceList);

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

        if( config.isRest() ) {
            return executeRestCommand(cmd);
        } else {
            return executeJmsCommand(cmd);
        }
    }

    void preprocessCommand( Object cmdObj, List<Object> extraClassInstanceList ) {
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
        } 
    }

    void addPossiblyNullObject( Object inputObject, List<Object> objectList ) {
        if( inputObject != null ) {
            if( inputObject instanceof List )  { 
                objectList.addAll((List) inputObject);
            } else if( inputObject instanceof JaxbStringObjectPairArray ) { 
                for( JaxbStringObjectPair stringObjectPair : ((JaxbStringObjectPairArray) inputObject).getItems() ) {
                    objectList.add(stringObjectPair.getValue());
                }
            } else {  
                objectList.add(inputObject);
            }
        }
    }

    static JaxbCommandsRequest prepareCommandRequest( Command command, String userName, String deploymentId, Long processInstanceId ) {
        if( deploymentId == null && !(command instanceof TaskCommand || command instanceof AuditCommand) ) {
            throw new MissingRequiredInfoException("A deployment id is required when sending commands involving the KieSession.");
        }
        JaxbCommandsRequest req;
        if( command instanceof AuditCommand ) {
            req = new JaxbCommandsRequest(command);
        } else {
            req = new JaxbCommandsRequest(deploymentId, command);
        }

        if( processInstanceId != null ) { 
            processInstanceId = findProcessInstanceId(command);
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
    private Object executeJmsCommand( Command command ) {
       
        Queue sendQueue;
        boolean isTaskCommand = (command instanceof TaskCommand);
        if( isTaskCommand ) {
            sendQueue = config.getTaskQueue();
            if( ! config.getUseUssl() && ! config.getDisableTaskSecurity() ) {
                throw new SecurityException("Task operation requests can only be sent via JMS if SSL is used.");
            }
        } else {
            sendQueue = config.getKsessionQueue();
        }
        
        return internalExecuteJmsCommand(command,
                config.getConnectionUserName(), config.getConnectionPassword(), 
                config.getUserName(), config.getPassword(), config.getDeploymentId(), config.getProcessInstanceId(),
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
        JaxbCommandsRequest jaxbRequest = prepareCommandRequest(command, config.getUserName(), config.getDeploymentId(), config.getProcessInstanceId());
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
            throw new RemoteCommunicationException("Unable to post request: " + e.getMessage(), e);
        } 
        
        // Get response
        boolean htmlException = false;
        JaxbExceptionResponse exceptionResponse = null;
        JaxbCommandsResponse cmdResponse = null;
        int responseStatus = httpResponse.code();
        try {
            String content = httpResponse.body();
            if( responseStatus < 300 ) {
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
                    exceptionResponse = deserializeResponseContent(content, JaxbExceptionResponse.class);
                } else if( contentType.startsWith(MediaType.TEXT_HTML) ) { 
                    htmlException = true;
                    exceptionResponse = new JaxbExceptionResponse();
                    Document doc = Jsoup.parse(content);
                    String body = doc.body().text();
                    exceptionResponse.setMessage(body);
                    exceptionResponse.setUrl(httpRequest.getUri().toString());
                    exceptionResponse.setStackTrace("");
                }
                else { 
                    throw new RemoteCommunicationException("Unable to deserialize response with content type '" + contentType + "'");
                }
            }
        } catch( Exception e ) {
            logger.error("Unable to retrieve response content from request with status {}: {}", e.getMessage(), e);
            throw new RemoteCommunicationException("Unable to retrieve content from response!", e);
        } finally { 
            httpRequest.disconnect();
        }
        
        if( cmdResponse != null ) {
            List<JaxbCommandResponse<?>> responses = cmdResponse.getResponses();
            if( responses.size() == 0 ) {
                return null;
            } else if( responses.size() == 1 ) {
                JaxbCommandResponse<?> responseObject = responses.get(0);
                if( responseObject instanceof JaxbExceptionResponse ) {
                    exceptionResponse = (JaxbExceptionResponse) responseObject;
                } else {
                    return (T) responseObject.getResult();
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
            throw new RemoteTaskException(exceptionResponse.getMessage() + ":\n" + exceptionResponse.getStackTrace());
        default:
            if( exceptionResponse != null ) { 
                if( ! htmlException ) { 
                    throw new RemoteApiException(exceptionResponse.getMessage() + ":\n" + exceptionResponse.getStackTrace());
                } else { 
                    throw new RemoteCommunicationException(exceptionResponse.getMessage() + ":\n" + exceptionResponse.getStackTrace());
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

    // TODO: https://issues.jboss.org/browse/JBPM-4296
    private static Long findProcessInstanceId( Object command ) {
        if( command instanceof AuditCommand ) {
            return null;
        }
        try {
            Field[] fields = command.getClass().getDeclaredFields();

            for( Field field : fields ) {
                field.setAccessible(true);
                if( field.isAnnotationPresent(XmlAttribute.class) ) {
                    String attributeName = field.getAnnotation(XmlAttribute.class).name();
                    if( "process-instance-id".equalsIgnoreCase(attributeName) ) {
                        return (Long) field.get(command);
                    }
                } else if( field.isAnnotationPresent(XmlElement.class) ) {
                    String elementName = field.getAnnotation(XmlElement.class).name();
                    if( "process-instance-id".equalsIgnoreCase(elementName) ) {
                        return (Long) field.get(command);
                    }
                } else if( field.getName().equals("processInstanceId") ) {
                    return (Long) field.get(command);
                }
            }
        } catch( Exception e ) {
            logger.debug("Unable to find process instance id on command {} due to {}", command, e.getMessage());
        }

        return null;
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
