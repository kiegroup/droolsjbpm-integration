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

package org.jbpm.persistence;

import org.drools.core.command.SingleSessionCommandService;
import org.drools.core.command.impl.AbstractInterceptor;
import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.runtime.process.AbortProcessInstanceCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.CreateCorrelatedProcessInstanceCommand;
import org.drools.core.command.runtime.process.CreateProcessInstanceCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.process.StartProcessInstanceCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.fluent.impl.InternalExecutable;
import org.drools.persistence.PersistableRunner;
import org.jbpm.persistence.api.ProcessPersistenceContext;
import org.jbpm.persistence.api.ProcessPersistenceContextManager;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.jbpm.process.instance.ProcessInstance;
import org.kie.api.command.Command;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.Executable;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.RequestContext;

import java.util.Collection;

public class ManualPersistProcessInterceptor extends AbstractInterceptor {

	private final SingleSessionCommandService interceptedService;

	public ManualPersistProcessInterceptor(SingleSessionCommandService interceptedService) {
		this.interceptedService = interceptedService;
	}

	@Override
	public RequestContext execute( Executable executable, RequestContext ctx ) {
		RuntimeException error = null;
		try {
			executeNext(executable,ctx);
		} catch (RuntimeException e) {
			//still try to save
			error = e;
		}
    	KieSession ksession = interceptedService.getKieSession();
    	try {
    		java.lang.reflect.Field jpmField = PersistableRunner.class.getDeclaredField( "jpm" );
    		jpmField.setAccessible(true);
    		Object jpm = jpmField.get(interceptedService);
    		if (error == null && isValidCommand( ( (InternalExecutable) executable ).getBatches().get(0).getCommands().get(0))) {
    			executeNext(new SingleCommandExecutable( new PersistProcessCommand(jpm, ksession) ), ctx);
    		}
    	} catch (Exception e) {
			throw new RuntimeException("Couldn't force persistence of process instance infos", e);
		}
		if (error != null) {
			throw error;
		}
		return ctx;
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
	
	public static class PersistProcessCommand implements ExecutableCommand<Void> {
		
		private final ProcessPersistenceContext persistenceContext;
		private final KieSession ksession;
		
		public PersistProcessCommand(Object jpm, KieSession ksession) {
			this.persistenceContext = ((ProcessPersistenceContextManager) jpm).getProcessPersistenceContext();
			this.ksession = ksession;
		}
		
		@Override
		public Void execute(Context context ) {
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

	public SingleSessionCommandService getInterceptedService() {
		return interceptedService;
	}
}
