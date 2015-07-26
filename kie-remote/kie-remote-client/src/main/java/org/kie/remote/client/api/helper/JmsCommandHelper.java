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

package org.kie.remote.client.api.helper;

import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.kie.api.command.Command;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.services.client.api.command.InternalJmsCommandHelper;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationProvider;


public class JmsCommandHelper {

    private JmsCommandHelper() { 
        // util class
    }
   
    /**
     * This method can be used to send a command via the JMS interface to business-central or kie-wb. 
     * </p>
     * It takes care of the logic used to communicate via JMS, and will return an object that the given command should 
     * 
     * @param command An instance of the {@link Command} that belongs to the "org.kie.remote.jaxb.gen" package.
     * @param userName
     * @param password
     * @param deploymentId
     * @param processInstanceId
     * @param connectionUserName
     * @param connectionPassword
     * @param factory
     * @param sendQueue
     * @param responseQueue
     * @param extraJaxbClasses
     * @param maxTimeoutInMillisecs
     * @return
     */
    public static Object sendClientJmsCommand( Command command, 
            String userName, String password, String deploymentId, Long processInstanceId, 
            String connectionUserName, String connectionPassword,
            ConnectionFactory factory, Queue sendQueue, Queue responseQueue,
            Set<Class<?>> extraJaxbClasses, long maxTimeoutInMillisecs) {
        
        SerializationProvider serializationProvider = ClientJaxbSerializationProvider.newInstance(extraJaxbClasses);
        
        return sendCoreJmsCommand(command, 
                userName, password, deploymentId, processInstanceId, 
                connectionUserName, connectionPassword,
                factory, sendQueue, responseQueue, 
                serializationProvider, extraJaxbClasses, maxTimeoutInMillisecs);
    }
    
    public static Object sendCoreJmsCommand( Command command, 
            String userName, String password, String deploymentId, Long processInstanceId, 
            String connectionUserName, String connectionPassword,
            ConnectionFactory factory, Queue sendQueue, Queue responseQueue,
            SerializationProvider serializationProvider, Set<Class<?>> extraJaxbClasses, long maxTimeoutInMillisecs) {
        
        return InternalJmsCommandHelper.internalExecuteJmsCommand(command, 
                connectionUserName, connectionPassword,
                userName, password, deploymentId, processInstanceId, null,
                factory, sendQueue, responseQueue, 
                serializationProvider, extraJaxbClasses, JaxbSerializationProvider.JMS_SERIALIZATION_TYPE,
                maxTimeoutInMillisecs);
    }
}
