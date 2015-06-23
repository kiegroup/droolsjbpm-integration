/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
