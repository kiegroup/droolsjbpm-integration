/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.internal.persistence.infinispan;

import org.kie.api.KieBase;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class InfinispanKnowledgeService {
    private static KieStoreServices provider;

    public static StatefulKnowledgeSession newStatefulKnowledgeSession(KieBase kbase,
                                                                       KieSessionConfiguration configuration,
                                                                       Environment environment) {
        return (StatefulKnowledgeSession)getInfinispanKnowledgeServiceProvider().newKieSession(kbase,
                configuration,
                environment);
    }

    public static StatefulKnowledgeSession loadStatefulKnowledgeSession(long id,
                                                                        KieBase kbase,
                                                                        KieSessionConfiguration configuration,
                                                                        Environment environment) {
        return (StatefulKnowledgeSession)getInfinispanKnowledgeServiceProvider().loadKieSession(id,
                kbase,
                configuration,
                environment);
    }

    private static synchronized void setInfinispanKnowledgeServiceProvider(KieStoreServices provider) {
        InfinispanKnowledgeService.provider = provider;
    }

    private static synchronized KieStoreServices getInfinispanKnowledgeServiceProvider() {
        if ( provider == null ) {
            loadProvider();
        }
        return provider;
    }

    @SuppressWarnings("unchecked")
    private static void loadProvider() {
        try {
            // we didn't find anything in properties so lets try and us reflection
            Class<KieStoreServices> cls = (Class<KieStoreServices>) Class.forName( "org.drools.persistence.infinispan.KnowledgeStoreServiceImpl" );
            setInfinispanKnowledgeServiceProvider( cls.newInstance() );
        } catch ( Exception e ) {
            throw new RuntimeException( "Provider org.drools.persistence.infinispan.KnowledgeStoreServiceImpl could not be set.",
                                        e );
        }
    }


}
