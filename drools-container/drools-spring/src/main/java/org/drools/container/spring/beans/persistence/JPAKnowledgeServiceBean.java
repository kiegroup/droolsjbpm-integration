package org.drools.container.spring.beans.persistence;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.drools.KnowledgeBase;
import org.drools.RuntimeDroolsException;
import org.drools.base.MapGlobalResolver;
import org.drools.persistence.jpa.JPAKnowledgeServiceProvider;
import org.drools.persistence.jpa.impl.JPAKnowledgeServiceProviderImpl;
import org.drools.persistence.processinstance.VariablePersistenceStrategyFactory;
import org.drools.persistence.processinstance.persisters.VariablePersister;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.vsm.ServiceManager;
import org.drools.vsm.local.ServiceManagerLocalClient;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class JPAKnowledgeServiceBean extends JpaDaoSupport  {
	private ServiceManager serviceManager;
	private KnowledgeBase kbase;
	private JPAKnowledgeServiceProvider jpaKnowledgeServiceProvider;
	private Environment environment;
	private AbstractPlatformTransactionManager transactionManager;
	private Map<Class<?>, Class<? extends VariablePersister>> variablePersisters = Collections.emptyMap();

	public StatefulKnowledgeSession newStatefulKnowledgeSession() {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		return (StatefulKnowledgeSession) txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				return getJpaTemplate().execute(new JpaCallback() {
					public StatefulKnowledgeSession doInJpa(EntityManager em) throws PersistenceException {
						return jpaKnowledgeServiceProvider.newStatefulKnowledgeSession(kbase, null, environment);
					}
				});
			}
		});
	}
	
	public StatefulKnowledgeSession loadStatefulKnowledgeSession(final int sessionId) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		return (StatefulKnowledgeSession) txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				return getJpaTemplate().execute(new JpaCallback() {
					public StatefulKnowledgeSession doInJpa(EntityManager em) throws PersistenceException {
						return jpaKnowledgeServiceProvider.loadStatefulKnowledgeSession(sessionId, kbase, null, environment);
					}
				});
			}
		});
	}
	
	@Override
	protected void initDao() {
		if (kbase == null) {
			throw new IllegalArgumentException("property kbase is mandatory");
		}
		if (serviceManager == null) {
			serviceManager = new ServiceManagerLocalClient();
		}
		if (environment == null) {
			environment = serviceManager.getKnowledgeBaseFactory().newEnvironment();
		}

		if (environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY) != null) {
			logger.debug("overwriting environment key: " + EnvironmentName.ENTITY_MANAGER_FACTORY);
		}
		environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, getJpaTemplate().getEntityManagerFactory());
		
		if (environment.get(EnvironmentName.TRANSACTION_MANAGER) != null) {
			logger.debug("overwriting environment key: " + EnvironmentName.TRANSACTION_MANAGER);
		}
		environment.set(EnvironmentName.TRANSACTION_MANAGER, getTransactionManager());
		environment.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
		jpaKnowledgeServiceProvider = serviceManager.JPAKnowledgeService();
		if (jpaKnowledgeServiceProvider instanceof JPAKnowledgeServiceProviderImpl) {
			((JPAKnowledgeServiceProviderImpl) jpaKnowledgeServiceProvider).setCommandServiceClass(SpringSingleSessionCommandService.class);
		} else {
			throw new RuntimeDroolsException("JPAKnowledgeService is not instance of: " + JPAKnowledgeServiceProviderImpl.class.getName());
		}
		
		if (variablePersisters != null && !variablePersisters.isEmpty()) {
			for (Map.Entry<Class<?>, Class<? extends VariablePersister>> entry : variablePersisters.entrySet()) {
				 VariablePersistenceStrategyFactory.getVariablePersistenceStrategy().setPersister(entry.getKey().getName(), entry.getValue().getName());
			}
		}
//		serviceManager.JPAKnowledgeService().loadStatefulKnowledgeSession(0, 
//					serviceManager.getKnowledgeBaseFactory().newKnowledgeBase(), 
//					null, 
//					environment);
	}

	public KnowledgeBase getKbase() {
		return kbase;
	}

	public void setKbase(KnowledgeBase kbase) {
		this.kbase = kbase;
	}
	
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public AbstractPlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setVariablePersisters(Map<Class<?>, Class<? extends VariablePersister>> variablePersisters) {
		this.variablePersisters = variablePersisters;
	}

	public Map<Class<?>, Class<? extends VariablePersister>> getVariablePersisters() {
		return variablePersisters;
	}
}
