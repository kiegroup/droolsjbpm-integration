package org.kie.services.client.api.command;

import static org.kie.services.client.serialization.SerializationConstants.DEPLOYMENT_ID_PROPERTY_NAME;
import static org.kie.services.client.serialization.SerializationConstants.EXTRA_JAXB_CLASSES_PROPERTY_NAME;
import static org.kie.services.client.serialization.SerializationConstants.SERIALIZATION_TYPE_PROPERTY_NAME;
import static org.kie.services.shared.ServicesVersion.VERSION;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.remote.client.jaxb.AcceptedClientCommands;
import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.jaxb.gen.AddTaskCommand;
import org.kie.remote.jaxb.gen.AuditCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.CompleteWorkItemCommand;
import org.kie.remote.jaxb.gen.FailTaskCommand;
import org.kie.remote.jaxb.gen.InsertObjectCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPair;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.SetGlobalCommand;
import org.kie.remote.jaxb.gen.SignalEventCommand;
import org.kie.remote.jaxb.gen.StartCorrelatedProcessCommand;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.remote.jaxb.gen.TaskCommand;
import org.kie.remote.jaxb.gen.UpdateCommand;
import org.kie.services.client.api.command.exception.MissingRequiredInfoException;
import org.kie.services.client.api.command.exception.RemoteApiException;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;
import org.kie.services.client.api.command.exception.RemoteTaskException;
import org.kie.services.client.api.rest.KieRemoteHttpRequest;
import org.kie.services.client.api.rest.KieRemoteHttpRequestException;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;
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

    AbstractRemoteCommandObject(RemoteConfiguration config) {
        this.config = config;
        if( this.config.isJms() ) { 
            
        }
    }

    protected boolean isTaskService = false;

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


    protected Object executeCommand( Object cmd ) {
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
                config.addJaxbClasses(extraJaxbClasses);
            }
        }

        if( config.isRest() ) {
            return executeRestCommand(cmd);
        } else {
            return executeJmsCommand(cmd);
        }
    }

    private void preprocessCommand( Object cmd, List<Object> extraClassInstanceList ) {
        if( cmd instanceof CompleteWorkItemCommand ) {
            addPossiblyNullObjectMap(((CompleteWorkItemCommand) cmd).getResult(), extraClassInstanceList);
        } else if( cmd instanceof SignalEventCommand ) {
            addPossiblyNullObject(((SignalEventCommand) cmd).getEvent(), extraClassInstanceList);
        } else if( cmd instanceof StartCorrelatedProcessCommand ) {
            addPossiblyNullObjectList(((StartCorrelatedProcessCommand) cmd).getData().getDatas(), extraClassInstanceList);
            addPossiblyNullObjectMap(((StartCorrelatedProcessCommand) cmd).getParameter(), extraClassInstanceList);
        } else if( cmd instanceof StartProcessCommand ) {
            StartProcessCommand startProcCmd = (StartProcessCommand) cmd;
            if( startProcCmd.getData() != null ) { 
                addPossiblyNullObjectList(startProcCmd.getData().getDatas(), extraClassInstanceList);
            } 
            addPossiblyNullObjectMap(((StartProcessCommand) cmd).getParameter(), extraClassInstanceList);
        } else if( cmd instanceof SetGlobalCommand ) {
            addPossiblyNullObject(((SetGlobalCommand) cmd).getObject(), extraClassInstanceList);
        } else if( cmd instanceof InsertObjectCommand ) {
            addPossiblyNullObject(((InsertObjectCommand) cmd).getObject(), extraClassInstanceList);
        } else if( cmd instanceof UpdateCommand ) {
            addPossiblyNullObject(((UpdateCommand) cmd).getObject(), extraClassInstanceList);
        } else if( cmd instanceof AddTaskCommand ) {
            addPossiblyNullObjectMap(((AddTaskCommand) cmd).getParameter(), extraClassInstanceList);
        } else if( cmd instanceof CompleteTaskCommand ) {
            addPossiblyNullObjectMap(((CompleteTaskCommand) cmd).getData(), extraClassInstanceList);
        } else if( cmd instanceof FailTaskCommand ) {
            addPossiblyNullObjectMap(((FailTaskCommand) cmd).getData(), extraClassInstanceList);
        } 
    }

    private void addPossiblyNullObject( Object inputObject, List<Object> objectList ) {
        if( inputObject != null ) {
            objectList.add(inputObject);
        }
    }

    private void addPossiblyNullObjectList( List<Object> inputList, List<Object> objectList ) {
        if( inputList != null && !inputList.isEmpty() ) {
            objectList.addAll(objectList);
        }
    }

    private void addPossiblyNullObjectMap( JaxbStringObjectPairArray inputMap, List<Object> objectList ) {
        if( inputMap != null && inputMap.getItems() != null && !inputMap.getItems().isEmpty() ) {
            for( JaxbStringObjectPair stringObjectPair : inputMap.getItems() ) {
                objectList.add(stringObjectPair.getValue());
            }
        }
    }

    private JaxbCommandsRequest prepareCommandRequest( Object command ) {
        if( config.getDeploymentId() == null && !(command instanceof TaskCommand || command instanceof AuditCommand) ) {
            throw new MissingRequiredInfoException("A deployment id is required when sending commands involving the KieSession.");
        }
        JaxbCommandsRequest req;
        if( command instanceof AuditCommand ) {
            req = new JaxbCommandsRequest(command);
        } else {
            req = new JaxbCommandsRequest(config.getDeploymentId(), command);
        }

        Long processInstanceId = findProcessInstanceId(command);
        if( processInstanceId == null ) {
            processInstanceId = config.getProcessInstanceId();
        }
        req.setProcessInstanceId(processInstanceId);
        req.setUser(config.getUserName());
        req.setVersion(VERSION);

        return req;
    }

    /**
     * Method to communicate with the backend via JMS.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private Object executeJmsCommand( Object command ) {
        JaxbCommandsRequest req = prepareCommandRequest(command);
        String deploymentId = config.getDeploymentId();

        ConnectionFactory factory = config.getConnectionFactory();
        Queue sendQueue;

        boolean isTaskCommand = (command instanceof TaskCommand);
        if( isTaskCommand ) {
            sendQueue = config.getTaskQueue();
            if( !config.getUseUssl() ) {
                throw new SecurityException("Task operation requests can only be sent via JMS if SSL is used.");
            }
        } else {
            sendQueue = config.getKsessionQueue();
        }
        Queue responseQueue = config.getResponseQueue();

        Connection connection = null;
        Session session = null;
        JaxbCommandsResponse cmdResponse = null;
        String corrId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = '" + corrId + "'";
        try {

            // setup
            MessageProducer producer;
            MessageConsumer consumer;
            try {
                if( config.getPassword() != null ) {
                    connection = factory.createConnection(config.getUserName(), config.getPassword());
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to setup a JMS connection.", jmse);
            }

            // Create msg
            BytesMessage msg;
            JaxbSerializationProvider serializationProvider;
            try {
                msg = session.createBytesMessage();

                // serialize request
                serializationProvider = config.getJaxbSerializationProvider();
                String xmlStr = serializationProvider.serialize(req);
                msg.writeUTF(xmlStr);

                // set properties
                // 1. corr id
                msg.setJMSCorrelationID(corrId);
                // 2. serialization info
                msg.setIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME, config.getSerializationType());
                Set<Class<?>> extraJaxbClasses = config.getExtraJaxbClasses();
                if( !extraJaxbClasses.isEmpty() ) {
                    String extraJaxbClassesPropertyValue = JaxbSerializationProvider
                            .classSetToCommaSeperatedString(extraJaxbClasses);
                    msg.setStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME, extraJaxbClassesPropertyValue);
                    if( deploymentId == null ) {
                        throw new MissingRequiredInfoException(
                                "Deserialization of parameter classes requires a deployment id, which has not been configured.");
                    }
                    msg.setStringProperty(DEPLOYMENT_ID_PROPERTY_NAME, deploymentId);
                }
                // 3. user/pass for task operations
                String userName = config.getUserName();
                String password = config.getPassword();
                if( isTaskCommand ) {
                    if( userName == null ) {
                        throw new RemoteCommunicationException(
                                "A user name is required when sending task operation requests via JMS");
                    }
                    if( password == null ) {
                        throw new RemoteCommunicationException(
                                "A password is required when sending task operation requests via JMS");
                    }
                    msg.setStringProperty("username", userName);
                    msg.setStringProperty("password", password);
                }
                // 4. process instance id
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to create and fill a JMS message.", jmse);
            } catch( SerializationException se ) {
                throw new RemoteCommunicationException("Unable to deserialze JMS message.", se.getCause());
            }

            // send
            try {
                producer.send(msg);
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                response = consumer.receive(config.getTimeout() * 1000);
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to receive or retrieve the JMS response.", jmse);
            }

            if( response == null ) {
                logger.warn("Response is empty");
                return null;
            }
            // extract response
            assert response != null: "Response is empty.";
            try {
                String xmlStr = ((BytesMessage) response).readUTF();
                cmdResponse = (JaxbCommandsResponse) serializationProvider.deserialize(xmlStr);
            } catch( JMSException jmse ) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jmse);
            } catch( SerializationException se ) {
                throw new RemoteCommunicationException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", se.getCause());
            }
            assert cmdResponse != null: "Jaxb Cmd Response was null!";
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                    if( session != null ) {
                        session.close();
                    }
                } catch( JMSException jmse ) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
        String version = cmdResponse.getVersion();
        if( version == null ) {
            version = "pre-6.0.3";
        }
        if( !version.equals(VERSION) ) {
            logger.info("Response received from server version [{}] while client is version [{}]! This may cause problems.",
                    version, VERSION);
        }
        List<JaxbCommandResponse<?>> responses = cmdResponse.getResponses();
        if( responses.size() > 0 ) {
            JaxbCommandResponse<?> response = responses.get(0);
            if( response instanceof JaxbExceptionResponse ) {
                JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) response;
                throw new RemoteApiException(exceptionResponse.getMessage());
            } else {
                return response.getResult();
            }
        } else {
            assert responses.size() == 0: "There should only be 1 response, " + "not " + responses.size()
                    + ", returned by a command!";
            return null;
        }
    }

    /**
     * Method to communicate with the backend via REST.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private <T> T executeRestCommand( Object command ) {
        JaxbCommandsRequest jaxbRequest = prepareCommandRequest(command);
        String deploymentId = config.getDeploymentId();

        KieRemoteHttpRequest httpRequest = config.getHttpRequest();
        if( config.getExtraJaxbClasses().isEmpty() && (isTaskService || command instanceof AuditCommand) ) {
            httpRequest = httpRequest.relativeRequest("/task/execute");
        } else {
            httpRequest = httpRequest.relativeRequest("/runtime/" + deploymentId + "/execute");
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

        try {
            logger.debug("Sending POST request with " + command.getClass().getSimpleName() + " to " + httpRequest.getUri());
            httpRequest.post().contentType(MediaType.APPLICATION_XML).body(jaxbRequestString).code();
        } catch( Exception e ) {
            throw new RemoteCommunicationException("Unable to post request: " + e.getMessage(), e);
        }

        // Get response
        JaxbExceptionResponse exceptionResponse = null;
        JaxbCommandsResponse commandResponse = null;
        int responseStatus = httpRequest.code();
        try {
            if( responseStatus < 300 ) {
                commandResponse = httpRequest.responseEntity(JaxbCommandsResponse.class);
            } else {
                exceptionResponse = httpRequest.responseEntity(JaxbExceptionResponse.class);
            }
        } catch( Exception e ) {
            logger.error("Unable to retrieve response content from request with status {}: {}", e.getMessage(), e);
            throw new RemoteCommunicationException("Unable to retrieve content from response!", e);
        }
        if( exceptionResponse == null && commandResponse != null ) {
            List<JaxbCommandResponse<?>> responses = commandResponse.getResponses();
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

        // Process exception response
        switch ( httpRequest.code() ) {
        case 409:
            throw new RemoteTaskException(exceptionResponse.getMessage() + ":\n" + exceptionResponse.getStackTrace());
        case 200:
        default:
            throw new RemoteApiException(exceptionResponse.getMessage() + ":\n" + exceptionResponse.getStackTrace());
        }
    }

    // TODO: https://issues.jboss.org/browse/JBPM-4296
    private Long findProcessInstanceId( Object command ) {
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

    protected static JaxbStringObjectPairArray convertMapToPairArray( Map<String, Object> parameters ) {
        JaxbStringObjectPairArray arrayMap = new JaxbStringObjectPairArray();
        if (parameters == null || parameters.isEmpty()) {
            return arrayMap;
        }
        List<JaxbStringObjectPair> items = arrayMap.getItems();
        for( Entry<String, Object> entry : parameters.entrySet() ) {
            JaxbStringObjectPair pair = new JaxbStringObjectPair();
            pair.setKey(entry.getKey());
            pair.setValue(entry.getValue());
            items.add(pair);
        }
        return arrayMap;
    }

    protected static <T> T getField( String fieldName, Class objClass, Object obj, Class<T> fieldClass ) throws Exception {
        Field field = objClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    public static <T> T unsupported( Class<?> realClass, Class<T> returnClass ) {
        String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
        throw new UnsupportedOperationException("The " + realClass.getSimpleName() + "." + methodName + "(..) method is not supported on the Remote Client instance.");
    }

}
