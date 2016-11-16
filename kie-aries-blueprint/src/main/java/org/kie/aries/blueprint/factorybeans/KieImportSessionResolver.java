/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.aries.blueprint.factorybeans;

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
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.api.time.SessionClock;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

import java.util.Collection;
import java.util.Map;

public class KieImportSessionResolver extends AbstractKieObjectsResolver implements KieSession, StatelessKieSession {

    private final String name;
    private KieSession kieSession;
    private StatelessKieSession statelessKieSession;

    public KieImportSessionResolver( String name ) {
        super(null);
        this.name = name;
    }

    void setSession( Object session ) {
        if (session instanceof KieSession) {
            this.kieSession = ( (KieSession) session );
        } else {
            this.statelessKieSession = ( (StatelessKieSession) session );
        }
    }

    @Override
    public Object init( BlueprintContextHelper context ) {
        return this;
    }

    @Override
    public int getId() {
        return kieSession.getId();
    }

    @Override
    public long getIdentifier() {
        return kieSession.getIdentifier();
    }

    @Override
    public void dispose() {
        kieSession.dispose();
    }

    @Override
    public void destroy() {
        kieSession.destroy();
    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return kieSession.getSessionClock();
    }

    @Override
    public void setGlobal( String s, Object o ) {
        kieSession.setGlobal( s, o );
    }

    @Override
    public Object getGlobal( String s ) {
        return kieSession.getGlobal( s );
    }

    @Override
    public Globals getGlobals() {
        return kieSession.getGlobals();
    }

    @Override
    public Calendars getCalendars() {
        return kieSession.getCalendars();
    }

    @Override
    public Environment getEnvironment() {
        return kieSession.getEnvironment();
    }

    @Override
    public KieBase getKieBase() {
        return kieSession.getKieBase();
    }

    @Override
    public void registerChannel( String s, Channel channel ) {
        kieSession.registerChannel( s, channel );
    }

    @Override
    public void unregisterChannel( String s ) {
        kieSession.unregisterChannel( s );
    }

    @Override
    public Map<String, Channel> getChannels() {
        return kieSession.getChannels();
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        return kieSession.getSessionConfiguration();
    }

    @Override
    public KieRuntimeLogger getLogger() {
        return kieSession.getLogger();
    }

    @Override
    public void addEventListener( ProcessEventListener processEventListener ) {
        kieSession.addEventListener( processEventListener );
    }

    @Override
    public void removeEventListener( ProcessEventListener processEventListener ) {
        kieSession.removeEventListener( processEventListener );
    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        return kieSession.getProcessEventListeners();
    }

    @Override
    public void addEventListener( RuleRuntimeEventListener ruleRuntimeEventListener ) {
        kieSession.addEventListener( ruleRuntimeEventListener );
    }

    @Override
    public void removeEventListener( RuleRuntimeEventListener ruleRuntimeEventListener ) {
        kieSession.removeEventListener( ruleRuntimeEventListener );
    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return kieSession.getRuleRuntimeEventListeners();
    }

    @Override
    public void addEventListener( AgendaEventListener agendaEventListener ) {
        kieSession.addEventListener( agendaEventListener );
    }

    @Override
    public void removeEventListener( AgendaEventListener agendaEventListener ) {
        kieSession.removeEventListener( agendaEventListener );
    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return kieSession.getAgendaEventListeners();
    }

    @Override
    public <T> T execute( Command<T> command ) {
        return kieSession.execute( command );
    }

    @Override
    public ProcessInstance startProcess( String s ) {
        return kieSession.startProcess( s );
    }

    @Override
    public ProcessInstance startProcess( String s, Map<String, Object> map ) {
        return kieSession.startProcess( s, map );
    }

    @Override
    public ProcessInstance createProcessInstance( String s, Map<String, Object> map ) {
        return kieSession.createProcessInstance( s, map );
    }

