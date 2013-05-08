package org.kie.services.client.api;

import static org.kie.services.client.api.RemoteRuntimeEngineConstants.DOMAIN_NAME;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.kie.services.client.message.serialization.impl.jaxb.JaxbServiceMessage;

public abstract class AbstractServiceRequestProxy implements InvocationHandler {

    protected ServiceMessage request;
    protected final MessageSerializationProvider serializationProvider;

    protected static Set<Method> unsupportedMethods = new HashSet<Method>();
    protected static String[] messageHolderMethods;
    static {
        Method[] objectMethods = Object.class.getMethods();
        for (Method objMethod : objectMethods) {
            unsupportedMethods.add(objMethod);
        }

        Method[] methods = MessageHolder.class.getMethods();
        messageHolderMethods = new String[methods.length];
        for (int i = 0; i < methods.length; ++i) {
            messageHolderMethods[i] = methods[i].getName();
        }
        Arrays.sort(messageHolderMethods);
    }

    // package level constructor
    protected AbstractServiceRequestProxy(Map<Integer, String> params, MessageSerializationProvider serializationProvider) {
        String domainName = params.get(DOMAIN_NAME);
        // Message
        this.request = new ServiceMessage(domainName);
        this.serializationProvider = serializationProvider;
    }

    public abstract Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

    protected Object handleMessageHolderMethodsAndUnsupportedMethods(Method method, Object[] args) {
        if (MessageHolder.class.equals(method.getDeclaringClass())) {
            ServiceMessage request = this.request;
            this.request = new ServiceMessage(this.request.getDomainName());
            // createJmsMessage
            if (messageHolderMethods[0].equals(method.getName())) {
                try {
                    return serializationProvider.convertServiceMessageToJmsMessage(request, (Session) args[0]);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to convert request to message: " + e.getMessage(), e);
                }
            // getMessageXmlString
            } else if (messageHolderMethods[1].equals(method.getName())) {
                JaxbServiceMessage jaxbRequest = new JaxbServiceMessage(request);
                try {
                    return JaxbSerializationProvider.convertJaxbObjectToString(jaxbRequest);
                } catch (JAXBException jaxbe) {
                    throw new RuntimeException("Unable to convert request to XML string: " + jaxbe.getMessage(), jaxbe);
                }
            // getRequest
            } else if (messageHolderMethods[2].equals(method.getName())) {
                return request;
            }
        }

        // No object methods (.wait(), .clone(), etc.. ) supported
        if ( unsupportedMethods.contains(method) ) { 
            throw new UnsupportedOperationException(method.getName() + " is an unsupported method for this instance.");
        }

        return null;
    }

}
