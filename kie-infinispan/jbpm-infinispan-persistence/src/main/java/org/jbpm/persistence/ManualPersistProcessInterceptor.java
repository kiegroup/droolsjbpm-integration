package org.jbpm.persistence;

import java.util.Collection;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.AbstractInterceptor;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.CreateCorrelatedProcessInstanceCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.persistence.SingleSessionCommandService;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.jbpm.process.instance.ProcessInstance;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.Context;

public class ManualPersistProcessInterceptor extends AbstractInterceptor {

	private final SingleSessionCommandService interceptedService;

	public ManualPersistProcessInterceptor(SingleSessionCommandService interceptedService) {
		this.interceptedService = interceptedService;
	}

	@Override
	public <T> T execute(Command<T> command) {
		RuntimeException error = null;
		T result = null;
		try {
			result = executeNext(command);
		} catch (RuntimeException e) {
			//still try to save
			error = e;
		}
    	KieSession ksession = interceptedService.getKieSession();
    	try {
    		java.lang.reflect.Field jpmField = SingleSessionCommandService.class.getDeclaredField("jpm");
    		jpmField.setAccessible(true);
    		Object jpm = jpmField.get(interceptedService);
    		if (error == null && isValidCommand(command)) {
    			executeNext(new PersistProcessCommand(jpm, ksession));
    		}
    	} catch (Exception e) {
			throw new RuntimeException("Couldn't force persistence of process instance infos", e);
		}
		if (error != null) {
			throw error;
		}
		return result;
	}
	
	protected boolean isValidCommand(Command<?> command) {
		return (command instanceof StartProcessCommand) ||
			   (command instanceof CreateProcessInstanceCommand) ||
			   (command instanceof CreateCorrelatedProcessInstanceCommand) ||
			   (command instanceof StartProcessInstanceCommand) ||
			   (command instanceof SignalEventCommand) ||
			   (command instanceof CompleteWorkItemCommand) ||
			   (command instanceof AbortWorkItemCommand) ||
			   (command instanceof AbortProcessInstanceCommand) ||
			   (command instanceof FireAllRulesCommand);
	}
	
	public static class PersistProcessCommand implements GenericCommand<Void> {
		
		private final ProcessPersistenceContext persistenceContext;
		private final KieSession ksession;
		
		public PersistProcessCommand(Object jpm, KieSession ksession) {
			this.persistenceContext = ((ProcessPersistenceContextManager) jpm).getProcessPersistenceContext();
			this.ksession = ksession;
		}
		
		@Override
		public Void execute(Context context) {
			Collection<?> processInstances = ksession.getProcessInstances();
			for (Object obj : processInstances) {
				ProcessInstance instance = (ProcessInstance) obj;
				boolean notCompeted = instance.getState() != ProcessInstance.STATE_COMPLETED;
				boolean notAborted = instance.getState() != ProcessInstance.STATE_ABORTED;
				boolean hasId = instance.getId() > 0;
				if (hasId && notCompeted && notAborted) {
					ProcessInstanceInfo info = new ProcessInstanceInfo(instance, ksession.getEnvironment());
					info.setId(instance.getId());
					info.transform();
					persistenceContext.persist(info);
				}
			}
			return null;
		}
	}

	public CommandService getInterceptedService() {
		return interceptedService;
	}
}
