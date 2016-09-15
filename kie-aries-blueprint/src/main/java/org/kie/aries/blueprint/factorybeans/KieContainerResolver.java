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
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.aries.blueprint.namespace.BlueprintContextHelper;

import java.util.Collection;

public class KieContainerResolver extends AbstractKieObjectsResolver implements KieContainer {

    private KieContainer kieContainer;

    public KieContainerResolver( ReleaseId releaseId ) {
        super( releaseId );
    }

    @Override
    public Object init(BlueprintContextHelper context ) {
        return getKieContainer();
    }

    private synchronized KieContainer getKieContainer() {
        if (kieContainer == null) {
            KieServices ks = KieServices.Factory.get();
            if ( releaseId == null ) {
                kieContainer = ks.getKieClasspathContainer();
            } else {
                kieContainer = resolveKContainer( releaseId );
            }
        }
        return kieContainer;
    }

    @Override
    public void dispose() {
        getKieContainer().dispose();
    }

    public ReleaseId getReleaseId() {
        return getKieContainer().getReleaseId();
    }

    public Results verify() {
        return getKieContainer().verify();
    }

    @Override
    public Results verify( String... kBaseNames ) {
        return getKieContainer().verify(kBaseNames);
    }

    @Override
    public Results updateToVersion( ReleaseId version ) {
        return getKieContainer().updateToVersion( version );
    }

    @Override
    public Collection<String> getKieBaseNames() {
        return getKieContainer().getKieBaseNames();
    }

    @Override
    public Collection<String> getKieSessionNamesInKieBase( String kBaseName ) {
        return getKieContainer().getKieSessionNamesInKieBase( kBaseName );
    }

    @Override
    public KieBase getKieBase() {
        return getKieContainer().getKieBase();
    }

    @Override
    public KieBase getKieBase( String kBaseName ) {
        return getKieContainer().getKieBase( kBaseName );
    }

    @Override
    public KieBase newKieBase( KieBaseConfiguration conf ) {
        return getKieContainer().newKieBase( conf ) ;
    }

    @Override
    public KieBase newKieBase( String kBaseName, KieBaseConfiguration conf ) {
        return getKieContainer().newKieBase( kBaseName, conf );
    }

    @Override
    public KieSession newKieSession() {
        return getKieContainer().newKieSession();
    }

    @Override
    public KieSession newKieSession( KieSessionConfiguration conf ) {
        return getKieContainer().newKieSession( conf );
    }

    @Override
    public KieSession newKieSession( Environment environment ) {
        return getKieContainer().newKieSession( environment );
    }

    @Override
    public KieSession newKieSession( Environment environment, KieSessionConfiguration conf ) {
        return getKieContainer().newKieSession( environment, conf );
    }

    @Override
    public KieSession newKieSession( String kSessionName ) {
        return getKieContainer().newKieSession( kSessionName );
    }

    @Override
    public KieSession newKieSession( String kSessionName, Environment environment ) {
        return getKieContainer().newKieSession( kSessionName, environment );
    }

    @Override
    public KieSession newKieSession( String kSessionName, KieSessionConfiguration conf ) {
        return getKieContainer().newKieSession( kSessionName, conf );
    }

    @Override
    public KieSession newKieSession( String kSessionName, Environment environment, KieSessionConfiguration conf ) {
        return getKieContainer().newKieSession(kSessionName, environment, conf);
    }

    @Override
    public StatelessKieSession newStatelessKieSession() {
        return getKieContainer().newStatelessKieSession();
    }

    @Override
    public StatelessKieSession newStatelessKieSession( KieSessionConfiguration conf ) {
        return getKieContainer().newStatelessKieSession( conf );

    }

    @Override
    public StatelessKieSession newStatelessKieSession( String kSessionName ) {
        return getKieContainer().newStatelessKieSession( kSessionName );
    }

    @Override
    public StatelessKieSession newStatelessKieSession( String kSessionName, KieSessionConfiguration conf ) {
        return getKieContainer().newStatelessKieSession(kSessionName, conf);
    }

    @Override
    public ClassLoader getClassLoader() {
        return getKieContainer().getClassLoader();
    }

    @Override
    public KieSessionConfiguration getKieSessionConfiguration() {
        return getKieContainer().getKieSessionConfiguration();
    }

    @Override
    public KieSessionConfiguration getKieSessionConfiguration( String s ) {
        return getKieContainer().getKieSessionConfiguration( s );
    }

    @Override
    public KieBaseModel getKieBaseModel( String s ) {
        return getKieContainer().getKieBaseModel( s );
    }

    @Override
    public KieSessionModel getKieSessionModel( String s ) {
        return getKieContainer().getKieSessionModel( s );
    }
}