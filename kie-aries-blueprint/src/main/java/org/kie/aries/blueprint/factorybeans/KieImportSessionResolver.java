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

    public void setSession( Object session ) {
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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getSessionClock -> TODO" );

    }

    @Override
    public void setGlobal( String s, Object o ) {
        kieSession.setGlobal( s, o );
    }

    @Override
    public Object getGlobal( String s ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getGlobal -> TODO" );

    }

    @Override
    public Globals getGlobals() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getGlobals -> TODO" );

    }

    @Override
    public Calendars getCalendars() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getCalendars -> TODO" );

    }

    @Override
    public Environment getEnvironment() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getEnvironment -> TODO" );

    }

    @Override
    public KieBase getKieBase() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getKieBase -> TODO" );

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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getChannels -> TODO" );

    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getSessionConfiguration -> TODO" );

    }

    @Override
    public KieRuntimeLogger getLogger() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getLogger -> TODO" );

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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getProcessEventListeners -> TODO" );

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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getRuleRuntimeEventListeners -> TODO" );

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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getAgendaEventListeners -> TODO" );

    }

    @Override
    public <T> T execute( Command<T> command ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.execute -> TODO" );

    }

    @Override
    public ProcessInstance startProcess( String s ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.startProcess -> TODO" );

    }

    @Override
    public ProcessInstance startProcess( String s, Map<String, Object> map ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.startProcess -> TODO" );

    }

    @Override
    public ProcessInstance createProcessInstance( String s, Map<String, Object> map ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.createProcessInstance -> TODO" );

    }

    @Override
    public ProcessInstance startProcessInstance( long l ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.startProcessInstance -> TODO" );

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
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getProcessInstances -> TODO" );

    }

    @Override
    public ProcessInstance getProcessInstance( long l ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getProcessInstance -> TODO" );

    }

    @Override
    public ProcessInstance getProcessInstance( long l, boolean b ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getProcessInstance -> TODO" );

    }

    @Override
    public void abortProcessInstance( long l ) {
        kieSession.abortProcessInstance( l );
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getWorkItemManager -> TODO" );

    }

    @Override
    public void halt() {
        kieSession.halt();
    }

    @Override
    public Agenda getAgenda() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getAgenda -> TODO" );

    }

    @Override
    public EntryPoint getEntryPoint( String s ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getEntryPoint -> TODO" );

    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getEntryPoints -> TODO" );

    }

    @Override
    public QueryResults getQueryResults( String s, Object... objects ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getQueryResults -> TODO" );

    }

    @Override
    public LiveQuery openLiveQuery( String s, Object[] objects, ViewChangedEventListener viewChangedEventListener ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.openLiveQuery -> TODO" );

    }

    @Override
    public String getEntryPointId() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getEntryPointId -> TODO" );

    }

    @Override
    public FactHandle insert( Object o ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.insert -> TODO" );

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
    public FactHandle getFactHandle( Object o ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getFactHandle -> TODO" );

    }

    @Override
    public Object getObject( FactHandle factHandle ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getObject -> TODO" );

    }

    @Override
    public Collection<? extends Object> getObjects() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getObjects -> TODO" );

    }

    @Override
    public Collection<? extends Object> getObjects( ObjectFilter objectFilter ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getObjects -> TODO" );

    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getFactHandles -> TODO" );

    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles( ObjectFilter objectFilter ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getFactHandles -> TODO" );

    }

    @Override
    public long getFactCount() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.getFactCount -> TODO" );

    }

    @Override
    public int fireAllRules() {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.fireAllRules -> TODO" );

    }

    @Override
    public int fireAllRules( int i ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.fireAllRules -> TODO" );

    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.fireAllRules -> TODO" );

    }

    @Override
    public int fireAllRules( AgendaFilter agendaFilter, int i ) {
        throw new UnsupportedOperationException( "org.kie.aries.blueprint.factorybeans.KieImportSessionResolver.fireAllRules -> TODO" );

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
    public void execute( Object o ) {
        statelessKieSession.execute( o );
    }

    @Override
    public void execute( Iterable iterable ) {
        statelessKieSession.execute( iterable );
    }
}
