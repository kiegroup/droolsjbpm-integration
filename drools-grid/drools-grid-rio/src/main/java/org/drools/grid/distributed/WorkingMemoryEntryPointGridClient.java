/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.distributed;

import java.util.Collection;
import org.drools.FactException;
import org.drools.FactHandle;
import org.drools.WorkingMemoryEntryPoint;
import org.drools.command.ExecuteCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.InsertObjectInEntryPointCommand;
import org.drools.common.DisconnectedFactHandle;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.ObjectFilter;

/**
 *
 * @author salaboy
 */
public class WorkingMemoryEntryPointGridClient implements WorkingMemoryEntryPoint{

     private GenericNodeConnector     client;
    private MessageSession              messageSession;
    private String                      instanceId;

    public WorkingMemoryEntryPointGridClient(String instanceId, GenericNodeConnector client, MessageSession messageSession) {
        this.client = client;
        this.messageSession = messageSession;
        this.instanceId = instanceId;
    }



    public FactHandle insert(Object object) throws FactException {
         String commandId = "ksession.insert" + messageSession.getNextId();
        String kresultsId = "kresults_" + messageSession.getSessionId();
        System.out.println("INSTANCE ID, ENTRY POINT!!!!! ="+instanceId);
        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new InsertObjectInEntryPointCommand(object, String.valueOf(object.hashCode()) ),
                                                                                  null,
                                                                                  null,
                                                                                  null,
                                                                                  instanceId,
                                                                                  kresultsId ) );

        try {
            Object result = client.write( msg ).getPayload();
            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            FactHandle handle = new DisconnectedFactHandle(((ExecutionResults) result).getFactHandle( String.valueOf(object.hashCode()) ).toString()) ;
            
            return  handle;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public FactHandle insert(Object object, boolean dynamic) throws FactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void retract(org.drools.runtime.rule.FactHandle handle) throws FactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(org.drools.runtime.rule.FactHandle handle, Object object) throws FactException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEntryPointId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public org.drools.runtime.rule.FactHandle getFactHandle(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getObject(org.drools.runtime.rule.FactHandle factHandle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Object> getObjects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Object> getObjects(ObjectFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends org.drools.runtime.rule.FactHandle> Collection<T> getFactHandles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends org.drools.runtime.rule.FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFactCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
