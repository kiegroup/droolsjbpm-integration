package org.kie.services.client.serialization.jaxb.impl.runtime;

import java.util.List;

import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;

public class JaxbCorrelationKeyFactory implements CorrelationKeyFactory {

    private static final JaxbCorrelationKeyFactory _instance = new JaxbCorrelationKeyFactory();
   
    private JaxbCorrelationKeyFactory() { 
       // private constructor 
    }
    
    public static CorrelationKeyFactory getInstance() { 
        return _instance;
    }
    
    @Override
    public CorrelationKey newCorrelationKey( String businessKey ) {
        if( businessKey == null || businessKey.isEmpty()) {
            throw new IllegalArgumentException("businessKey cannot be empty!");
        }
        
        JaxbCorrelationKey corrKey = new JaxbCorrelationKey();
        JaxbCorrelationProperty corrProp = new JaxbCorrelationProperty(businessKey);
        corrKey.getJaxbProperties().add(corrProp);
        return corrKey;
    }

    @Override
    public CorrelationKey newCorrelationKey( List<String> properties ) {
        if( properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("properties cannot be empty!");
        }

        JaxbCorrelationKey corrKey = new JaxbCorrelationKey();
        for( String businessKey : properties ) { 
            if( businessKey == null || businessKey.isEmpty()) {
                throw new IllegalArgumentException("null or empty properties are not accepted!");
            }
                JaxbCorrelationProperty corrProp = new JaxbCorrelationProperty(businessKey);
                corrKey.getJaxbProperties().add(corrProp);
        }
        return corrKey;
    }

}
