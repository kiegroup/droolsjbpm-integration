package org.kie.services.client.api.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.xml.bind.JAXBException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jbpm.services.task.commands.TaskCommand;
import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.services.client.api.RemoteConfiguration;
import org.kie.services.client.api.RemoteConfiguration.AuthenticationType;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic to interact with the REST or JMS api's. It is the basis for all of the remote interface instances.
 */
public abstract class AbstractRemoteCommandObject {

    protected static Logger logger = LoggerFactory.getLogger(AbstractRemoteCommandObject.class);

    protected final String url;
    protected final RemoteConfiguration config;

    public AbstractRemoteCommandObject(RemoteConfiguration config) {
        this.url = null;
        this.config = config;
    }

    public AbstractRemoteCommandObject(String url, RemoteConfiguration config) {
        this.url = url;
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(Command<T> command) {
        if (config.isRest()) {
            return executeRestCommand(command);
        } else {
            return executeJmsCommand(command);
        }
    }

    public <T> T executeJmsCommand(Command<T> command) {
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
                if( config.getPassword() != null ) { 
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
            try {
                msg = session.createBytesMessage();
                msg.setJMSCorrelationID(corrId);
                msg.setIntProperty("serialization", config.getSerializationType());
                // TODO: pluggable serialization based on serialization type
                String xmlStr = JaxbSerializationProvider.convertJaxbObjectToString(req);
                msg.writeUTF(xmlStr);
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to create and fill a JMS message.", jmse);
            } catch (JAXBException jaxbe) {
                throw new RemoteRuntimeException("Unable to deserialze JMS message.", jaxbe);
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

            // extract response
            assert response != null : "Response is empty.";
            try {
                String xmlStr = ((BytesMessage) response).readUTF();
                // TODO: pluggable serialization based on serialization type
                cmdResponse = (JaxbCommandsResponse) JaxbSerializationProvider.convertStringToJaxbObject(xmlStr);
            } catch (JMSException jmse) {
                throw new RemoteRuntimeException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jmse);
            } catch (JAXBException jaxbe) {
                throw new RemoteRuntimeException("Unable to extract " + JaxbCommandsResponse.class.getSimpleName()
                        + " instance from JMS response.", jaxbe);
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
        if( responses.size() > 0 ) { 
            JaxbCommandResponse<?> response = responses.get(0);
            if( response instanceof JaxbExceptionResponse ) { 
                JaxbExceptionResponse exceptionResponse = (JaxbExceptionResponse) response;
                throw new RemoteRuntimeException(exceptionResponse.getMessage());
            } else { 
                return (T) response.getResult();
            }
        } else { 
            assert responses.size() == 0 : "There shouldn't be more than 1 response [" + responses.size() + "] returned by a command!";
            return null;
        }
    }

    public <T> T executeRestCommand(Command<T> command) {
        String baseUrl = config.getUrl();
        String deploymentId = config.getDeploymentId();
        AuthenticationType authenticationType = config.getAuthenticationType();

        ClientRequest restRequest = null;
        DefaultHttpClient client = new DefaultHttpClient();
        if (AuthenticationType.BASIC == authenticationType) {
            String username = config.getUsername();
            String password = config.getPassword();
            client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
        } else if (AuthenticationType.FORM == authenticationType) {
            String username = config.getUsername();
            String password = config.getPassword();
            // first challenge authentication to be enforced - return logon form
            // currently pointing to a non existing resource
            HttpPost mainMethod = new HttpPost(baseUrl);
            List<NameValuePair> nameValuePairsEmpty = new ArrayList<NameValuePair>(1);
            try {
                mainMethod.setEntity(new UrlEncodedFormEntity(nameValuePairsEmpty));
                HttpResponse response = client.execute(mainMethod);

                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("Error invoking REST " + url + " " + response.getStatusLine());
                }

                EntityUtils.consume(response.getEntity());

                // next authenticate the request
                HttpPost authMethod = new HttpPost(baseUrl + "/j_security_check");
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("j_username", username));
                nameValuePairs.add(new BasicNameValuePair("j_password", password));

                authMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // make sure that request will be redirected request has been authenticated and authorized
                client.setRedirectStrategy(new DefaultRedirectStrategy() {
                    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
                        boolean isRedirect = false;
                        try {
                            isRedirect = super.isRedirected(request, response, context);
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        }
                        if (!isRedirect) {
                            int responseCode = response.getStatusLine().getStatusCode();
                            if (responseCode == 301 || responseCode == 302) {
                                return true;
                            }
                        }
                        return isRedirect;
                    }
                });
                HttpResponse authresponse = client.execute(authMethod);
                List<Integer> invalidCodes = Arrays.asList(new Integer[] { 500, 401, 403 });
                if (invalidCodes.contains(response.getStatusLine().getStatusCode())) {
                    throw new RuntimeException("Error invoking REST " + url + " " + response.getStatusLine());
                }
                EntityUtils.consume(authresponse.getEntity());

            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Could not initialize form-based authentication", e);
            } catch (ClientProtocolException e) {
                throw new RuntimeException("Could not initialize form-based authentication", e);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize form-based authentication", e);
            }
        }
        
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
        restRequest = new ClientRequest(url, executor);
        restRequest.body(MediaType.APPLICATION_XML, new JaxbCommandsRequest(deploymentId, command));
        ClientResponse<Object> response = null;
        
        try {
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
                throw new RuntimeException("Unexpected number of results: " + responses.size());
            }
        } else {
            // TODO error handling
            throw new RuntimeException("Error invoking REST " + url + " " + response.getResponseStatus() + " "
                    + response.getEntity(String.class));
        }

    }

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

}
