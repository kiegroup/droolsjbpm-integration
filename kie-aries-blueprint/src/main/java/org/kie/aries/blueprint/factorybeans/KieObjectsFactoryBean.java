/*
 * Copyright 2013 JBoss Inc
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
package org.kie.aries.blueprint.factorybeans;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.core.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.persistence.jpa.KnowledgeStoreServiceImpl;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.kie.api.builder.ReleaseId;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.aries.blueprint.helpers.JPAPlaceholderResolverStrategyHelper;

import java.util.HashMap;
import java.util.List;

public class KieObjectsFactoryBean {

    public static Object fetchKBase(String id, ReleaseId releaseId, KBaseOptions kbaseOptions) {
        return new KieBaseResolver(releaseId, id);
    }

    public static Object fetchKContainer(ReleaseId releaseId){
        return new KieContainerResolver(releaseId);
    }

    public static Object createKieSessionRef(String id, ReleaseId releaseId, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands){
        return new KieSessionRefResolver( releaseId, id, listeners, loggers, commands );
    }

    public static Object createKieSession(String id, ReleaseId releaseId, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands, KSessionOptions kSessionOptions){
        return new KieSessionResolver( releaseId, listeners, loggers, commands, kSessionOptions );
    }

    public static KieStoreServices createKieStore() throws Exception {
        return new KnowledgeStoreServiceImpl();
    }

    public static ReleaseId createReleaseId(String id, String groupId, String artifactId, String version){
        return new ReleaseIdImpl(groupId, artifactId, version);
    }

    public static Environment createEnvironment(String id, HashMap<String, Object> parameters, List<Object> marshallingStrategies){
        Environment environment = EnvironmentFactory.newEnvironment();
        if ( parameters != null) {
            for (String key : parameters.keySet()){
                environment.set(key, parameters.get(key));
            }
        }
        for (int i=0; i<marshallingStrategies.size(); i++){
            Object object = marshallingStrategies.get(i);
            if ( object instanceof JPAPlaceholderResolverStrategyHelper) {
                JPAPlaceholderResolverStrategy jpaPlaceholderResolverStrategy;
                Environment refEnv = ((JPAPlaceholderResolverStrategyHelper)object).getEnvironment();
                if ( refEnv == null) {
                    jpaPlaceholderResolverStrategy = new JPAPlaceholderResolverStrategy(environment);
                } else {
                    jpaPlaceholderResolverStrategy = new JPAPlaceholderResolverStrategy(refEnv);
                }
                marshallingStrategies.set(i, jpaPlaceholderResolverStrategy);
                break;
            }
        }
        if ( marshallingStrategies != null){
            environment.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, marshallingStrategies.toArray(new ObjectMarshallingStrategy[]{}));
        }
        return environment;
    }

    public static ClassObjectMarshallingStrategyAcceptor createDefaultAcceptor(){
        return ClassObjectMarshallingStrategyAcceptor.DEFAULT;
    }
}
