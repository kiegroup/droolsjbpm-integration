/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.grid.remote;

import java.util.Properties;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.command.FinishedCommand;
import org.drools.command.NewKnowledgeBaseCommand;
import org.drools.command.SetVariableCommand;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;

public class KnowledgeBaseProviderRemoteClient
    implements
    KnowledgeBaseFactoryService {
    
    private GenericNodeConnector client;
    private MessageSession          messageSession;
    public KnowledgeBaseProviderRemoteClient(GenericNodeConnector client) {
         this.client = client;
         this.messageSession = new MessageSession();
    }

    public Environment newEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( ( KnowledgeBaseConfiguration ) null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        //return new NewKnowledgeBaseCommand(null);

        String localId = UUID.randomUUID().toString();

        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new SetVariableCommand( "__TEMP__",
                                                           localId,
                                                           new NewKnowledgeBaseCommand( null ) ) );
        try {
            Object object = client.write( msg ).getPayload();

            if ( !(object instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly ended" );
            }

        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

        return new KnowledgeBaseRemoteClient( localId,
                                              client, messageSession );

    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader... classLoader) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

}
