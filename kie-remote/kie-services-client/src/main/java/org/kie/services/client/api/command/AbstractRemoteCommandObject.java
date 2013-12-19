package org.kie.services.client.api.command;

import static org.kie.services.client.serialization.SerializationConstants.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Response.Status;

import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartCorrelatedProcessCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.jbpm.services.task.commands.AddTaskCommand;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.CompositeCommand;
import org.jbpm.services.task.commands.FailTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationException;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
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
    }

    protected boolean isTaskService = false;

    // Compatibility methods -----------------------------------------------------------------------------------------------------

    public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + Task.class.getSimpleName()
                + " implementation.");
    }

    public void writeExternal(ObjectOutput arg0) throws IOException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException(methodName + " is not supported on the JAXB " + Task.class.getSimpleName()
                + " implementation.");
    }

    // Execute methods -----------------------------------------------------------------------------------------------------

    public <T> T execute(Command<T> cmd) {
        if( ! AcceptedCommands.getSet().contains(cmd.getClass()) ) {
            StackTraceElement [] st = Thread.currentThread().getStackTrace();
            String methodName = st[2].getMethodName();
            throw new UnsupportedOperationException( "The ." + methodName + "(..) method is not supported on the remote api." );
        }
        
        if( AcceptedCommands.SEND_OBJECT_PARAMETER_COMMANDS.contains(cmd.getClass()) ) {
            List<Object> extraClassInstanceList = new ArrayList<Object>();
            preprocessCommand(cmd, extraClassInstanceList);
            
            if( ! extraClassInstanceList.isEmpty() ) { 
                Set<Class<?>> extraJaxbClasses = new HashSet<Class<?>>();
                for( Object jaxbObject : extraClassInstanceList ) { 
                    Class<?> jaxbClass = jaxbObject.getClass();
                    if( jaxbClass.isLocalClass() || jaxbClass.isAnonymousClass() ) { 
                        throw new SerializationException("Only proper classes are allowed as parameters for the remote API: neither local nor anonymous classes are accepted: " + jaxbClass.getName());
                    }
                    extraJaxbClasses.add(jaxbClass);
                }
                config.addJaxbClasses(extraJaxbClasses);
            }
        }

        if (config.isRest()) {
            return executeRestCommand(cmd);
        } else {
            return executeJmsCommand(cmd);
        }
    }

    private void preprocessCommand(Command cmd, List<Object> extraClassInstanceList) {
        if( cmd instanceof CompleteWorkItemCommand ) {
            addPossiblyNullObjectMap(((CompleteWorkItemCommand) cmd).getResults(), extraClassInstanceList);
        } else if( cmd instanceof SignalEventCommand ) {
            addPossiblyNullObject(((SignalEventCommand) cmd).getEvent(), extraClassInstanceList);
        } else if( cmd instanceof StartCorrelatedProcessCommand ) {
            addPossiblyNullObjectList(((StartCorrelatedProcessCommand) cmd).getData(), extraClassInstanceList);
            addPossiblyNullObjectMap(((StartCorrelatedProcessCommand) cmd).getParameters(), extraClassInstanceList );
        } else if( cmd instanceof StartProcessCommand ) {
            addPossiblyNullObjectList(((StartProcessCommand) cmd).getData(), extraClassInstanceList);
            addPossiblyNullObjectMap(((StartProcessCommand) cmd).getParameters(), extraClassInstanceList );
        } else if( cmd instanceof SetGlobalCommand ) {
            addPossiblyNullObject(((SetGlobalCommand) cmd).getObject(), extraClassInstanceList );
        } else if( cmd instanceof InsertObjectCommand ) {
            addPossiblyNullObject(((InsertObjectCommand) cmd).getObject(), extraClassInstanceList);
        } else if( cmd instanceof UpdateCommand ) {
            addPossiblyNullObject(((UpdateCommand) cmd).getObject(),  extraClassInstanceList);
        } else if( cmd instanceof AddTaskCommand ) {
            addPossiblyNullObjectMap(((AddTaskCommand) cmd).getParams(), extraClassInstanceList);
        } else if( cmd instanceof CompleteTaskCommand ) {
            addPossiblyNullObjectMap(((CompleteTaskCommand) cmd).getData(), extraClassInstanceList);
        } else if( cmd instanceof FailTaskCommand ) {
            addPossiblyNullObjectMap(((FailTaskCommand) cmd).getData(), extraClassInstanceList);
        } else if (cmd instanceof CompositeCommand) {
            CompositeCommand composite = (CompositeCommand) cmd;

            preprocessCommand(composite.getMainCommand(), extraClassInstanceList);
            if (composite.getCommands() != null) {
                for (Command c : composite.getCommands()) {
                    preprocessCommand(c, extraClassInstanceList);
                }
            }
        }
    }

    private void addPossiblyNullObject(Object inputObject, List<Object> objectList ) { 
        if( inputObject != null ) { 
            objectList.add(inputObject);
        }
    }
    
    private void addPossiblyNullObjectList(List<Object> inputList, List<Object> objectList ) { 
        if( inputList != null && ! inputList.isEmpty() ) { 
            objectList.addAll(objectList);
        }
    }
    
    private void addPossiblyNullObjectMap(Map<String, Object> inputMap, List<Object> objectList ) { 
        if( inputMap != null && ! inputMap.isEmpty() ) { 
            objectList.addAll(inputMap.values());
        }
    }
    
    /**
     * Method to communicate with the backend via JMS.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private <T> T executeJmsCommand(Command<T> command) {
        JaxbCommandsRequest req = new JaxbCommandsRequest(config.getDeploymentId(), command);

        ConnectionFactory factory = config.getConnectionFactory();
        Queue sendQueue;
        if (command instanceof TaskCommand) {
            sendQueue = config.getKsessionQueue();
        } else {
            sendQueue = config.getTaskQueue();
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
                if (config.getPassword() != null) {
                    connection = factory.createConnection(config.getUsername(), config.getPassword());
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to setup a JMS connection.", jmse);
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
                msg.setJMSCorrelationID(corrId);
                msg.setIntProperty(SERIALIZATION_TYPE_PROPERTY_NAME, config.getSerializationType());
                Set<Class<?>> extraJaxbClasses = config.getExtraJaxbClasses(); 
                if( ! extraJaxbClasses.isEmpty() ) { 
                    String extraJaxbClassesPropertyValue = JaxbSerializationProvider.classSetToCommaSeperatedString(extraJaxbClasses);
                    msg.setStringProperty(EXTRA_JAXB_CLASSES_PROPERTY_NAME, extraJaxbClassesPropertyValue);
                    msg.setStringProperty(DEPLOYMENT_ID_PROPERTY_NAME, config.getDeploymentId());
                }
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to create and fill a JMS message.", jmse);
            } catch (SerializationException se) {
                throw new RemoteRuntimeException("Unable to deserialze JMS message.", se.getCause());
            }

            // send
            try {
                producer.send(msg);
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                response = consumer.receive(config.getQualityOfServiceThresholdMilliSeconds());
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to receive or retrieve the JMS response.", jmse);
            }


            if (response == null) {
                logger.warn("Response is empty, leaving");
                return null;
            }
            // extract response
            assert response != null : "Response is empty.";
            try {
                String xmlStr = ((BytesMessage) response).readUTF();
                cmdResponse = (JaxbCommandsResponse) serializationProvider.deserialize(xmlStr);
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jmse);
            } catch (SerializationException se) {
                throw new RemoteRuntimeException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", se.getCause());
            }
            assert cmdResponse != null : "Jaxb Cmd Response was null!";
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    session.close();
                } catch (JMSException jmse) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
        List<JaxbCommandResponse<?>> responses = cmdResponse.getResponses();
        if (responses.size() > 0) {
            JaxbCommandResponse<?> response = responses.get(0);
            if (response instanceof JaxbExceptionResponse) {
                JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) response;
                throw new RemoteRuntimeException(exceptionResponse.getMessage());
            } else {
                return (T) response.getResult();
            }
        } else {
            assert responses.size() == 0 : "There should only be 1 response, "
                    + "not " + responses.size() + ", returned by a command!";
            return null;
        }
    }

    /**
     * Method to communicate with the backend via REST.
     * 
     * @param command The {@link Command} object to be executed.
     * @return The result of the {@link Command} object execution.
     */
    private <T> T executeRestCommand(Command<T> command) {
        String deploymentId = config.getDeploymentId();
        ClientRequestFactory requestFactory = config.getRequestFactory();
        ClientRequest restRequest;
        if (isTaskService) {
            restRequest = requestFactory.createRelativeRequest("/task/execute");
        } else {
            restRequest = requestFactory.createRelativeRequest("/runtime/" + deploymentId + "/execute");
        }
        JaxbCommandsRequest jaxbRequest = new JaxbCommandsRequest(deploymentId, command);
        String jaxbRequestString = config.getJaxbSerializationProvider().serialize(jaxbRequest);
        restRequest.body(MediaType.APPLICATION_XML, jaxbRequestString);
        
        ClientResponse<Object> response = null;
        String requestUrl;
        try {
            requestUrl = restRequest.getUri();
            response = restRequest.post(Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Unable to post request: " + e.getMessage(), e);
        }

        if (response.getResponseStatus() == Status.OK) {
            JaxbCommandsResponse commandResponse = response.getEntity(JaxbCommandsResponse.class);
            List<JaxbCommandResponse<?>> responses = commandResponse.getResponses();
            if (responses.size() == 0) {
                return null;
            } else if (responses.size() == 1) {
                JaxbCommandResponse<?> responseObject = responses.get(0);
                if (responseObject instanceof JaxbExceptionResponse) {
                    JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) responseObject;
                    String causeMessage = exceptionResponse.getCauseMessage();
                    throw new RuntimeException(exceptionResponse.getMessage()
                            + (causeMessage == null ? "" : " Caused by: " + causeMessage));
                } else {
                    return (T) responseObject.getResult();
                }
            } else {
                throw new RuntimeException("Unexpected number of results from " + command.getClass().getSimpleName() + ":"
                        + responses.size() + " results instead of only 1");
            }
        } else {
            throw new RuntimeException("Error invoking " + command.getClass().getSimpleName() + " via REST (" + requestUrl + "):\n"
                    + response.getEntity(String.class));
        }
    }
}
