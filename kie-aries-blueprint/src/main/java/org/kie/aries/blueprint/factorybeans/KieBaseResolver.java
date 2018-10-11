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

import java.util.Collection;
import java.util.Set;

import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.KieSessionsPool;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

public class KieBaseResolver extends AbstractKieObjectsResolver implements KieBase {

    private final String id;

    private KieBase kieBase;

    public KieBaseResolver( ReleaseId releaseId, String id ) {
        super( releaseId );
        this.id = id;
    }

    @Override
    public Object init(BlueprintContextHelper context ) {
        return getKieBase();
    }

    private synchronized KieBase getKieBase() {
        if (kieBase == null) {
            kieBase = resolveKBase( id, releaseId );
        }
        return kieBase;
    }

    @Override
    public Collection<KiePackage> getKiePackages() {
        return getKieBase().getKiePackages();
    }

    @Override
    public KiePackage getKiePackage( String packageName ) {
        return getKieBase().getKiePackage( packageName );
    }

    @Override
    public void removeKiePackage( String packageName ) {
        getKieBase().removeKiePackage( packageName );
    }

    @Override
    public Rule getRule( String packageName, String ruleName ) {
        return getKieBase().getRule( packageName, ruleName );
    }

    @Override
    public void removeRule( String packageName, String ruleName ) {
        getKieBase().removeRule( packageName, ruleName );
    }

    @Override
    public Query getQuery( String packageName, String queryName ) {
        return getKieBase().getQuery( packageName, queryName );
    }

    @Override
    public void removeQuery( String packageName, String queryName ) {
        getKieBase().removeQuery( packageName, queryName );
    }

    @Override
    public void removeFunction( String packageName, String functionName ) {
        getKieBase().removeFunction( packageName, functionName );
    }

    @Override
    public FactType getFactType( String packageName, String typeName ) {
        return getKieBase().getFactType( packageName, typeName );
    }

    @Override
    public Process getProcess( String processId ) {
        return getKieBase().getProcess( processId );
    }

    @Override
    public void removeProcess( String processId ) {
        getKieBase().removeProcess( processId );
    }

    @Override
    public Collection<Process> getProcesses() {
        return getKieBase().getProcesses();
    }

    @Override
    public KieSession newKieSession( KieSessionConfiguration conf, Environment environment ) {
        return getKieBase().newKieSession(conf, environment);
    }

    @Override
    public KieSession newKieSession() {
        return getKieBase().newKieSession();
    }

    @Override
    public KieSessionsPool newKieSessionsPool( int initialSize ) {
        return getKieBase().newKieSessionsPool( initialSize );
    }

    @Override
    public Collection<? extends KieSession> getKieSessions() {
        return getKieBase().getKieSessions();
    }

    @Override
    public StatelessKieSession newStatelessKieSession( KieSessionConfiguration conf ) {
        return getKieBase().newStatelessKieSession(conf);
    }

    @Override
    public StatelessKieSession newStatelessKieSession() {
        return getKieBase().newStatelessKieSession();
    }

    @Override
    public Set<String> getEntryPointIds() {
        return getKieBase().getEntryPointIds();
    }

    @Override
    public void addEventListener( KieBaseEventListener listener ) {
        getKieBase().addEventListener( listener );
    }

    @Override
    public void removeEventListener( KieBaseEventListener listener ) {
        getKieBase().removeEventListener( listener );
    }

    @Override
    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return getKieBase().getKieBaseEventListeners();
    }
}