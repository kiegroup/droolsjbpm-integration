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
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

import java.util.Collection;
import java.util.Set;

public class KieImportBaseResolver extends AbstractKieObjectsResolver implements KieBase {

    private final String name;
    private KieBase kieBase;

    public KieImportBaseResolver( String name ) {
        super(null);
        this.name = name;
    }

    void setBase( KieBase kieBase ) {
        this.kieBase = kieBase;
    }

    @Override
    public Object init( BlueprintContextHelper context ) {
        return this;
    }

    @Override
    public Collection<KiePackage> getKiePackages() {
        return kieBase.getKiePackages();
    }

    @Override
    public KiePackage getKiePackage( String s ) {
        return kieBase.getKiePackage( s );
    }

    @Override
    public void removeKiePackage( String s ) {
        kieBase.removeKiePackage( s );
    }

    @Override
    public Rule getRule( String s, String s1 ) {
        return kieBase.getRule( s, s1 );
    }

    @Override
    public void removeRule( String s, String s1 ) {
        kieBase.removeRule( s, s1 );
    }

    @Override
    public Query getQuery( String s, String s1 ) {
        return kieBase.getQuery( s, s1 );
    }

    @Override
    public void removeQuery( String s, String s1 ) {
        kieBase.removeQuery( s, s1 );
    }

    @Override
    public void removeFunction( String s, String s1 ) {
        kieBase.removeFunction( s, s1 );
    }

    @Override
    public FactType getFactType( String s, String s1 ) {
        return kieBase.getFactType( s, s1 );
    }

    @Override
    public Process getProcess( String s ) {
        return kieBase.getProcess( s );
    }

    @Override
    public void removeProcess( String s ) {
        kieBase.removeProcess( s );
    }

    @Override
    public Collection<Process> getProcesses() {
        return kieBase.getProcesses();
    }

    @Override
    public KieSession newKieSession( KieSessionConfiguration kieSessionConfiguration, Environment environment ) {
        return kieBase.newKieSession( kieSessionConfiguration, environment );
    }

    @Override
    public KieSession newKieSession() {
        return kieBase.newKieSession();
    }

    @Override
    public Collection<? extends KieSession> getKieSessions() {
        return kieBase.getKieSessions();
    }

    @Override
    public StatelessKieSession newStatelessKieSession( KieSessionConfiguration kieSessionConfiguration ) {
        return kieBase.newStatelessKieSession( kieSessionConfiguration );
    }

    @Override
    public StatelessKieSession newStatelessKieSession() {
        return kieBase.newStatelessKieSession();
    }

    @Override
    public Set<String> getEntryPointIds() {
        return kieBase.getEntryPointIds();
    }

    @Override
    public void addEventListener( KieBaseEventListener kieBaseEventListener ) {
        kieBase.addEventListener( kieBaseEventListener );
    }

    @Override
    public void removeEventListener( KieBaseEventListener kieBaseEventListener ) {
        kieBase.removeEventListener( kieBaseEventListener );
    }

    @Override
    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        return kieBase.getKieBaseEventListeners();
    }

}
