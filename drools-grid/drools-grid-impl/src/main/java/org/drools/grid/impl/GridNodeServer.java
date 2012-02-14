/*
 * Copyright 2010 salaboy.
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
 * under the License.
 */
package org.drools.grid.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.ResolvingKnowledgeCommandContext;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.GridNode;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.io.impl.NodeData;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridNodeServer
        implements
        MessageReceiverHandler {

    private GridNode gnode;
    private NodeData data;
    private Map<String, String> sessions = new HashMap<String, String>();
    private static Logger logger = LoggerFactory.getLogger(GridNodeServer.class);

    public GridNodeServer(GridNode gnode,
            NodeData data) {
        if (logger.isDebugEnabled()) {
            logger.debug(" ### GridNodeServer: Creating GridNodeServer for node: " + gnode.getId());
        }
        this.gnode = gnode;
        this.data = data;
    }

    public void messageReceived(Conversation conversation,
            Message msg) {
        final CommandImpl cmd = (CommandImpl) msg.getBody();
        this.execs.get(cmd.getName()).execute(gnode,
                conversation,
                msg,
                cmd);
    }
    private Map<String, Exec> execs = new HashMap<String, Exec>() {

        {
            put("execute",
                    new Exec() {

                        public void execute(Object object,
                                Conversation con,
                                Message msg,
                                CommandImpl cmd) {

                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            GenericCommand command = (GenericCommand) list.get(0);

                            // Setup the evaluation context 
                            ContextImpl localSessionContext = new ContextImpl("session_" + cmd.getName(),
                                    data.getContextManager(),
                                    data.getTemp());
//                                                       
                            ExecutionResultImpl localKresults = new ExecutionResultImpl();
                            localSessionContext.set("kresults_" + cmd.getName(),
                                    localKresults);
                            //Bad Hack that works for now, I need to do a proper check (execute command, etc)
                            if (list.size() > 1) {
                                String instanceId = (String) list.get(1);
                                if (instanceId != null || !instanceId.equals("")) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(" ### GridNodeServer: I'm inside the node =" + gnode.getId() + " instance: " + gnode);
                                        logger.debug(" ### GridNodeServer: Setting in the local context instanceId = " + instanceId
                                                + " - session = " + gnode.get(instanceId, StatefulKnowledgeSession.class));
                                    }
                                    localSessionContext.set(instanceId, gnode.get(instanceId, StatefulKnowledgeSession.class));
                                }
                            }

                            ResolvingKnowledgeCommandContext resolvingContext = new ResolvingKnowledgeCommandContext(localSessionContext);
                            if (logger.isTraceEnabled()) {
                                        logger.trace(" ### GridNodeServer (execute): "+command);
                            }
                            Object result = command.execute(resolvingContext);

                            con.respond(result);
                        }
                    });
            put("registerKsession",
                    new Exec() {

                        public void execute(Object object,
                                Conversation con,
                                Message msg,
                                CommandImpl cmd) {
                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            String instanceId = (String) list.get(1);
                            // Set the already created session into the node localcontext
                            gnode.set((String) list.get(0), data.getTemp().get(instanceId));
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (registerKsession):  node: (" + gnode.getId() + ") - session: (" + (String) list.get(0) + ") - instanceId : " + instanceId);
                            }
                            sessions.put((String) list.get(0), instanceId);
                            // Respond nothing
                            con.respond(null);
                        }
                    });
            put("lookupKsession",
                    new Exec() {

                        public void execute(Object object,
                                Conversation con,
                                Message msg,
                                CommandImpl cmd) {
                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            String sessionId = (String) list.get(0);
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (lookupKsession): Looking inside gnode: (" + gnode.getId() + ") session: " + sessionId);
                            }
                            String instanceId = sessions.get(sessionId);
                            if (logger.isTraceEnabled()) {
                                logger.trace(" ### GridNodeServer (lookupKsession): Available Sessions: " + sessions);
                                logger.trace(" ### GridNodeServer (lookupKsession): Instance Id Found inside GridNodeServer for session (" + sessionId + ") - " + instanceId);
                            }
                            if (instanceId == null || instanceId.equals("")) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace(" ### GridNodeServer (lookupKsession): The session is in the local context: " + gnode.get(sessionId, String.class));
                                    logger.trace(" ### GridNodeServer (lookupKsession): I'm inside the node =" + gnode.getId() + " instance: " + gnode);
                                }
                                instanceId = gnode.get(sessionId, String.class);
                                sessions.put(sessionId, instanceId);
                            }
                            con.respond(instanceId);
                        }
                    });
            put("lookupKsessionId",
                    new Exec() {

                        public void execute(Object object,
                                Conversation con,
                                Message msg,
                                CommandImpl cmd) {
                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            String sessionId = (String) list.get(0);

                            String instanceId = sessions.get(sessionId);
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (lookupKsessionId): Available Sessions: " + sessions);
                                logger.debug(" ### GridNodeServer (lookupKsessionId): Instance Id Found inside gnode: (" + gnode.getId() + ") for session (" + sessionId + ") - " + instanceId);
                            }
                            if (instanceId == null || instanceId.equals("")) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(" ### GridNodeServer (lookupKsessionId): The session is in the local context: " + gnode.get(sessionId, String.class));
                                    logger.debug(" ### GridNodeServer (lookupKsessionId): I'm inside the node =" + gnode.getId() + " instance: " + gnode);
                                }
                                instanceId = gnode.get(sessionId, String.class);
                                sessions.put(sessionId, instanceId);
                            }

                            con.respond(instanceId);
                        }
                    });
        }
    };

    public static interface Exec {

        void execute(Object object,
                Conversation con,
                Message msg,
                CommandImpl cmd);
    }

    public NodeData getData() {
        return data;
    }
}