    @Override
    public ProcessInstance startProcessInstance( long l ) {
        return kieSession.startProcessInstance( l );
    }

    @Override
    public void signalEvent( String s, Object o ) {
        kieSession.signalEvent( s, o );
    }

    @Override
    public void signalEvent( String s, Object o, long l ) {
        kieSession.signalEvent( s, o, l );
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return kieSession.getProcessInstances();
    }

    @Override
    public ProcessInstance getProcessInstance( long l ) {
        return kieSession.getProcessInstance( l );
    }

    @Override
    public ProcessInstance getProcessInstance( long l, boolean b ) {
        return kieSession.getProcessInstance( l, b );
    }

    @Override
    public void abortProcessInstance( long l ) {
        kieSession.abortProcessInstance( l );
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return kieSession.getWorkItemManager();
    }

    @Override
    public void halt() {
        kieSession.halt();
    }

    @Override
    public Agenda getAgenda() {
        return kieSession.getAgenda();
    }

    @Override
    public EntryPoint getEntryPoint( String s ) {
        return kieSession.getEntryPoint( s );
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return kieSession.getEntryPoints();
    }

    @Override
    public QueryResults getQueryResults( String s, Object... objects ) {
        return kieSession.getQueryResults( s, objects );
    }

    @Override
    public LiveQuery openLiveQuery( String s, Object[] objects, ViewChangedEventListener viewChangedEventListener ) {
        return kieSession.openLiveQuery( s, objects, viewChangedEventListener );
    }

    @Override
    public String getEntryPointId() {
        return kieSession.getEntryPointId();
    }

    @Override
    public FactHandle insert( Object o ) {
        return kieSession.insert( o );
    }

    @Override
    public void retract( FactHandle factHandle ) {
        kieSession.retract( factHandle );
    }

    @Override
    public void delete( FactHandle factHandle ) {
        kieSession.delete( factHandle );
    }

    @Override
    public void delete( FactHandle factHandle, FactHandle.State state ) {
        kieSession.delete( factHandle, state );
    }

    @Override
    public void update( FactHandle factHandle, Object o ) {
        kieSession.update( factHandle, o );
    }

    @Override
    public void update( FactHandle factHandle, Object o, String... modifiedProperties ) {
        kieSession.update( factHandle, o, modifiedProperties );
    }

    @Override
    public FactHandle getFactHandle( Object o ) {
        return kieSession.getFactHandle(  o );
    }

    @Override
    public Object getObject( FactHandle factHandle ) {
        return kieSession.getObject( factHandle );
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return kieSession.getObjects();
    }

    @Override
    public Collection<? extends Object> getObjects( ObjectFilter objectFilter ) {
        return kieSession.getObjects( objectFilter );
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return kieSession.getFactHandles();
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles( ObjectFilter objectFilter ) {
        return kieSession.getFactHandles( objectFilter );
    }

    @Override
    public long getFactCount() {
        return kieSession.getFactCount();
    }

    @Override
    public int fireAllRules() {
        return kieSession.fireAllRules();
    }

    @Override
    public int fireAllRules( int i ) {
        return kieSession.fireAllRules( i );
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter ) {
        return kieSession.fireAllRules( agendaFilter );
    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter, int i ) {
        return kieSession.fireAllRules( agendaFilter, i );
    }

    @Override
    public void fireUntilHalt() {
        kieSession.fireUntilHalt();
    }

    @Override
    public void fireUntilHalt( AgendaFilter agendaFilter ) {
        kieSession.fireUntilHalt( agendaFilter );
    }
    
    @Override
    public void submit(AtomicAction action) {
        kieSession.submit( action );
    }

    @Override
    public <T> T getKieRuntime(Class<T> cls) {
        return kieSession.getKieRuntime( cls );
    }

    @Override
    public void execute( Object o ) {
        statelessKieSession.execute( o );
    }

    @Override
    public void execute( Iterable iterable ) {
        statelessKieSession.execute( iterable );
    }
}
