/*
* Copyright 2012 JBoss Inc
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
package org.drools.container.spring.beans;

import static org.drools.container.spring.namespace.EnvironmentDefinitionParser.ELEMENT_CUSTOM_MARSHALLING_STRATEGY;
import static org.drools.container.spring.namespace.EnvironmentDefinitionParser.ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY;
import static org.drools.container.spring.namespace.EnvironmentDefinitionParser.ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY;
import static org.drools.container.spring.namespace.EnvironmentDefinitionParser.ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY;
import static org.drools.container.spring.namespace.EnvironmentDefinitionParser.ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.impl.EnvironmentFactory;
import org.drools.marshalling.ObjectMarshallingStrategy;
import org.drools.marshalling.ObjectMarshallingStrategyAcceptor;
import org.drools.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
import org.drools.marshalling.impl.IdentityPlaceholderResolverStrategy;
import org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.drools.runtime.Calendars;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.Globals;
import org.drools.type.DateFormats;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NamedBean;
import org.springframework.orm.jpa.JpaTransactionManager;

public class EnvironmentDefBeanFactory implements
        FactoryBean<Environment>, InitializingBean, BeanNameAware, NamedBean, DisposableBean {

    private String name;
    private String beanName;
    private Environment environment;
    private EntityManagerFactory entityManagerFactory;
    private JpaTransactionManager transactionManager;
    private Globals globals;
    private Calendars calendars;
    private DateFormats dateFormats;

    private ObjectMarshallingStrategyAcceptor identityPlaceholderResolverStrategyAcceptor;
    private ObjectMarshallingStrategyAcceptor serializablePlaceholderResolverStrategyAcceptor;

    private EntityManager appScopedEntityManager;
    private EntityManager cmdScopedEntityManager;
    private Environment jpaPlaceHolderResolverStrategyEnv;
    private List<String> objectMarshallersOrder = new ArrayList<String>();

    private List<ObjectMarshallingStrategy> customMarshallingStrategies = new ArrayList<ObjectMarshallingStrategy>();

    public List<String> getObjectMarshallersOrder() {
        return objectMarshallersOrder;
    }

    public void setObjectMarshallersOrder(List<String> objectMarshallersOrder) {
        this.objectMarshallersOrder = objectMarshallersOrder;
    }

    public List<ObjectMarshallingStrategy> getCustomMarshallingStrategies() {
        return customMarshallingStrategies;
    }

    public void setCustomMarshallingStrategies(List<ObjectMarshallingStrategy> customMarshallingStrategies) {
        this.customMarshallingStrategies = customMarshallingStrategies;
    }

    public Calendars getCalendars() {
        return calendars;
    }

    public void setCalendars(Calendars calendars) {
        this.calendars = calendars;
    }

    public DateFormats getDateFormats() {
        return dateFormats;
    }

    public void setDateFormats(DateFormats dateFormats) {
        this.dateFormats = dateFormats;
    }

    public Environment getJpaPlaceHolderResolverStrategyEnv() {
        return jpaPlaceHolderResolverStrategyEnv;
    }

    public void setJpaPlaceHolderResolverStrategyEnv(Environment jpaPlaceHolderResolverStrategyEnv) {
        this.jpaPlaceHolderResolverStrategyEnv = jpaPlaceHolderResolverStrategyEnv;
    }

    public EntityManager getAppScopedEntityManager() {
        return appScopedEntityManager;
    }

    public void setAppScopedEntityManager(EntityManager appScopedEntityManager) {
        this.appScopedEntityManager = appScopedEntityManager;
    }

    public EntityManager getCmdScopedEntityManager() {
        return cmdScopedEntityManager;
    }

    public void setCmdScopedEntityManager(EntityManager cmdScopedEntityManager) {
        this.cmdScopedEntityManager = cmdScopedEntityManager;
    }

    public ObjectMarshallingStrategyAcceptor getIdentityPlaceholderResolverStrategyAcceptor() {
        return identityPlaceholderResolverStrategyAcceptor;
    }

    public void setIdentityPlaceholderResolverStrategyAcceptor(ObjectMarshallingStrategyAcceptor identityPlaceholderResolverStrategyAcceptor) {
        this.identityPlaceholderResolverStrategyAcceptor = identityPlaceholderResolverStrategyAcceptor;
    }

    public ObjectMarshallingStrategyAcceptor getSerializablePlaceholderResolverStrategyAcceptor() {
        return serializablePlaceholderResolverStrategyAcceptor;
    }

    public void setSerializablePlaceholderResolverStrategyAcceptor(ObjectMarshallingStrategyAcceptor serializablePlaceholderResolverStrategyAcceptor) {
        this.serializablePlaceholderResolverStrategyAcceptor = serializablePlaceholderResolverStrategyAcceptor;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Globals getGlobals() {
        return globals;
    }

    public void setGlobals(Globals globals) {
        this.globals = globals;
    }

    public JpaTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void destroy() throws Exception {

    }

    public Environment getObject() throws Exception {
        return environment;
    }

    public Class<Environment> getObjectType() {
        return Environment.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public void afterPropertiesSet() throws Exception {
        environment = EnvironmentFactory.newEnvironment();
        if ( entityManagerFactory != null ) {
            environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
        }
        if ( transactionManager != null ){
            environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        }
        if ( globals != null ) {
            environment.set(EnvironmentName.GLOBALS, globals);
        }

        if ( dateFormats != null ) {
            environment.set(EnvironmentName.DATE_FORMATS, dateFormats);
        }

        if ( calendars != null ) {
            environment.set(EnvironmentName.CALENDARS, calendars);
        }

        if ( objectMarshallersOrder != null && objectMarshallersOrder.size() > 0 ) {
            List<ObjectMarshallingStrategy> strategies = getStrategies();
            environment.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES,strategies.toArray(new ObjectMarshallingStrategy[]{}));
        }

        if ( appScopedEntityManager != null ) {
            environment.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, appScopedEntityManager);
        }

        if ( cmdScopedEntityManager != null ) {
            environment.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, cmdScopedEntityManager);
        }
    }

    private List<ObjectMarshallingStrategy> getStrategies() {
        List<ObjectMarshallingStrategy> strategies = new ArrayList<ObjectMarshallingStrategy>();

        int customMarshaller = 0;
        for (String marshaller : objectMarshallersOrder ){
            if (ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(marshaller)){
                if ( serializablePlaceholderResolverStrategyAcceptor == null ) {
                    serializablePlaceholderResolverStrategyAcceptor = ClassObjectMarshallingStrategyAcceptor.DEFAULT;
                }
                strategies.add(new SerializablePlaceholderResolverStrategy(serializablePlaceholderResolverStrategyAcceptor));
            } else if (ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(marshaller)){
                if ( identityPlaceholderResolverStrategyAcceptor == null ) {
                    identityPlaceholderResolverStrategyAcceptor = ClassObjectMarshallingStrategyAcceptor.DEFAULT;
                }
                strategies.add(new IdentityPlaceholderResolverStrategy(identityPlaceholderResolverStrategyAcceptor));
            } else if (ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY.equalsIgnoreCase(marshaller)){
                strategies.add(new ProcessInstanceResolverStrategy());
            } else if (ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(marshaller)){
                if ( jpaPlaceHolderResolverStrategyEnv != null ) {
                    strategies.add(new JPAPlaceholderResolverStrategy(jpaPlaceHolderResolverStrategyEnv));
                } else {
                    strategies.add(new JPAPlaceholderResolverStrategy(environment));
                }
            } else if (ELEMENT_CUSTOM_MARSHALLING_STRATEGY.equalsIgnoreCase(marshaller)){
                strategies.add(customMarshallingStrategies.get(customMarshaller));
                customMarshaller++;
            }
        }
        return strategies;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeanName(String name) {
        beanName = name;
    }

    public String getBeanName() {
        return beanName;
    }
}
