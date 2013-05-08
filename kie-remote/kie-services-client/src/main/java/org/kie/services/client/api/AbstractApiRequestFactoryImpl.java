package org.kie.services.client.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.kie.services.client.message.serialization.impl.MapMessageSerializationProvider;
import org.kie.services.client.message.serialization.impl.ProtobufSerializationProvider;
import org.kie.services.client.message.serialization.impl.jaxb.JaxbServiceMessage;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

public abstract class AbstractApiRequestFactoryImpl {

    protected MessageSerializationProvider serializationProvider = null;

    public void setSerializationProvider(MessageSerializationProvider serializationProvider) {
        this.serializationProvider = serializationProvider;
    }

    public void setSerialization(MessageSerializationProvider.Type serializationType) {
        switch (serializationType) {
        case JAXB:
            this.serializationProvider = new JaxbSerializationProvider();
            break;
        case MAP_MESSAGE:
            this.serializationProvider = new MapMessageSerializationProvider();
            break;
        case PROTOBUF:
            this.serializationProvider = new ProtobufSerializationProvider();
            break;
        default:
            throw new UnsupportedOperationException("Unknown serialization type: " + serializationType.toString());
        }
    }

    public Message createJmsMessage(RuntimeEngine remoteRuntimeEngine, Session jmsSession) {
        return internalCreateJmsMessage(remoteRuntimeEngine, jmsSession);
    }

    public Message createJmsMessage(KieSession remoteKieSession, Session jmsSession) {
        return internalCreateJmsMessage(remoteKieSession, jmsSession);
    }

    public Message createJmsMessage(TaskService remoteTaskService, Session jmsSession) {
        return internalCreateJmsMessage(remoteTaskService, jmsSession);
    }

    public Message createJmsMessage(WorkItemManager remoteWorkItemManager, Session jmsSession) {
        return internalCreateJmsMessage(remoteWorkItemManager, jmsSession);
    }

    private Message internalCreateJmsMessage(Object messageHolderInstance, Session jmsSession) {
        if (!(messageHolderInstance instanceof MessageHolder)) {
            throw new UnsupportedOperationException(messageHolderInstance.getClass().getSimpleName()
                    + " is not a Remote RuntimeEngine instance.");
        }
        return ((MessageHolder) messageHolderInstance).createJmsMessage(jmsSession);
    }

    public ServiceMessage sendRestRequest(RuntimeEngine remoteRuntimeEngine, URL baseUrl) {
        return internalSendRestRequest(remoteRuntimeEngine, baseUrl);
    }

    public ServiceMessage sendRestRequest(KieSession remoteKieSession, URL baseUrl) {
        return internalSendRestRequest(remoteKieSession, baseUrl);
    }

    public ServiceMessage sendRestRequest(TaskService remoteTaskService, URL baseUrl) {
        return internalSendRestRequest(remoteTaskService, baseUrl);
    }

    public ServiceMessage sendRestRequest(WorkItemManager remoteWorkItemManager, URL baseUrl) {
        return internalSendRestRequest(remoteWorkItemManager, baseUrl);
    }

    private ServiceMessage internalSendRestRequest(Object messageHolderInstance, URL baseUrl) {
        if (!(messageHolderInstance instanceof MessageHolder)) {
            throw new UnsupportedOperationException(messageHolderInstance.getClass().getSimpleName()
                    + " is not a Remote instance of a " + messageHolderInstance.getClass().getSimpleName());
        }

        // create REST request
        ServiceMessage requestMsg = ((MessageHolder) messageHolderInstance).getRequest();
        URL requestUrl = extactRestUrlFromMessage(requestMsg, baseUrl);

        ClientRequest restRequest = new ClientRequest(requestUrl.toString());
        String msgXmlString = ((MessageHolder) messageHolderInstance).getMessageXmlString();
        restRequest.body(MediaType.APPLICATION_XML, msgXmlString);

        // Get response
        ServiceMessage result = new ServiceMessage(requestMsg.getDomainName());
        try {
            ClientResponse<JaxbServiceMessage> response = restRequest.post(JaxbServiceMessage.class);
            if (response.getResponseStatus() == Status.OK) {
                result = ((JaxbSerializationProvider) this.serializationProvider).convertJaxbRequesMessageToServiceMessage(response
                        .getEntity());
            } else {
                // TODO: add status/failure to result
            }
        } catch (Exception e) {
            // TODO: add exception to ServiceMessage result
        }
        
        // TODO: what to return?
        return null;
    }

    private URL extactRestUrlFromMessage(ServiceMessage msg, URL baseUrl) {
        if( msg.getOperations().isEmpty() ) { 
            throw new IllegalStateException("No operations have been called on the remote instance.");
        }
        
        OperationMessage operation = msg.getOperations().get(0);
        String operName = operation.getMethod().getName();
        String operUrl = null;
        switch(operation.getServiceType()) { 
        case ServiceMessage.KIE_SESSION_REQUEST:
            // TODO: convert operName to url (operUrl)
            break;
        case ServiceMessage.WORK_ITEM_MANAGER_REQUEST:
            // TODO: convert operName to url (operUrl)
            break;
        case ServiceMessage.TASK_SERVICE_REQUEST:
            // TODO: convert operName to url (operUrl)
            break;
        default:
            throw new IllegalStateException("Unknown service type for OperationMessage: " + operation.getServiceType());
        }
        
        URL requestUrl = null;
        try {
            requestUrl = new URL(baseUrl, msg.getDomainName() + "/" + operUrl);
        } catch (MalformedURLException murle) {
            throw new IllegalStateException("Unable to convert operation '" + operName + "' to URL: " + murle.getMessage(), murle);
        }
        
        return requestUrl;
    }
}
