package org.drools.container.spring.beans;

import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatelessKnowledgeSession;

public class StatelessKnowledgeSessionBeanFactory extends AbstractKnowledgeSessionBeanFactory {
    private StatelessKnowledgeSession ksession;
    private KnowledgeAgent kagent;
    
    public void setKnowledgeAgent(KnowledgeAgent kagent) {
    	this.kagent = kagent;
    }
    
    public KnowledgeAgent getKnowledgeAgent() {
    	return this.kagent;
    }

    public Class<StatelessKnowledgeSession> getObjectType() {
        return StatelessKnowledgeSession.class;
    }

    @Override
    protected CommandExecutor getCommandExecutor() {
        return ksession;
    }

    @Override
    protected void internalAfterPropertiesSet() {
    	if ( this.kagent != null ) {
    		ksession = this.kagent.newStatelessKnowledgeSession();
    	} else {
    		ksession = getKbase().newStatelessKnowledgeSession();
    	}
    }
}
