package org.jbpm.persistence.processinstance;

import java.util.List;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.jbpm.persistence.ProcessPersistenceContextManager;
import org.jbpm.process.instance.event.DefaultSignalManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.runtime.EnvironmentName;

public class InfinispanSignalManager extends DefaultSignalManager {

    public InfinispanSignalManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
    }
    
    public void signalEvent(String type,
                            Object event) {
        for ( long id : getProcessInstancesForEvent( type ) ) {
            try {
           		getKnowledgeRuntime().getProcessInstance( id );
            } catch (IllegalStateException e) {
                // IllegalStateException can be thrown when using RuntimeManager
                // and invalid ksession was used for given context
            }
        }
        super.signalEvent( type,
                           event );
    }

    private List<Long> getProcessInstancesForEvent(String type) {
        ProcessPersistenceContext context = ((ProcessPersistenceContextManager) getKnowledgeRuntime().getEnvironment().get( EnvironmentName.PERSISTENCE_CONTEXT_MANAGER )).getProcessPersistenceContext();
        return context.getProcessInstancesWaitingForEvent(type);
    }

}
