package org.drools.container.spring.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.command.SingleSessionCommandService;
import org.drools.container.spring.beans.persistence.SpringSingleSessionCommandService;
import org.drools.persistence.processinstance.JPAProcessInstanceManagerFactory;
import org.drools.persistence.processinstance.JPASignalManagerFactory;
import org.drools.persistence.processinstance.JPAWorkItemManagerFactory;
import org.drools.persistence.processinstance.VariablePersistenceStrategyFactory;
import org.drools.persistence.processinstance.persisters.VariablePersister;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

public class JPASingleSessionCommandServiceFactory implements FactoryBean, InitializingBean {

	private Environment environment;
	private KnowledgeBase knowledgeBase;
	private EntityManagerFactory entityManagerFactory;
	private PlatformTransactionManager transactionManager;
	private Map<String, Class<? extends VariablePersister>> variablePersisters;

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Map<String, Class<? extends VariablePersister>> getVariablePersisters() {
		if (variablePersisters == null) {
			variablePersisters = new HashMap<String, Class<? extends VariablePersister>>();
		}
		return variablePersisters;
	}

	public void setVariablePersisters(Map<String, Class<? extends VariablePersister>> variablePersisters) {
		this.variablePersisters = variablePersisters;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {

		return new JPASessionService(getEnvironment(), getKnowledgeBase(), getEntityManagerFactory(),
				getTransactionManager());
	}

	public Class<JPASingleSessionCommandService> getObjectType() {
		return JPASingleSessionCommandService.class;
	}

	public boolean isSingleton() {
		return false;
	}

	public void afterPropertiesSet() throws Exception {
		if (getEnvironment() == null) {
			setEnvironment(KnowledgeBaseFactory.newEnvironment());
		}
		if (getTransactionManager() == null) {
			throw new IllegalArgumentException("transactionManager property is mandatory");
		}
		if (getEntityManagerFactory() == null) {
			throw new IllegalArgumentException("entityManagerFactory property is mandatory");
		}
		if (getKnowledgeBase() == null) {
			throw new IllegalArgumentException("knowledgeBase property is mandatory");
		}
		if (!getVariablePersisters().isEmpty()) {
			for (Map.Entry<String, Class<? extends VariablePersister>> entry : getVariablePersisters().entrySet()) {
				VariablePersistenceStrategyFactory.getVariablePersistenceStrategy().setPersister(entry.getKey(),
						entry.getValue().getName());
			}
		}
	}

	private static class JPASessionService implements JPASingleSessionCommandService {

		public JPASessionService(Environment environment, KnowledgeBase knowledgeBase,
				EntityManagerFactory entityManagerFactory, PlatformTransactionManager transactionManager) {
			super();
			this.environment = environment;
			this.knowledgeBase = knowledgeBase;
			this.entityManagerFactory = entityManagerFactory;
			this.transactionManager = transactionManager;
		}

		private Environment environment;
		private KnowledgeBase knowledgeBase;
		private EntityManagerFactory entityManagerFactory;
		private PlatformTransactionManager transactionManager;

		public SingleSessionCommandService createNew() {
			return new SpringSingleSessionCommandService(knowledgeBase, getSessionConfiguration(), getEnvironment());
		}

		public SingleSessionCommandService load(int sessionId) {
			return new SpringSingleSessionCommandService(sessionId, knowledgeBase, getSessionConfiguration(),
					getEnvironment());
		}

		public Environment getEnvironment() {
			environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
			environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
			return environment;
		}

		private SessionConfiguration getSessionConfiguration() {
			Properties properties = new Properties();
			properties.setProperty("drools.commandService", SpringSingleSessionCommandService.class.getName());
			properties.setProperty("drools.processInstanceManagerFactory", JPAProcessInstanceManagerFactory.class
					.getName());
			properties.setProperty("drools.workItemManagerFactory", JPAWorkItemManagerFactory.class.getName());
			properties.setProperty("drools.processSignalManagerFactory", JPASignalManagerFactory.class.getName());
			return new SessionConfiguration(properties);
		}
	}
}
