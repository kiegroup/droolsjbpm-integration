package org.kie.services.client.message.serialization;

import javax.jms.Message;
import javax.jms.Session;

import org.kie.services.client.message.ServiceMessage;

/**
 * This interface defines the methods that all serialization provider implementations must adhere to.
 * </p>
 * All implementations must also set an int property in the JMS messages that they create. 
 * See {@link MessageSerializationProvider.Type} for more info.
 */
public interface MessageSerializationProvider {

    public Message convertServiceMessageToJmsMessage(ServiceMessage request, Session jmsSession) 
            throws Exception;
    
    public ServiceMessage convertJmsMessageToServiceMessage(Message msg) throws Exception;
    
    /**
     * This method is not strictly necessary but serves as an important reminder that 
     * the serialization type int property must be set in messages. 
     * @return
     */
    public int getSerializationType();
    
    public static String SERIALIZATION_TYPE_PROPERTY = "serialization_type";
    
    public enum Type { 
        MAP_MESSAGE(0), JAXB(1), PROTOBUF(2);
        
        private int value;
        
        private Type(int intValue) { 
            this.value = intValue;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
