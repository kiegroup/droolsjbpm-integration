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

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

public abstract class AbstractKieObjectsResolver implements Initializable {
    private BundleContext bundleContext;

    protected final ReleaseId releaseId;

    public AbstractKieObjectsResolver( ReleaseId releaseId ) {
        this.releaseId = releaseId;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public KieBase resolveKBase( String id, ReleaseId releaseId ) {
        KieContainer kieContainer = resolveKContainer( releaseId );
        KieBase kieBase = kieContainer.getKieBase(id);
        if (kieBase == null) {
            kieBase = kieContainer.newKieBase(id, null);
        }
        return kieBase;
    }

    public Object resolveKSession( String id, ReleaseId releaseId ) {
        return resolveKSession( id, resolveKContainer(releaseId) );
    }

    protected Object resolveKSession( String id, KieContainer kieContainer ) {
        KieContainerImpl kcontainer = (KieContainerImpl) kieContainer;
        KieProject kProject = kcontainer.getKieProject();
        KieSessionModel kieSessionModel = kProject.getKieSessionModel( id );
        if ( kieSessionModel == null) {
            return null;
        }
        if (kieSessionModel.getType() == KieSessionModel.KieSessionType.STATEFUL) {
            return kcontainer.getKieSession( id );
        } else if (kieSessionModel.getType() == KieSessionModel.KieSessionType.STATELESS) {
            return kcontainer.getStatelessKieSession( id );
        }
        return null;
    }

    protected KieContainer resolveKContainer( ReleaseId releaseId ) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Cannot resolve a KieContainer using a null ReleaseId");
        }
        if (bundleContext == null) {
            throw new IllegalStateException("Blueprint 'bundleContext' was not set! It is needed in order to create new KieContainer.");
        }
        KieContainer kieContainer = KieServices.Factory.get().newKieContainer( releaseId, bundleContext.getBundle().adapt( BundleWiring.class ).getClassLoader() );
        if ( kieContainer == null) {
            throw new IllegalArgumentException("Could not find a KModule with ReleaseId ("+releaseId+")");
        }
        return kieContainer;
    }

    public KieSession newStatefulSession( String kbaseName, ReleaseId releaseId, KieSessionConfiguration conf ) {
        KieBase kieBase = resolveKBase( kbaseName, releaseId );
        if (kieBase == null) {
            KieContainer kieContainer = resolveKContainer( releaseId );
            if (conf == null) {
                return kieContainer.newKieSession(kbaseName);
            } else {
                return kieContainer.newKieSession(conf);
            }
        } else {
            if (conf == null) {
                return kieBase.newKieSession();
            } else {
                return kieBase.newKieSession(conf, null);
            }
        }
    }

    public StatelessKieSession newStatelessSession( String kbaseName, ReleaseId releaseId, KieSessionConfiguration conf ) {
        KieBase kieBase = resolveKBase(kbaseName, releaseId);
        if (kieBase == null) {
            KieContainer kieContainer = resolveKContainer(releaseId);
            if (conf == null) {
                return kieContainer.newStatelessKieSession();
            } else {
                return kieContainer.newStatelessKieSession(conf);
            }
        } else {
            if (conf == null) {
                return kieBase.newStatelessKieSession();
            } else {
                return kieBase.newStatelessKieSession(conf);
            }
        }
    }
}
