package org.drools.grid.remote;

import java.util.Collection;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.command.ExecuteCommand;
import org.drools.command.FinishedCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.common.DefaultFactHandle;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.WorkingMemoryEventListener;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;
import org.drools.grid.internal.commands.RegisterRemoteWorkItemHandlerCommand;
import org.drools.grid.remote.internal.commands.GetWorkItemManagerCommand;
import org.drools.grid.remote.internal.commands.GetWorkingMemoryEntryPointRemoteCommand;
import org.drools.grid.remote.internal.commands.StartProcessRemoteCommand;
import org.drools.runtime.Calendars;
import org.drools.runtime.Channel;
import org.drools.runtime.Environment;
import org.drools.runtime.ExecutionResults;
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

/*
 * @author: salaboy
 */
public class StatefulKnowledgeSessionRemoteClient
    implements
    StatefulKnowledgeSession {

    private GenericNodeConnector connector;
    private MessageSession       messageSession;
    private String               instanceId;

    public StatefulKnowledgeSessionRemoteClient(String instanceId,
                                                GenericNodeConnector connector,
                                                MessageSession messageSession) {
        this.instanceId = instanceId;
        this.connector = connector;
        this.messageSession = messageSession;

    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int fireAllRules() {
        String commandId = "ksession.fireAllRules" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( CommandFactory.newFireAllRules( commandId ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );
        try {
            this.connector.connect();
            Object object = this.connector.write( msg ).getPayload();

            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            this.connector.disconnect();
            //return (Integer) ((ExecutionResults) object).getValue(commandId);
            return (Integer) object;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public int fireAllRules(int max) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int fireAllRules(AgendaFilter agendaFilter) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void fireUntilHalt() {
        // TODO Auto-generated method stub
    }

    public void fireUntilHalt(AgendaFilter agendaFilter) {
        // TODO Auto-generated method stub
    }

    public ExecutionResults execute(Command command) {
        String commandId = "ksession.execute" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new ExecuteCommand( commandId,
                                                                                                      command ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object object = this.connector.write( msg ).getPayload();
            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            this.connector.disconnect();
            return (ExecutionResults) ((ExecutionResults) object).getValue( commandId );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public Environment getEnvironment() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getGlobal(String identifier) {
        String commandId = "ksession.execute" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new GetGlobalCommand( identifier ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object result = this.connector.write( msg ).getPayload();
            if ( result == null ) {
                throw new RuntimeException( "Response was not correctly received = null" );
            }
            this.connector.disconnect();
            return result;

        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public Globals getGlobals() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase getKnowledgeBase() {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends SessionClock> T getSessionClock() {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerExitPoint(String name,
                                  ExitPoint exitPoint) {
        // TODO Auto-generated method stub
    }

    public void setGlobal(String identifier,
                          Object object) {
        String commandId = "ksession.setGlobal" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new SetGlobalCommand( identifier,
                                                                                                        object ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object result = this.connector.write( msg ).getPayload();
            if ( result == null ) {
                throw new RuntimeException( "Response was not correctly received = null" );
            }

            if ( !(result instanceof FinishedCommand) ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            this.connector.disconnect();

        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

    }

    public void unregisterExitPoint(String name) {
        // TODO Auto-generated method stub
    }

    public Agenda getAgenda() {
        // TODO Auto-generated method stub
        return null;
    }

    public QueryResults getQueryResults(String query) {
        // TODO Auto-generated method stub
        return null;
    }

    public QueryResults getQueryResults(String query,
                                        Object[] arguments) {
        // TODO Auto-generated method stub
        return null;
    }

    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint(String name) {
        String commandId = "ksession.getWorkingMemoryEntryPoint" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new GetWorkingMemoryEntryPointRemoteCommand( name ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  name,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object object = this.connector.write( msg ).getPayload();

            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            this.connector.disconnect();
            return new WorkingMemoryEntryPointRemoteClient( name,
                                                            this.connector,
                                                            this.messageSession );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public Collection< ? extends WorkingMemoryEntryPoint> getWorkingMemoryEntryPoints() {
        // TODO Auto-generated method stub
        return null;
    }

    public void halt() {
        // TODO Auto-generated method stub
    }

    public FactHandle getFactHandle(Object object) {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends FactHandle> Collection<T> getFactHandles() {
        // TODO Auto-generated method stub
        return null;
    }

    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getObject(FactHandle factHandle) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Object> getObjects() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<Object> getObjects(ObjectFilter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    public FactHandle insert(Object object) {
        String commandId = "ksession.insert" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new InsertObjectCommand( object,
                                                                                                           true ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object result = this.connector.write( msg ).getPayload();
            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            DefaultFactHandle handle = (DefaultFactHandle) result;

            this.connector.disconnect();
            return handle;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public void retract(FactHandle handle) {
        // TODO Auto-generated method stub
    }

    public void update(FactHandle handle,
                       Object object) {
        // TODO Auto-generated method stub
    }

    public void abortProcessInstance(long id) {
        // TODO Auto-generated method stub
    }

    public ProcessInstance getProcessInstance(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<ProcessInstance> getProcessInstances() {
        // TODO Auto-generated method stub
        return null;
    }

    public WorkItemManager getWorkItemManager() {
        String kresultsId = "kresults_" + this.messageSession.getSessionId();
        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   true,
                                   new KnowledgeContextResolveFromContextCommand( new GetWorkItemManagerCommand(),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );
        try {
            this.connector.connect();
            Object payload = this.connector.write( msg ).getPayload();
            WorkItemManager workItemManager = (WorkItemManager) ((ExecutionResults) payload).getValue( "workItemManager" );
            ((WorkItemManagerRemoteClient) workItemManager).setConnector( this.connector );
            ((WorkItemManagerRemoteClient) workItemManager).setMessageSession( this.messageSession );
            ((WorkItemManagerRemoteClient) workItemManager).setInstanceId( this.instanceId );
            this.connector.disconnect();
            return workItemManager;
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }
    }

    public void registerWorkItemHandler(String name,
                                        String workItemHandler) {

        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new RegisterRemoteWorkItemHandlerCommand( name,
                                                                                                                            workItemHandler ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            this.connector.write( msg );
            this.connector.disconnect();
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

    }

    public void signalEvent(String type,
                            Object event) {
        // TODO Auto-generated method stub
    }

    public ProcessInstance startProcess(String processId) {
        String commandId = "ksession.execute" + this.messageSession.getNextId();
        String kresultsId = "kresults_" + this.messageSession.getSessionId();

        Message msg = new Message( this.messageSession.getSessionId(),
                                   this.messageSession.counter.incrementAndGet(),
                                   false,
                                   new KnowledgeContextResolveFromContextCommand( new StartProcessRemoteCommand( processId ),
                                                                                  null,
                                                                                  null,
                                                                                  this.instanceId,
                                                                                  kresultsId ) );

        try {
            this.connector.connect();
            Object object = this.connector.write( msg ).getPayload();
            if ( object == null ) {
                throw new RuntimeException( "Response was not correctly received" );
            }
            this.connector.disconnect();
            return (ProcessInstance) ((ExecutionResults) object).getValue( processId );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message",
                                        e );
        }

    }

    public ProcessInstance startProcess(String processId,
                                        Map<String, Object> parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addEventListener(WorkingMemoryEventListener listener) {
        // TODO Auto-generated method stub
    }

    public void addEventListener(AgendaEventListener listener) {
        // TODO Auto-generated method stub
    }

    public Collection<AgendaEventListener> getAgendaEventListeners() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<WorkingMemoryEventListener> getWorkingMemoryEventListeners() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeEventListener(WorkingMemoryEventListener listener) {
        // TODO Auto-generated method stub
    }

    public void removeEventListener(AgendaEventListener listener) {
        // TODO Auto-generated method stub
    }

    public void addEventListener(ProcessEventListener listener) {
        // TODO Auto-generated method stub
    }

    public Collection<ProcessEventListener> getProcessEventListeners() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeEventListener(ProcessEventListener listener) {
        // TODO Auto-generated method stub
    }

    public String getEntryPointId() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getFactCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void signalEvent(String type,
                            Object event,
                            long processInstanceId) {
        // TODO Auto-generated method stub
    }

    public Calendars getCalendars() {
        // TODO Auto-generated method stub
        return null;
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

    public LiveQuery openLiveQuery(String query,
                                   Object[] arguments,
                                   ViewChangedEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    
    public KnowledgeSessionConfiguration getSessionConfiguration() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
