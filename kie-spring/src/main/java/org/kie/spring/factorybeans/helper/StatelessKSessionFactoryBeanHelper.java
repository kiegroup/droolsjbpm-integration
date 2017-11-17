/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.factorybeans.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.spring.factorybeans.KSessionFactoryBean;

public class StatelessKSessionFactoryBeanHelper extends KSessionFactoryBeanHelper {

    protected StatelessKieSession kieSession;

    private List<Command<?>> commands;

    public StatelessKSessionFactoryBeanHelper(KSessionFactoryBean factoryBean, StatelessKieSession kieSession) {
        super(factoryBean);
        this.kieSession = kieSession;
    }

    @Override
    public void internalAfterPropertiesSet() throws Exception {
    }

    @Override
    public void executeBatch() {
        if (factoryBean.getBatch() != null && !factoryBean.getBatch().isEmpty()) {
            commands = factoryBean.getBatch();
        }
    }

    @Override
    public Object internalGetObject() {
        return commands == null ? kieSession : new DelegateStatelessKieSession(kieSession, commands);
    }

    @Override
    public Object internalNewObject() {
        if (kieBase != null) {
            return kieBase.newStatelessKieSession(factoryBean.getConf());
        }
        return null;
    }

    public static class DelegateStatelessKieSession implements StatelessKieSession {

        private final StatelessKieSession delegate;
        private final List<Command<?>> commands;

        public DelegateStatelessKieSession( StatelessKieSession delegate, List<Command<?>> commands ) {
            this.delegate = delegate;
            this.commands = commands;
        }

        @Override
        public Globals getGlobals() {
            return delegate.getGlobals();
        }

        @Override
        public void setGlobal( String s, Object o ) {
            delegate.setGlobal( s, o );
        }

        @Override
        public void registerChannel( String s, Channel channel ) {
            delegate.registerChannel( s, channel );
        }

        @Override
        public void unregisterChannel( String s ) {
            delegate.unregisterChannel( s );
        }

        @Override
        public Map<String, Channel> getChannels() {
            return delegate.getChannels();
        }

        @Override
        public KieBase getKieBase() {
            return delegate.getKieBase();
        }

        @Override
        public KieRuntimeLogger getLogger() {
            return delegate.getLogger();
        }

        @Override
        public void addEventListener( ProcessEventListener processEventListener ) {
            delegate.addEventListener( processEventListener );
        }

        @Override
        public void removeEventListener( ProcessEventListener processEventListener ) {
            delegate.removeEventListener( processEventListener );
        }

        @Override
        public Collection<ProcessEventListener> getProcessEventListeners() {
            return delegate.getProcessEventListeners();
        }

        @Override
        public void addEventListener( RuleRuntimeEventListener ruleRuntimeEventListener ) {
            delegate.addEventListener( ruleRuntimeEventListener );
        }

        @Override
        public void removeEventListener( RuleRuntimeEventListener ruleRuntimeEventListener ) {
            delegate.removeEventListener( ruleRuntimeEventListener );
        }

        @Override
        public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
            return delegate.getRuleRuntimeEventListeners();
        }

        @Override
        public void addEventListener( AgendaEventListener agendaEventListener ) {
            delegate.addEventListener( agendaEventListener );
        }

        @Override
        public void removeEventListener( AgendaEventListener agendaEventListener ) {
            delegate.removeEventListener( agendaEventListener );
        }

        @Override
        public Collection<AgendaEventListener> getAgendaEventListeners() {
            return delegate.getAgendaEventListeners();
        }

        @Override
        public <T> T execute( Command<T> command ) {
            KieCommands kieCommands = KieServices.Factory.get().getCommands();
            List<Command<?>> cmds = new ArrayList<Command<?>>();
            cmds.addAll( commands );
            cmds.add( command );
            return (T) delegate.execute( CommandFactory.newBatchExecution( cmds ) );
        }

        @Override
        public void execute( Object object ) {
            KieCommands kieCommands = KieServices.Factory.get().getCommands();
            List<Command<?>> cmds = new ArrayList<Command<?>>();
            cmds.addAll( commands );
            cmds.add( kieCommands.newInsert( object ) );
            cmds.add( kieCommands.newFireAllRules() );
            delegate.execute( CommandFactory.newBatchExecution( cmds ) );
        }

        @Override
        public void execute( Iterable objects ) {
            KieCommands kieCommands = KieServices.Factory.get().getCommands();
            List<Command<?>> cmds = new ArrayList<Command<?>>();
            cmds.addAll( commands );
            for ( Object object : objects ) {
                cmds.add( kieCommands.newInsert( object ) );
            }
            cmds.add( kieCommands.newFireAllRules() );
            delegate.execute( CommandFactory.newBatchExecution( cmds ) );
        }
    }
}
