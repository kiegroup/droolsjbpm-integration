package org.kie.services.client.api.command;

import static org.kie.remote.client.jaxb.ConversionUtil.convertMapToJaxbStringObjectPairArray;

import java.util.Collection;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.api.time.SessionClock;
import org.kie.remote.jaxb.gen.AbortProcessInstanceCommand;
import org.kie.remote.jaxb.gen.AbortWorkItemCommand;
import org.kie.remote.jaxb.gen.CompleteWorkItemCommand;
import org.kie.remote.jaxb.gen.DisconnectedFactHandle;
import org.kie.remote.jaxb.gen.FireAllRulesCommand;
import org.kie.remote.jaxb.gen.GetFactCountCommand;
import org.kie.remote.jaxb.gen.GetGlobalCommand;
import org.kie.remote.jaxb.gen.GetProcessInstanceCommand;
import org.kie.remote.jaxb.gen.GetProcessInstancesCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.SetGlobalCommand;
import org.kie.remote.jaxb.gen.SignalEventCommand;
import org.kie.remote.jaxb.gen.StartProcessCommand;
import org.kie.remote.jaxb.gen.TraitTypeEnum;
import org.kie.remote.jaxb.gen.UpdateCommand;
import org.kie.services.client.api.command.exception.MissingRequiredInfoException;

public class KieSessionClientCommandObject extends AbstractRemoteCommandObject implements KieSession {

    private WorkItemManager workItemManager;
    
    public KieSessionClientCommandObject(RemoteConfiguration config) {
        super(config);
        if( config.isJms() && config.getKsessionQueue() == null ) { 
            throw new MissingRequiredInfoException("A KieSession queue is necessary in order to create a Remote JMS Client KieSession instance.");
        }
    }
    
    @Override
    public int fireAllRules() {
        return (Integer) executeCommand(new FireAllRulesCommand());
    }

    @Override
    public int fireAllRules( int max ) {
        FireAllRulesCommand cmd = new FireAllRulesCommand();
        cmd.setMax(max);
        return (Integer) executeCommand(new FireAllRulesCommand());
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter ) {
        return unsupported(KieSession.class, Integer.class);
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter, int max ) {
        return unsupported(KieSession.class, Integer.class);
    }

