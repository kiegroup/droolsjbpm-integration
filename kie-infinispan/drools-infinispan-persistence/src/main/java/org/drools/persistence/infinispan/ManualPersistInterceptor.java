package org.drools.persistence.infinispan;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.AbstractInterceptor;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistenceContextManager;
import org.drools.persistence.SessionMarshallingHelper;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.persistence.info.SessionInfo;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.Context;
import org.kie.internal.marshalling.MarshallerFactory;

public class ManualPersistInterceptor extends AbstractInterceptor {

	private final SingleSessionCommandService interceptedService;
	
	public ManualPersistInterceptor(SingleSessionCommandService decorated) {
		this.interceptedService = decorated;
	}
	
	public <T> T execute(Command<T> command) {
		T result = executeNext(command);
		try {
	    	KieSession ksession = interceptedService.getKieSession();
	    	java.lang.reflect.Field sessionInfoField = SingleSessionCommandService.class.getDeclaredField("sessionInfo");
	    	sessionInfoField.setAccessible(true);
	    	java.lang.reflect.Field jpmField = SingleSessionCommandService.class.getDeclaredField("jpm");
	    	jpmField.setAccessible(true);
	    	Object jpm = jpmField.get(interceptedService);
	    	Object sessionInfo = sessionInfoField.get(interceptedService);
	    	if (!(command instanceof DisposeCommand)) {
	    		executeNext(new PersistCommand(sessionInfo, jpm, ksession));
	    	}
		} catch (Exception e) {
			throw new RuntimeException("Couldn't force persistence of session info", e);
		}
		return result;
	}

	public static class PersistCommand implements GenericCommand<Void> {
		
		private final SessionInfo sessionInfo;
		private final PersistenceContext persistenceContext;
		private final KieSession ksession;
		
		public PersistCommand(Object sessionInfo, Object jpm, KieSession ksession) {
			this.sessionInfo = (SessionInfo) sessionInfo;
			this.persistenceContext = ((PersistenceContextManager) jpm).getApplicationScopedPersistenceContext();
			this.ksession = ksession;
		}
		
		@Override
		public Void execute(Context context) {
			/*if (sessionInfo.getId() == null || sessionInfo.getId() <= 0) {
				sessionInfo.setJPASessionMashallingHelper(new SessionMarshallingHelper(
						ksession, ksession.getSessionConfiguration()));
			} else {
				sessionInfo.setJPASessionMashallingHelper(new SessionMarshallingHelper(
						ksession.getKieBase(), ksession.getSessionConfiguration(), 
						ksession.getEnvironment()));
			}*/
			if (sessionInfo.getJPASessionMashallingHelper() == null) {
				sessionInfo.setJPASessionMashallingHelper(new SessionMarshallingHelper(
						ksession.getKieBase(), ksession.getSessionConfiguration(), 
						ksession.getEnvironment()));
			}
			sessionInfo.update();
			persistenceContext.persist(sessionInfo);
			return null;
		}
	}

	public CommandService getInterceptedService() {
		return interceptedService;
	}
	
}
