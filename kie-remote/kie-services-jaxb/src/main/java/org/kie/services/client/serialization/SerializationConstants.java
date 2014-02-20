package org.kie.services.client.serialization;

public class SerializationConstants {

    /**
     * It would be nice to use "standard" values for the property names below (such as "org.kie.remote.jms.serialiation.type"),
     * but hornetq (and probably other JMS providers) complain if the property name  is not a 'valid Java identifier'. 
     * </p>
     * The 'valid Java identifier' specification is probably the one described in the JLS and defined by the Character
     * static methods that have related names (  Character.isJavaIdentifierPart(ch), etc. ).
     */
    
    public static final String SERIALIZATION_TYPE_PROPERTY_NAME = "serialization";
    public static final String EXTRA_JAXB_CLASSES_PROPERTY_NAME = "extraJaxbClasses";
    public static final String DEPLOYMENT_ID_PROPERTY_NAME = "deploymentId";
    
}
