/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.grid.remote;

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
 * @author salaboy
 */
public class WorkItemManagerRemoteClient implements WorkItemManager, Serializable {

	private static final long serialVersionUID = 1L;
	
	private GenericNodeConnector     client;
        private MessageSession              messageSession;
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
