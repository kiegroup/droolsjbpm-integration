/*
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
package org.drools.grid.internal.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.kie.command.Context;
import org.kie.runtime.KnowledgeRuntime;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.WorkItemHandler;

public class RegisterWorkItemHandlerRemoteCommand implements GenericCommand<Object> {

    private String handlerClassName;
    private String workItemName;

    public RegisterWorkItemHandlerRemoteCommand() {
    }

    public RegisterWorkItemHandlerRemoteCommand(String workItemName, String handlerClassName) {

        this.handlerClassName = handlerClassName;
        this.workItemName = workItemName;
    }

    public String getHandlerClassName() {
        return handlerClassName;
    }

    public String getWorkItemName() {
        return workItemName;
    }

    public void setWorkItemName(String workItemName) {
        this.workItemName = workItemName;
    }

    public Object execute(Context context) {

        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        Class workItemHandlerClass  = null;
        try {
            workItemHandlerClass = Class.forName(handlerClassName);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
        }

        Constructor constructor = null;
        boolean initializeWithSession = false;
        try {
            constructor = workItemHandlerClass.getConstructor(KnowledgeRuntime.class);
            initializeWithSession = true;
        } catch (NoSuchMethodException noex) {
        }

        if (!initializeWithSession) {
            try {
                constructor = workItemHandlerClass.getConstructor();
            } catch (NoSuchMethodException noex) {
            }
        }


        WorkItemHandler reflectionHandler = null;

        if (initializeWithSession) {

            try {
                reflectionHandler = (WorkItemHandler) constructor.newInstance(ksession);
            } catch (InstantiationException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {

            try {
                reflectionHandler = (WorkItemHandler) constructor.newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(RegisterWorkItemHandlerRemoteCommand.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (reflectionHandler == null) {
            throw new IllegalStateException("No WorkItemHandler Instantiated for " + workItemName);
        }
        ksession.getWorkItemManager().registerWorkItemHandler(workItemName, reflectionHandler);

        return null;
    }

    public String toString() {
        return "Remote: session.getWorkItemManager().registerWorkItemHandler("
                + workItemName + ", " + handlerClassName + ");";
    }
}
