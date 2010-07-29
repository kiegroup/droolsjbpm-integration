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

package org.drools.grid.command;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;

public class RegisterRemoteWorkItemHandlerCommand implements GenericCommand<Object> {
	
	private String handler;
	private String workItemName;

        public RegisterRemoteWorkItemHandlerCommand() {
        }

        public RegisterRemoteWorkItemHandlerCommand(String workItemName, String handler) {
            this.handler = handler;
            this.workItemName = workItemName;
        }
        
	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getWorkItemName() {
		return workItemName;
	}

	public void setWorkItemName(String workItemName) {
		this.workItemName = workItemName;
	}

    public Object execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession(); 
        WorkItemHandler workItemHandler = null;
        try {
             Class t = Class.forName(handler);
             Constructor c = t.getConstructor(KnowledgeRuntime.class);
             workItemHandler =  (WorkItemHandler) c.newInstance(ksession);
        } catch (InstantiationException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RegisterRemoteWorkItemHandlerCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        ksession.getWorkItemManager().registerWorkItemHandler(workItemName, workItemHandler);
		return null;
	}

	public String toString() {
		return "session.getWorkItemManager().registerWorkItemHandler("
			+ workItemName + ", " + handler +  ");";
	}

}