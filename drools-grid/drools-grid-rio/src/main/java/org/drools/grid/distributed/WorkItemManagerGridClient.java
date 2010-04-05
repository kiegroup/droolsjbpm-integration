package org.drools.grid.distributed;


import java.io.Serializable;
import java.util.Map;

import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.drools.grid.generic.GenericNodeConnector;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageSession;


/**
 *
 * @author Lucas Amador
 * @author salaboy: salaboy@gmail.com
 */
public class WorkItemManagerGridClient implements WorkItemManager, Serializable {

	private static final long serialVersionUID = 1L;
	
	//private GridExecutionNodeConnection nodeConnection;
        private GenericNodeConnector client;
        private MessageSession messageSession;
	private String instanceId;

	public void abortWorkItem(long id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void completeWorkItem(long id, Map<String, Object> results) {
		String kresultsId = "kresults_" + messageSession.getSessionId();
        Message msg = new Message( messageSession.getSessionId(),
                                   messageSession.counter.incrementAndGet(),
                                   true,
                                   new KnowledgeContextResolveFromContextCommand( new CompleteWorkItemCommand(id, results),
                                                                                  null,
                                                                                  null,
                                                                                  instanceId,
                                                                                  kresultsId ) );
        try {
            client.write( msg );
        } catch ( Exception e ) {
            throw new RuntimeException( "Unable to execute message", e );
        }
	}

	public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

        public void setClient(GenericNodeConnector client) {
            this.client = client;
        }

        public void setMessageSession(MessageSession messageSession) {
            this.messageSession = messageSession;
        }

	

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

}
