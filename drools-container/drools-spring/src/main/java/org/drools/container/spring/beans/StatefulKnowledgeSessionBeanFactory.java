package org.drools.container.spring.beans;

import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatefulKnowledgeSession;

public class StatefulKnowledgeSessionBeanFactory extends AbstractKnowledgeSessionBeanFactory  {
	private StatefulKnowledgeSession ksession;

	public Class<StatefulKnowledgeSession> getObjectType() {
		return StatefulKnowledgeSession.class;
	}

	@Override
	protected CommandExecutor getCommandExecutor() {
		return ksession;
	}

	@Override
	protected void internalAfterPropertiesSet() {
		ksession = getKbase().newStatefulKnowledgeSession();
	}
}
