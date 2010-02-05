package org.drools.container.spring.beans.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.drools.KnowledgeBase;
import org.drools.RuleBase;
import org.drools.SessionConfiguration;
import org.drools.command.CommandService;
import org.drools.command.Context;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.persistence.processinstance.JPAProcessInstanceManager;
import org.drools.persistence.processinstance.JPASignalManager;
import org.drools.persistence.processinstance.JPAWorkItemManager;
import org.drools.persistence.session.JPASessionMarshallingHelper;
import org.drools.persistence.session.SessionInfo;
import org.drools.reteoo.ReteooStatefulSession;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.JpaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class SpringSingleSessionCommandService implements CommandService {
	private JpaTemplate jpaTemplate;
	private SessionInfo sessionInfo;
	private JPASessionMarshallingHelper marshallingHelper;
	private StatefulKnowledgeSession ksession;
	private Environment env;
	private KnowledgeCommandContext kContext;
	private PlatformTransactionManager transactionManager;

	public void checkEnvironment(Environment env) {
		if (env.get(EnvironmentName.ENTITY_MANAGER_FACTORY) == null) {
			throw new IllegalArgumentException("Environment must have an EntityManagerFactory");
		}
		this.env = env;
		transactionManager = (PlatformTransactionManager) env.get(EnvironmentName.TRANSACTION_MANAGER);
		if (transactionManager == null) {
			throw new IllegalArgumentException("Environment must have an TransactionManager");
		}
	}

	public SpringSingleSessionCommandService(RuleBase ruleBase,
			SessionConfiguration conf,
			Environment env) {
		this(new KnowledgeBaseImpl(ruleBase), conf, env);
	}

	public SpringSingleSessionCommandService(int sessionId,
			RuleBase ruleBase,
			SessionConfiguration conf,
			Environment env) {
		this(sessionId, new KnowledgeBaseImpl(ruleBase), conf, env);
	}

	public SpringSingleSessionCommandService(KnowledgeBase kbase,
			KnowledgeSessionConfiguration conf,
			Environment env) {
		if (conf == null) {
			conf = new SessionConfiguration();
		}
		checkEnvironment(env);
		this.sessionInfo = new SessionInfo();

		ReteooStatefulSession session = (ReteooStatefulSession)
				((KnowledgeBaseImpl) kbase).ruleBase.newStatefulSession((SessionConfiguration) conf, this.env);
		this.ksession = new StatefulKnowledgeSessionImpl(session, kbase);

		this.kContext = new KnowledgeCommandContext(new ContextImpl("ksession", null), null, null, this.ksession, null);
		((JPASignalManager) ((StatefulKnowledgeSessionImpl) ksession).session.getSignalManager())
				.setCommandService(this);

		this.marshallingHelper = new JPASessionMarshallingHelper(this.ksession, conf);

		this.sessionInfo.setJPASessionMashallingHelper(this.marshallingHelper);

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		jpaTemplate = new JpaTemplate((EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY));

		txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				Object result = jpaTemplate.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) throws PersistenceException {
						SpringSingleSessionCommandService.this.env.set(EnvironmentName.ENTITY_MANAGER, em);
						em.persist(sessionInfo);
						// update the session id to be the same as the session info id
						((StatefulKnowledgeSessionImpl) ksession).session.setId(sessionInfo.getId());
						em.flush();
						SpringSingleSessionCommandService.this.env.set(EnvironmentName.ENTITY_MANAGER, null);
						return null;
					}
				});
				return result;
			}
		});
	}

	public SpringSingleSessionCommandService(final int sessionId,
			final KnowledgeBase kbase,
			KnowledgeSessionConfiguration conf,
			Environment env) {
		final KnowledgeSessionConfiguration localConf = conf == null ? new SessionConfiguration() : conf;
		this.env = env;
		checkEnvironment(env);
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		jpaTemplate = new JpaTemplate((EntityManagerFactory) env.get(EnvironmentName.ENTITY_MANAGER_FACTORY));
		txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				return jpaTemplate.execute(new JpaCallback() {

					public Object doInJpa(EntityManager em) throws PersistenceException {
						SpringSingleSessionCommandService.this.env.set(EnvironmentName.ENTITY_MANAGER, em);
						sessionInfo = em.find(SessionInfo.class, sessionId);

						if (sessionInfo == null) {
							SpringSingleSessionCommandService.this.env.set(EnvironmentName.ENTITY_MANAGER, null);
							throw new RuntimeException("Could not find session data for id " + sessionId);
						}

						marshallingHelper = new JPASessionMarshallingHelper(sessionInfo,
								kbase,
								localConf,
								SpringSingleSessionCommandService.this.env);

						sessionInfo.setJPASessionMashallingHelper(marshallingHelper);
						ksession = marshallingHelper.getObject();
						kContext = new KnowledgeCommandContext(new ContextImpl("ksession", null), null, null, ksession,
								null);
						((JPASignalManager) ((StatefulKnowledgeSessionImpl) ksession).session.getSignalManager())
								.setCommandService(SpringSingleSessionCommandService.this);

						// update the session id to be the same as the session info id
						((StatefulKnowledgeSessionImpl) ksession).session.setId(sessionInfo.getId());
						em.flush();
						SpringSingleSessionCommandService.this.env.set(EnvironmentName.ENTITY_MANAGER, null);
						return sessionInfo;
					}
				});
			}
		});
	}

	public Context getContext() {
		return this.kContext;
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T execute(final GenericCommand<T> command) {
		ksession.halt();

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		T result = (T) txTemplate.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				T result = (T) jpaTemplate.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						env.set(EnvironmentName.ENTITY_MANAGER, em);
						SessionInfo sessionInfoMerged = em.merge(sessionInfo);
						sessionInfoMerged.setJPASessionMashallingHelper(sessionInfo.getJPASessionMashallingHelper());
						sessionInfo = sessionInfoMerged;
						// sessionInfo = em.find(SessionInfo.class, sessionInfo.getId());
						// sessionInfo.setJPASessionMashallingHelper(marshallingHelper);
						// marshallingHelper.loadSnapshot(sessionInfo.getData(), ksession);
						T result = command.execute(kContext);
						em.flush();
						env.set(EnvironmentName.ENTITY_MANAGER, null);
						return result;
					}

				});
				// clean up cached process and work item instances
				((JPAProcessInstanceManager) ((StatefulKnowledgeSessionImpl) ksession).session
						.getProcessInstanceManager()).clearProcessInstances();
				((JPAWorkItemManager) ((StatefulKnowledgeSessionImpl) ksession).session.getWorkItemManager())
						.clearWorkItems();
				return result;
			}
		});
		return result;
	}

	public void dispose() {
		if (ksession != null) {
			ksession.dispose();
		}
	}

	public int getSessionId() {
		return sessionInfo.getId();
	}
}
