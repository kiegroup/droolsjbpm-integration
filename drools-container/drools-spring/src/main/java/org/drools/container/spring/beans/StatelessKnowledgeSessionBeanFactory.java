package org.drools.container.spring.beans;

import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatelessKnowledgeSession;

public class StatelessKnowledgeSessionBeanFactory extends AbstractKnowledgeSessionBeanFactory  {
	private StatelessKnowledgeSession ksession;

	public Class<StatelessKnowledgeSession> getObjectType() {
		return StatelessKnowledgeSession.class;
	}

	@Override
	protected CommandExecutor getCommandExecutor() {
		return ksession;
	}

	@Override
	protected void internalAfterPropertiesSet() {
		ksession = getKbase().newStatelessKnowledgeSession();
	}
}
