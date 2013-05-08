package org.kie.services.client.message.serialization.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.MessageSerializationProvider;

/**
 * TODO: THIS IS NOT PRODUCTION READY!!
 */
public class MapMessageSerializationProvider implements MessageSerializationProvider {

    /**
     * See {@link Type}.
     */
    private int serializationType = Type.MAP_MESSAGE.getValue();
    
    private final static String DOMAIN_NAME     = "d";
    private final static String VERSION         = "v";
    private final static String SERVICE_TYPE    = "t";
    private final static String NUM_OPERATIONS  = "o";
    private final static String METHOD_NAME     = "m";
    private final static String NUM_ARGUMENTS   = "a";
    private final static String RESULT          = "r";
    
    @Override
    // Add throws exception instead of returning null?
    public Message convertServiceMessageToJmsMessage(ServiceMessage request, Session jmsSession) throws Exception {
        MapMessage message = null;
        String currentKey = null;

        try {
            message = jmsSession.createMapMessage();

            currentKey = VERSION;
            message.setInt(currentKey, request.getVersion());

            if (request.getDomainName() != null) {
                currentKey = DOMAIN_NAME;
                message.setString(currentKey, request.getDomainName());
            }

            List<OperationMessage> operations = request.getOperations();
            int numOperations = request.getOperations().size();
            for (int i = 0; i < numOperations; ++i) {
                OperationMessage operation = operations.get(i);
                currentKey = i + ":" + METHOD_NAME;
                message.setString(currentKey, operation.getMethodName());
                currentKey = i + ":" + SERVICE_TYPE;
                message.setInt(currentKey, operation.getServiceType());

                if (operation.isResponse()) {
                   currentKey = i + ":" + RESULT;
                   message.setObject(currentKey, operation.getResult());
                } else {
                    Object[] args = operation.getArgs();
                    if (args != null) {
                        currentKey = i + ":" + NUM_ARGUMENTS;
                        message.setInt(String.valueOf(currentKey), operation.getArgs().length);

                        for (int a = 0; i < operation.getArgs().length; ++a) {
                            currentKey = i + ":" + Integer.toString(a);
                            message.setObject(currentKey, args[a]);
                        }
                    } else {
                        currentKey = NUM_ARGUMENTS;
                        message.setInt(currentKey, 0);
                    }
                }
            }
            message.setInt(NUM_OPERATIONS, numOperations);
        } catch (JMSException e) {
            try {
                Integer.parseInt(currentKey);
                currentKey = "method argument " + currentKey;
            } catch (NumberFormatException nfe) {
                // do nothing
            }
            throw new UnsupportedOperationException("Unable to insert " + currentKey + " into JMS message.", e);
        }

        message.setInt(SERIALIZATION_TYPE_PROPERTY, serializationType);
        return message;
    }

    @Override
    public ServiceMessage convertJmsMessageToServiceMessage(Message msg) throws Exception {
        ServiceMessage serviceMsg = new ServiceMessage();
        MapMessage mapMsg = (MapMessage) msg;
        String currentKey = null;

        try {
            int msgVer = mapMsg.getInt(VERSION);
            if( msgVer != 1 ) { 
                throw new RuntimeException("Unsupported service message version: " + msgVer );
            }
            
            String strValue = mapMsg.getString(DOMAIN_NAME);
            serviceMsg.setDomainName(strValue);
            

            int numOperations = mapMsg.getIntProperty(NUM_OPERATIONS);
            for (int i = 0; i < numOperations; ++i) {
                String methodName = mapMsg.getStringProperty(METHOD_NAME);
                int serviceType = mapMsg.getIntProperty(SERVICE_TYPE);
                Object result = mapMsg.getObjectProperty(RESULT);
                boolean isResponse = result != null;
               
                int numArgs = mapMsg.getIntProperty(NUM_ARGUMENTS);
                Object [] args = new Object[numArgs];
                for( int a = 0; a < numArgs; ++a ) { 
                    currentKey = i + ":" + a;
                    args[a] = mapMsg.getObject(currentKey);
                }
                OperationMessage operation = new OperationMessage(methodName, args, serviceType);
                operation.setResponse(isResponse);
                serviceMsg.addOperation(operation);
            }
        } catch (JMSException e) {
            try {
                Integer.parseInt(currentKey);
                currentKey = "method argument " + currentKey;
            } catch (NumberFormatException nfe) {
                // do nothing
            }
            throw new UnsupportedOperationException("Unable to retrieve " + currentKey + " from JMS message.", e);
        }
        
        return serviceMsg;
    }

    @Override
    public int getSerializationType() {
        return serializationType;
    }
    

}
