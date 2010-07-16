package org.drools.container.spring.beans;

import java.util.Map;

import org.drools.SessionConfiguration;
import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;

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
        if ( getConf() != null && getWorkItems() != null && !getWorkItems().isEmpty() ) {
            Map<String, WorkItemHandler> map = ((SessionConfiguration) getConf()).getWorkItemHandlers();
            map.putAll( getWorkItems() );
        }
        
    	if ( this.kagent != null ) {
    		ksession = this.kagent.newStatelessKnowledgeSession( getConf() );
    	} else {
    		ksession = getKbase().newStatelessKnowledgeSession( getConf() );
    	}
    }
}
