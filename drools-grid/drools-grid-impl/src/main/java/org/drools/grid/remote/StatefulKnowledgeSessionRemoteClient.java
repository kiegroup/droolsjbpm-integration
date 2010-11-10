/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */

package org.drools.grid.remote;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.runtime.Calendars;
import org.drools.runtime.Channel;
import org.drools.runtime.Environment;
import org.drools.runtime.ExitPoint;
import org.drools.runtime.Globals;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItemManager;
import org.drools.runtime.rule.Agenda;
import org.drools.runtime.rule.AgendaFilter;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.LiveQuery;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.ViewChangedEventListener;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionClock;

/**
 *
 * @author salaboy
 */
public class StatefulKnowledgeSessionRemoteClient
    implements
    StatefulKnowledgeSession {
    private String                 instanceId;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager    cm;

    public StatefulKnowledgeSessionRemoteClient(String localId,
                                                GridServiceDescription gsd,
                                                ConversationManager cm) {
        this.instanceId = localId;
        this.gsd = gsd;
        this.cm = cm;
    }

    public int getId() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void dispose() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public int fireAllRules() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( CommandFactory.newFireAllRules(),
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       this.instanceId,
                                                                                                                       kresultsId ) } ) );

        Object result = sendMessage( this.cm,
                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                     this.gsd.getId(),
                                     cmd );

        return (Integer) result;

        //         String commandId = "ksession.fireAllRules" + this.messageSession.getNextId();
        //        String kresultsId = "kresults_" + this.messageSession.getSessionId();
        //
        //        Message msg = new Message( this.messageSession.getSessionId(),
        //                                   this.messageSession.counter.incrementAndGet(),
        //                                   false,
        //                                   new KnowledgeContextResolveFromContextCommand( CommandFactory.newFireAllRules( commandId ),
        //                                                                                  null,
        //                                                                                  null,
        //                                                                                  this.instanceId,
        //                                                                                  kresultsId ) );
        //        try {
        //            this.connector.connect();
        //            Object object = this.connector.write( msg ).getPayload();
        //
        //            if ( object == null ) {
        //                throw new RuntimeException( "Response was not correctly received" );
        //            }
        //            this.connector.disconnect();
        //            //return (Integer) ((ExecutionResults) object).getValue(commandId);
        //            return (Integer) object;
        //        } catch ( Exception e ) {
        //            throw new RuntimeException( "Unable to execute message",
        //                                        e );
        //        }
    }

    public int fireAllRules(int max) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public int fireAllRules(AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void fireUntilHalt() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void fireUntilHalt(AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public <T> T execute(Command<T> command) {
        //        String localId = UUID.randomUUID().toString();
        //        String commandId = "ksession.execute" + this.gsd.getId();
        //        String kresultsId = "kresults_" + this.gsd.getId();
        //        CommandImpl cmd = new CommandImpl("execute",
        //                Arrays.asList(new Object[]{new KnowledgeContextResolveFromContextCommand( new ExecuteCommand( command ),
        //                                                                                  null,
        //                                                                                  null,
        //                                                                                  this.instanceId,
        //                                                                                  kresultsId )}));
        //        
        //        Object result = sendMessage(this.cm,
        //                (InetSocketAddress[]) this.gsd.getAddresses().get("socket").getObject(),
        //                this.gsd.getServiceInterface().getName(),
        //                cmd);
        //        
        //        
        //        
        //        return (T) result;

        //        String commandId = "ksession.execute" + this.messageSession.getNextId();
        //        String kresultsId = "kresults_" + this.messageSession.getSessionId();
        //
        //        Message msg = new Message( this.messageSession.getSessionId(),
        //                                   this.messageSession.counter.incrementAndGet(),
        //                                   false,
        //                                   new KnowledgeContextResolveFromContextCommand( new ExecuteCommand( commandId,
        //                                                                                                      command ),
        //                                                                                  null,
        //                                                                                  null,
        //                                                                                  this.instanceId,
        //                                                                                  kresultsId ) );
        //
        //        try {
        //            this.connector.connect();
        //            Object object = this.connector.write( msg ).getPayload();
        //            if ( object == null ) {
        //                throw new RuntimeException( "Response was not correctly received" );
        //            }
        //            this.connector.disconnect();
        //            return (ExecutionResults) ((ExecutionResults) object).getValue( commandId );
        //        } catch ( Exception e ) {
        //            throw new RuntimeException( "Unable to execute message",
        //                                        e );
        //        }
        throw new UnsupportedOperationException( "Not supported yet." );

    }

    public <T extends SessionClock> T getSessionClock() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void setGlobal(String identifier,
                          Object object) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Object getGlobal(String identifier) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Globals getGlobals() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Calendars getCalendars() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Environment getEnvironment() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase getKnowledgeBase() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void registerExitPoint(String name,
                                  ExitPoint exitPoint) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void unregisterExitPoint(String name) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void registerChannel(String name,
                                Channel channel) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void unregisterChannel(String name) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Map<String, Channel> getChannels() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeSessionConfiguration getSessionConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void halt() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Agenda getAgenda() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String name) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection< ? extends WorkingMemoryEntryPoint> getWorkingMemoryEntryPoints() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public QueryResults getQueryResults(String query,
                                        Object... arguments) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public LiveQuery openLiveQuery(String query,
                                   Object[] arguments,
                                   ViewChangedEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getEntryPointId() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public FactHandle insert(Object object) {

        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( object,
                                                                                                                                                true ),
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       this.instanceId,
                                                                                                                       kresultsId ) } ) );

        Object result = sendMessage( this.cm,
                                     (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                     this.gsd.getId(),
                                     cmd );

        return (FactHandle) result;
    }

    public void retract(FactHandle handle) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void update(FactHandle handle,
                       Object object) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public FactHandle getFactHandle(Object object) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Object getObject(FactHandle factHandle) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<Object> getObjects() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<Object> getObjects(ObjectFilter filter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public <T extends FactHandle> Collection<T> getFactHandles() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public long getFactCount() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public ProcessInstance startProcess(String processId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public ProcessInstance startProcess(String processId,
                                        Map<String, Object> parameters) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void signalEvent(String type,
                            Object event) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void signalEvent(String type,
                            Object event,
                            long processInstanceId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<ProcessInstance> getProcessInstances() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public ProcessInstance getProcessInstance(long processInstanceId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void abortProcessInstance(long processInstanceId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public WorkItemManager getWorkItemManager() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void addEventListener(WorkingMemoryEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeEventListener(WorkingMemoryEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<WorkingMemoryEventListener> getWorkingMemoryEventListeners() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void addEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<AgendaEventListener> getAgendaEventListeners() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void addEventListener(ProcessEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeEventListener(ProcessEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<ProcessEventListener> getProcessEventListeners() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public static Object sendMessage(ConversationManager conversationManager,
                                     Serializable addr,
                                     String id,
                                     Object body) {

        InetSocketAddress[] sockets = null;
        if ( addr instanceof InetSocketAddress[] ) {
            sockets = (InetSocketAddress[]) addr;
        } else if ( addr instanceof InetSocketAddress ) {
            sockets = new InetSocketAddress[ 1 ];
            sockets[0] = (InetSocketAddress) addr;
        }

        BlockingMessageResponseHandler handler = new BlockingMessageResponseHandler();
        Exception exception = null;
        for ( InetSocketAddress socket : sockets ) {
            try {
                Conversation conv = conversationManager.startConversation( socket,
                                                                           id );
                conv.sendMessage( body,
                                  handler );
                exception = null;
            } catch ( Exception e ) {
                exception = e;
                conversationManager.endConversation();
            }
            if ( exception == null ) {
                break;
            }
        }
        if ( exception != null ) {
            throw new RuntimeException( "Unable to send message",
                                        exception );
        }
        try {
            return handler.getMessage().getBody();
        } finally {
            conversationManager.endConversation();
        }
    }
}
