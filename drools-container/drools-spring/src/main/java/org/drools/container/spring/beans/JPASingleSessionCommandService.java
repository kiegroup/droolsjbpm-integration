package org.drools.container.spring.beans;

import org.drools.command.SingleSessionCommandService;


public interface JPASingleSessionCommandService  {
	public SingleSessionCommandService newStatefulKnowledgeSession();
	public SingleSessionCommandService loadStatefulKnowledgeSession(int sessionId);
}
