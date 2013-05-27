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
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.aries.blueprint.helpers.JPAPlaceholderResolverStrategyHelper;
import org.osgi.service.blueprint.container.ComponentDefinitionException;

import java.util.HashMap;
import java.util.List;

public class KieObjectsFactoryBean {

    public static KieBase fetchKBase(String id, ReleaseId releaseId){
        System.out.println("fetchKBase :: "+id+", releaseId:: "+releaseId );
        KieObjectsResolver kieObjectsResolver = KieObjectsResolver.get();
        return kieObjectsResolver.resolveKBase(id, null);
    }

    public static Object createKieSessionRef(String id, ReleaseId releaseId, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands){

        System.out.println(id+"  "+commands);
        KieObjectsResolver kieObjectsResolver = KieObjectsResolver.get();
        Object obj = kieObjectsResolver.resolveKSession(id, releaseId);
        if ( obj != null) {
            KieSessionFactoryBeanHelper.addListeners((KieRuntimeEventManager) obj, listeners);
            KieSessionFactoryBeanHelper.attachLoggers((KieRuntimeEventManager) obj, loggers);
            if (obj instanceof KieSession){
                KieSessionFactoryBeanHelper.executeCommands((KieSession)obj, commands);
            }
            return obj;
        }
        throw new ComponentDefinitionException("No KSession found in kmodule.xml with id '"+id+"'.");
    }

    public static Object createKieSession(String id, ReleaseId releaseId, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands, String kbaseRef, String type){
        KieObjectsResolver kieObjectsResolver = KieObjectsResolver.get();
        Object obj ;
        System.out.println(id+"  "+commands);
        if ("stateless".equalsIgnoreCase(type)) {
            obj = kieObjectsResolver.newStatelessSession(kbaseRef, releaseId, null);
        } else {
            obj = kieObjectsResolver.newStatefulSession(kbaseRef, releaseId, null);
            KieSessionFactoryBeanHelper.executeCommands((KieSession)obj, commands);
        }

        KieSessionFactoryBeanHelper.addListeners((KieRuntimeEventManager) obj, listeners);
        KieSessionFactoryBeanHelper.attachLoggers((KieRuntimeEventManager) obj, loggers);

        return obj;
    }

    public static KieStoreServices createKieStore() throws Exception {

        KieObjectsResolver objectsResolver = KieObjectsResolver.get();
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