    @Override
    public void fireUntilHalt() {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public void fireUntilHalt( AgendaFilter agendaFilter ) {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public <T> T execute( Command<T> command ) {
        return (T) unsupported(KieSession.class, Object.class);
    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return (T) unsupported(KieSession.class, Object.class);
    }

    @Override
    public void setGlobal( String identifier, Object value ) {
        SetGlobalCommand cmd = new SetGlobalCommand();
        cmd.setIdentifier(identifier);
        cmd.setObject(value);
        executeCommand(cmd);
    }

    @Override
    public Object getGlobal( String identifier ) {
        GetGlobalCommand cmd = new GetGlobalCommand();
        cmd.setIdentifier(identifier);
        return executeCommand(cmd);
    }

    @Override
    public Globals getGlobals() {
        return unsupported(KieSession.class, Globals.class);
    }

    @Override
    public Calendars getCalendars() {
        return unsupported(KieSession.class, Calendars.class);
    }

    @Override
    public Environment getEnvironment() {
        return unsupported(KieSession.class, Environment.class);
    }

    @Override
    public KieBase getKieBase() {
        return unsupported(KieSession.class, KieBase.class);
    }

    @Override
    public void registerChannel( String name, Channel channel ) {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public void unregisterChannel( String name ) {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public Map<String, Channel> getChannels() {
        return unsupported(KieSession.class, Map.class);
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        return unsupported(KieSession.class, KieSessionConfiguration.class);
    }

    @Override
    public void halt() {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public Agenda getAgenda() {
        return unsupported(KieSession.class, Agenda.class);
    }

    @Override
    public EntryPoint getEntryPoint( String name ) {
        return unsupported(KieSession.class, EntryPoint.class);
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return unsupported(KieSession.class, Collection.class);
    }

    @Override
    public QueryResults getQueryResults( String query, Object... arguments ) {
        return unsupported(KieSession.class, QueryResults.class);
    }

    @Override
    public LiveQuery openLiveQuery( String query, Object[] arguments, ViewChangedEventListener listener ) {
        return unsupported(KieSession.class, LiveQuery.class);
    }

    @Override
    public String getEntryPointId() {
        return unsupported(KieSession.class, String.class);
    }

    @Override
    public FactHandle insert( Object object ) {
        return unsupported(KieSession.class, FactHandle.class);
    }

    @Override
    public void retract( FactHandle handle ) {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public void delete( FactHandle handle ) {
        unsupported(KieSession.class, Void.class);
    }

    @Override
    public void update( FactHandle handle, Object object ) {
        UpdateCommand cmd = new UpdateCommand();
        if( ! handle.getClass().getSimpleName().startsWith("Disconnected") ) { 
            String methodName = (new Throwable()).getStackTrace()[1].getMethodName();
            throw new UnsupportedOperationException("The " + methodName + " on the Remote Client " + KieSession.class.getSimpleName()
                   + " only accepts DisconnectedFactHandle instances." );
        }
        cmd.setObject(object);
        DisconnectedFactHandle discFactHandle = factHandleToGenDisconnectedFactHandle(handle);
        cmd.setHandle(discFactHandle);
        executeCommand(cmd);
    }

    static DisconnectedFactHandle factHandleToGenDisconnectedFactHandle(FactHandle handle) { 
        DisconnectedFactHandle discHandle = new DisconnectedFactHandle();
       try { 
          Class origDiscFactHandleClass = Class.forName("org.drools.core.common.DisconnectedFactHandle");
          discHandle.setId( getField("id", origDiscFactHandleClass, discHandle, Integer.class));
          discHandle.setIdentityHashCode(getField("identityHashCode", origDiscFactHandleClass, discHandle, Integer.class));
          discHandle.setObjectHashCode(getField("objectHashCode", origDiscFactHandleClass, discHandle, Integer.class));
          discHandle.setRecency(getField("recency", origDiscFactHandleClass, discHandle, Long.class));
          discHandle.setObject(getField("object", origDiscFactHandleClass, discHandle, Object.class));
          Object obj = getField("traitType", origDiscFactHandleClass, discHandle, Object.class);
          discHandle.setTraitType(TraitTypeEnum.fromValue(obj.toString()));
       } catch( Exception e ) { 
          throw new RuntimeException("Unable to serialize fact handle :" + e.getMessage(), e); 
       }
       return discHandle;
    }

    @Override
    public FactHandle getFactHandle( Object object ) {
        return unsupported(KieSession.class, FactHandle.class);
    }

    @Override
    public Object getObject( FactHandle factHandle ) {
        return unsupported(KieSession.class, Object.class);
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return unsupported(KieSession.class, Collection.class);
    }

    @Override
    public Collection<? extends Object> getObjects( ObjectFilter filter ) {
        return unsupported(KieSession.class, Collection.class);
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return unsupported(KieSession.class, Collection.class);
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles( ObjectFilter filter ) {
        return unsupported(KieSession.class, Collection.class);
    }

    @Override
    public long getFactCount() {
        return (Long) executeCommand(new GetFactCountCommand());
    }

    @Override
    public ProcessInstance startProcess( String processId ) {
        StartProcessCommand cmd = new StartProcessCommand();
        cmd.setProcessId(processId);
        return (ProcessInstance) executeCommand(cmd);
    }

    @Override
    public ProcessInstance startProcess( String processId, Map<String, Object> parameters ) {
        StartProcessCommand cmd = new StartProcessCommand();
        cmd.setProcessId(processId);
        JaxbStringObjectPairArray arrayMap = convertMapToJaxbStringObjectPairArray(parameters);
        cmd.setParameter(arrayMap);
        return (ProcessInstance) executeCommand(cmd);
    }

    @Override
    public ProcessInstance createProcessInstance( String processId, Map<String, Object> parameters ) {
        return unsupported(KieSession.class, ProcessInstance.class);
    }

    @Override
    public ProcessInstance startProcessInstance( long processInstanceId ) {
        return unsupported(KieSession.class, ProcessInstance.class);
    }

    @Override
    public void signalEvent( String type, Object event ) {
        SignalEventCommand cmd = new SignalEventCommand();
        cmd.setEvent(event);
        cmd.setEventType(type);
        executeCommand(cmd);
    }

    @Override
    public void signalEvent( String type, Object event, long processInstanceId ) {
        SignalEventCommand cmd = new SignalEventCommand();
        cmd.setEvent(event);
        cmd.setEventType(type);
        cmd.setProcessInstanceId(processInstanceId);
        executeCommand(cmd);
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return (Collection<ProcessInstance>) executeCommand(new GetProcessInstancesCommand());
    }

    @Override
    public ProcessInstance getProcessInstance( long processInstanceId ) {
        GetProcessInstanceCommand cmd = new GetProcessInstanceCommand();
        cmd.setProcessInstanceId(processInstanceId);
        cmd.setReadOnly(true);
        return (ProcessInstance) executeCommand(cmd);
    }

    @Override
    public ProcessInstance getProcessInstance( long processInstanceId, boolean readonly ) {
        GetProcessInstanceCommand cmd = new GetProcessInstanceCommand();
        cmd.setProcessInstanceId(processInstanceId);
        cmd.setReadOnly(readonly);
        return (ProcessInstance) executeCommand(cmd);
    }

    @Override
    public void abortProcessInstance( long processInstanceId ) {
        AbortProcessInstanceCommand cmd = new AbortProcessInstanceCommand();
        cmd.setProcessInstanceId(processInstanceId);
        executeCommand(cmd);
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        if( this.workItemManager == null ) { 
           this.workItemManager = new WorkItemManager() {
            
            @Override
            public void registerWorkItemHandler( String workItemName, WorkItemHandler handler ) {
                unsupported(WorkItemManager.class, Void.class);
            }
            
            @Override
            public void completeWorkItem( long id, Map<String, Object> results ) {
                CompleteWorkItemCommand cmd = new CompleteWorkItemCommand();
                cmd.setId(id);
                JaxbStringObjectPairArray arrayMap = convertMapToJaxbStringObjectPairArray(results);
                cmd.setResult(arrayMap);
                executeCommand(cmd);
            }
            
            @Override
            public void abortWorkItem( long id ) {
                AbortWorkItemCommand cmd = new AbortWorkItemCommand();
                cmd.setId(id);
                executeCommand(cmd);
            }
        }; 
        }
        return this.workItemManager;
    }

    @Override
    public KieRuntimeLogger getLogger() {
        return unsupported(WorkItemManager.class, KieRuntimeLogger.class);
    }

    @Override
    public void addEventListener( RuleRuntimeEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);
    }

    @Override
    public void removeEventListener( RuleRuntimeEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);
    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return unsupported(WorkItemManager.class, Collection.class);
    }

    @Override
    public void addEventListener( AgendaEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);

    }

    @Override
    public void removeEventListener( AgendaEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);
    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return unsupported(WorkItemManager.class, Collection.class);
    }

    @Override
    public void addEventListener( ProcessEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);
    }

    @Override
    public void removeEventListener( ProcessEventListener listener ) {
        unsupported(WorkItemManager.class, Void.class);
    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        return unsupported(WorkItemManager.class, Collection.class);
    }

    @Override
    public int getId() {
        return unsupported(WorkItemManager.class, Integer.class);
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Dispose does not need to be called on the Remote Client  " + KieSession.class.getSimpleName() + " implementation.");
    }

    @Override
    public void destroy() {
        unsupported(WorkItemManager.class, Void.class);
    }

}
