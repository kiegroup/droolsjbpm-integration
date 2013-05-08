package org.kie.services.client.api;

import javax.jms.Message;
import javax.jms.Session;

import org.kie.services.client.message.ServiceMessage;

/**
 * This interface contains the methods necessary to retrieve the {@link ServiceMessage} instance
 * or other representations of the message. 
 * </p>
 * The actual code that implements this is implemented in the {@link AbstractApiRequestFactoryImpl}
 * class. 
 * </p>
 * <b>If you change any of the methods here, make sure to modify the {@link AbstractApiRequestFactoryImpl} 
 * instance appropriately.
 */
public interface MessageHolder {

    /**
     * This operation <b>clears and resets</b> the underlying {@link ServiceMessage} instance. 
     * </p>
     * Get the underlying {@link ServiceMessage} instance containing the information 
     * about the operations request.
     * @return {@link ServiceMessage} containing the service operation information stored up until this operation.
     */
    public ServiceMessage getRequest();
    
    /**
     * This operation <b>clears and resets</b> the underlying {@link ServiceMessage} instance. 
     * </p>
     * Create a JMS message that contains the {@link ServiceMessage} info. 
     * @param session The JMS session needed to create a JMS message. 
     * @return A JMS message containing the {@link ServiceMessage} information.
     */
    public Message createJmsMessage(Session session);
    
    /**
     * This operation <b>clears and resets</b> the underlying {@link ServiceMessage} instance. 
     * </p>
     * Create a string containing the (JAXB) XML representation of the {@link ServiceMessage}.
     * @return {@link String} containing (JAXB supported) XML. 
     */
    public String getMessageXmlString();
    
}
