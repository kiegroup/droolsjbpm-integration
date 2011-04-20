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

package org.drools.grid.remote;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import org.drools.FactException;
import org.drools.FactHandle;
import org.drools.WorkingMemoryEntryPoint;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.ObjectFilter;

public class WorkingMemoryEntryPointRemoteClient
    implements
    WorkingMemoryEntryPoint {

    private String                 instanceId;
    private String                 name;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager    cm;

    public WorkingMemoryEntryPointRemoteClient(String instanceId,
                                               String name,
                                               GridServiceDescription gsd,
                                               ConversationManager cm) {
        this.instanceId = instanceId;
        this.name = name;
        this.gsd = gsd;
        this.cm = cm;

    }

    public FactHandle insert(Object object) throws FactException {

        String kresultsId = "kresults_" + this.gsd.getId();

        InsertObjectCommand insertCmd = new InsertObjectCommand( object,
                                                                 true );
        insertCmd.setEntryPoint( name );
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( insertCmd,
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      this.name,
                                                                                                                      kresultsId )} ) );

        Object result = ConversationUtil.sendMessage( this.cm,
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
        return ((FactHandle) result);

    }

    public FactHandle insert(Object object,
                             boolean dynamic) throws FactException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void retract(org.drools.runtime.rule.FactHandle handle) throws FactException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void update(org.drools.runtime.rule.FactHandle handle,
                       Object object) throws FactException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String name) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getEntryPointId() {
        return this.name;
    }

    public org.drools.runtime.rule.FactHandle getFactHandle(Object object) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Object getObject(org.drools.runtime.rule.FactHandle factHandle) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<Object> getObjects() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<Object> getObjects(ObjectFilter filter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public <T extends org.drools.runtime.rule.FactHandle> Collection<T> getFactHandles() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public <T extends org.drools.runtime.rule.FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public long getFactCount() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
