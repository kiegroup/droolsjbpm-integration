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


import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.drools.core.command.ExecuteCommand;
import org.drools.core.command.GetSessionClockCommand;
import org.drools.core.command.KnowledgeContextResolveFromContextCommand;
import org.drools.core.command.SetVariableCommandFromCommand;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.command.runtime.GetFactCountCommand;
import org.drools.core.command.runtime.GetGlobalsCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.GetProcessInstancesCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.FireUntilHaltCommand;
import org.drools.core.command.runtime.rule.GetFactHandleCommand;
import org.drools.core.command.runtime.rule.GetFactHandlesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.command.runtime.rule.HaltCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.AsyncBatchExecutionCommandImpl;
import org.drools.grid.remote.command.GetWorkingMemoryEntryPointRemoteCommand;
import org.drools.grid.remote.command.QueryRemoteCommand;
import org.kie.KnowledgeBase;
import org.kie.api.command.Command;
import org.kie.api.command.CommandFactory;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.SessionEntryPoint;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.time.SessionClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatefulKnowledgeSessionRemoteClient
    implements
    StatefulKnowledgeSession {
    
    private static Logger logger = LoggerFactory.getLogger(StatefulKnowledgeSessionRemoteClient.class);
    
    private String                           instanceId;
    private GridServiceDescription<GridNode> gsd;
    private ConversationManager              cm;
    private KnowledgeSessionConfigurationRemoteClient conf;
    
    private Long timeout;
    private Long minWaitTime;
    
    
    public StatefulKnowledgeSessionRemoteClient(String localId,
                                                GridServiceDescription gsd,
                                                ConversationManager cm,
                                                KnowledgeSessionConfigurationRemoteClient conf) {
        this.instanceId = localId;
        this.gsd = gsd;
        this.cm = cm;
        this.conf = conf;
        
        //Configure timeouts
        if (this.conf != null){
            String configuredTimeout = this.conf.getProperty(KnowledgeSessionConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT);
            if (configuredTimeout != null){
                timeout = Long.parseLong(configuredTimeout);
            }
            String configuredMinWaitTime = this.conf.getProperty(KnowledgeSessionConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME);
            if (configuredMinWaitTime != null){
                minWaitTime = Long.parseLong(configuredMinWaitTime);
            }
        }
        
    }

    public int getId() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void dispose() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new DisposeCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId ), this.instanceId} ) );

        this.sendMessage(cmd);
    }

    public int fireAllRules() {
        return fireAllRules( -1 );

    }

    public int fireAllRules(int max) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( CommandFactory.newFireAllRules( max ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        return (Integer)this.sendMessage(cmd);

    }

    public int fireAllRules(AgendaFilter agendaFilter) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand( agendaFilter ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        return (Integer) this.sendMessage(cmd);
    }

    public int fireAllRules(AgendaFilter agendaFilter,
                            int max) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new FireAllRulesCommand( agendaFilter,
                                                                                                                                               max ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        return (Integer) this.sendMessage(cmd);
    }

    public void fireUntilHalt() {
        fireUntilHalt( null );
    }

    public void fireUntilHalt(AgendaFilter agendaFilter) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new FireUntilHaltCommand( agendaFilter ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public <T> T execute(Command<T> command) {

        String kresultsId = "kresults_" + this.gsd.getId();
        if(logger.isDebugEnabled()){
            logger.debug(" ### Calling Execute from the SKS Remote Client with instanceId: "+this.instanceId);
        }
       
        Object result = null;
        if(command instanceof AsyncBatchExecutionCommandImpl){
             CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new ExecuteCommand( command , false),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId ),this.instanceId} ) );
            ConversationUtil.sendAsyncMessage( this.cm,
                                                      (InetSocketAddress) this.gsd.getAddresses().get( "socket" ).getObject(),
                                                      this.gsd.getId(),
                                                      cmd );
            // Result must be null if it's async
        }else{
             CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new ExecuteCommand( command , true),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId ),this.instanceId} ) );
            result = this.sendMessage(cmd);
            
            if(logger.isDebugEnabled()){
             logger.debug(" ### Execute Method Returns: "+result);
            }
        }
        
        return (T) result;

    }

    public <T extends SessionClock> T getSessionClock() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetSessionClockCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);
        
        return (T) result;
    }

    public void setGlobal(String identifier,
                          Object object) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( CommandFactory.newSetGlobal( identifier,
                                                                                                                                                   object ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public Object getGlobal(String identifier) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( CommandFactory.newGetGlobal( identifier ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return result;
    }

    public Globals getGlobals() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetGlobalsCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (Globals) result;
    }

    public Calendars getCalendars() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Environment getEnvironment() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgeBase getKieBase() {
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

    public KieSessionConfiguration getSessionConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void halt() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new HaltCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public Agenda getAgenda() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public SessionEntryPoint getEntryPoint(String name) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetWorkingMemoryEntryPointRemoteCommand( name ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      name,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);

        return new WorkingMemoryEntryPointRemoteClient( this.instanceId,
                                                        name,
                                                        this.gsd,
                                                        this.cm );
    }

    public Collection< ? extends SessionEntryPoint> getEntryPoints() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public QueryResults getQueryResults(String query,
                                        Object... arguments) {

        String localId = "query_" + UUID.randomUUID().toString();
        String kresultsId = "kresults_" + this.gsd.getId();
        
        
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{ new SetVariableCommandFromCommand("__TEMP__", localId, 
                                                                                new KnowledgeContextResolveFromContextCommand( new QueryRemoteCommand( localId ,
                                                                                                                                                        query, false,
                                                                                                                                                        arguments ),
                                                                                                                                                         null,
                                                                                                                                                         null,
                                                                                                                                                        this.instanceId,
                                                                                                                                                         kresultsId ))  } ) );

        this.sendMessage(cmd);

        return new QueryResultsRemoteClient( query,
                                             this.instanceId,
                                             localId,
                                             this.gsd,
                                             this.cm );

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
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( object,
                                                                                                                                               true ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (FactHandle) result;
    }

    public void retract(FactHandle handle) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( CommandFactory.newDelete( handle ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public void delete(FactHandle handle) {
        retract(handle);
    }

    public void update(FactHandle handle,
                       Object object) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new UpdateCommand( handle,
                                                                                                                                         object ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public FactHandle getFactHandle(Object object) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetFactHandleCommand( object,
                                                                                                                                                true ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (FactHandle) result;
    }

    public Object getObject(FactHandle factHandle) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetObjectCommand( factHandle ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return result;
    }

    public Collection<Object> getObjects() {
        return getObjects( null );
    }

    public Collection<Object> getObjects(ObjectFilter filter) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetObjectsCommand( filter ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (Collection<Object>) result;
    }

    public <T extends FactHandle> Collection<T> getFactHandles() {
        return getFactHandles( null );
    }

    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetFactHandlesCommand( filter,
                                                                                                                                                 true ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);
        
        return (Collection<T>) result;
    }

    public long getFactCount() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetFactCountCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (Long) result;
    }

    public ProcessInstance startProcess(String processId) {
        return startProcess( processId,
                             null );
    }

    public ProcessInstance startProcess(String processId,
                                        Map<String, Object> parameters) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new StartProcessCommand( processId,
                                                                                                                                               parameters ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (ProcessInstance) result;
    }

    public ProcessInstance createProcessInstance(String processId,
                                                 Map<String, Object> parameters) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new CreateProcessInstanceCommand( processId,
                                                                                                                                                        parameters ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (ProcessInstance) result;
    }

    public ProcessInstance startProcessInstance(long processInstanceId) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new StartProcessInstanceCommand( processInstanceId ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (ProcessInstance) result;
    }

    public void signalEvent(String type,
                            Object event) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new SignalEventCommand( type,
                                                                                                                                              event ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public void signalEvent(String type,
                            Object event,
                            long processInstanceId) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new SignalEventCommand( type,
                                                                                                                                              event ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);
    }

    public Collection<ProcessInstance> getProcessInstances() {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetProcessInstancesCommand(),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (Collection<ProcessInstance>) result;
    }

    public ProcessInstance getProcessInstance(long processInstanceId) {
        String kresultsId = "kresults_" + this.gsd.getId();
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new GetProcessInstanceCommand( processInstanceId ),
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        Object result = this.sendMessage(cmd);

        return (ProcessInstance) result;
    }

    public void abortProcessInstance(long processInstanceId) {

        String kresultsId = "kresults_" + this.gsd.getId();
        AbortProcessInstanceCommand cmdAbort = new AbortProcessInstanceCommand();
        cmdAbort.setProcessInstanceId( processInstanceId );
        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( cmdAbort,
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      this.instanceId,
                                                                                                                      kresultsId )} ) );

        this.sendMessage(cmd);

    }

    public WorkItemManager getWorkItemManager() {

        return new WorkItemManagerRemoteClient( this.instanceId,
                                                this.gsd,
                                                this.cm );

    }

    private Object sendMessage(Object body){
        
        //send the message
        return ConversationUtil.sendMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                body,
                minWaitTime,
                timeout);
        
        
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

    public String getInstanceId() {
        return instanceId;
    }

}
