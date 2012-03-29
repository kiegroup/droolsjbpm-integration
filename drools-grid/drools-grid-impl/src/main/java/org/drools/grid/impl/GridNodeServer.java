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
    //This map keesp the relationship between the session name and the generated id inside the gnode
    // Example: session1, <UUID generated inside this specific gnode> 
    private Map<String, String> sessions = new HashMap<String, String>();
    // This map keeps the relationship between the clientSessionid and the Session name
    // Example: <UUID-SessionId>, session1
    private Map<String, String> clientSessions = new HashMap<String, String>();

    private Map<String, String> internalSessionsExposed = new HashMap<String, String>();
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

                            //This is a set of Bad Hack that works for now, I need to do a proper check (execute command, etc)
                            // These hacks where done to make it work and must be corrected to make it work properly
                            if (list.size() > 1) {
                                String instanceId = (String) list.get(1);
                                if (instanceId != null || !instanceId.equals("")) {

//                                  
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(" ### GridNodeServer(execute): Looking for id: =" + instanceId + " inside gnode");

                                    }
                                    if( logger.isTraceEnabled()){
                                        logger.trace(" ### GridNodeServer(execute): sessions mappings: =" + sessions.keySet());
                                        logger.trace(" ### GridNodeServer(execute): client sessions mappings: =" + clientSessions.keySet());
                                        logger.trace(" ### GridNodeServer(execute): internal sessions exposed mappings: =" + internalSessionsExposed.keySet());
                                        logger.trace(" ### GridNodeServer(execute): sessions mappings values: =" + sessions.values());
                                        logger.trace(" ### GridNodeServer(execute): client sessions mappings values: =" + clientSessions.values());
                                        logger.trace(" ### GridNodeServer(execute): internal sessions exposed mappings values: =" + internalSessionsExposed.values());
                                    }


                                    String sessionName = clientSessions.get(instanceId);
                                    if(sessionName == null || sessionName.equals("")){
                                        sessionName = internalSessionsExposed.get(instanceId);
                                    }
                                    StatefulKnowledgeSession ksession = gnode.get(sessionName, StatefulKnowledgeSession.class);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(" ### GridNodeServer(execute): Looking for id: =" + instanceId + " inside (sessionName = " + sessionName + ")cached client sessions - result: " + ksession);
                                    }

                                    if (ksession != null) {
                                        localSessionContext.set(instanceId, ksession);
                                    }

                                }
                            }
                            ResolvingKnowledgeCommandContext resolvingContext = new ResolvingKnowledgeCommandContext(localSessionContext);

                            if (logger.isTraceEnabled()) {
                                logger.trace(" ### GridNodeServer (execute): " + command);
                            }
                            Object result = command.execute(resolvingContext);

                            con.respond(result);
                        }
                    });
            put(
                    "registerKsession",
                    new Exec() {

                        public void execute(Object object,
                                            Conversation con,
                                            Message msg,
                                            CommandImpl cmd) {
                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            String sessionName = (String) list.get(0);
                            String clientInstanceId = (String) list.get(1);

                            // Set the already created session into the node localcontext
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (registerKsession):  registering into GNODE - sessionName: (" + sessionName + ") - instanceId : " + clientInstanceId);
                            }
                            // Inside the Gnode and inside the local cache we have the locally generated IDs
                            gnode.set(sessionName, data.getTemp().get(clientInstanceId));
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (registerKsession):  param: (" + clientInstanceId + ") - resolve from node String.class : " + gnode.get(sessionName, String.class));
                            }
                            sessions.put(sessionName, gnode.get(sessionName, String.class));

                            clientSessions.put(clientInstanceId, sessionName);

                            if (logger.isTraceEnabled()) {
                                logger.trace(" ### GridNodeServer (registerKsession): clientSession Entry [ " + clientInstanceId + " , " + sessionName + " ]");
                                logger.trace(" ### GridNodeServer (registerKsession): sessions Entry [ " + sessionName + " , " + clientInstanceId + " ]");
                            }
                            // Respond nothing
                            con.respond(null);
                        }
                    });
            put(
                    "lookupKsession",
                    new Exec() {

                        public void execute(Object object,
                                            Conversation con,
                                            Message msg,
                                            CommandImpl cmd) {
                            GridNode gnode = (GridNode) object;
                            List list = cmd.getArguments();
                            String sessionName = (String) list.get(0);
                            if (logger.isDebugEnabled()) {
                                logger.debug("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession):  node: (" + gnode.getId() + ") - sessionname: (" + sessionName + ")");

                            }
                            if( logger.isTraceEnabled()){
                                logger.trace("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession):  \t available client sessions: " + clientSessions.keySet());
                                logger.trace("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession):  \t available cached sessions: " + sessions.keySet());
                            }
                            String clientSessionId = clientSessions.get(sessionName);

                            if (clientSessionId == null || clientSessionId.equals("")) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession): The session is in the local context: " + gnode.get(sessionName, String.class));
                                    logger.debug(" ### GridNodeServer (lookupKsession): I'm inside the node =" + gnode.getId() + " instance: " + gnode);

                                }
                                clientSessionId = gnode.get(sessionName, String.class);
                                if ( logger.isDebugEnabled() ) {
                                    logger.debug("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession): Registering internal Session Id into internalSessionExposed with sessionId: =" + clientSessionId + " for session name: " + sessionName);
                                }
                                internalSessionsExposed.put( clientSessionId, sessionName);

                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug("(" + Thread.currentThread().getId() + ")"+ Thread.currentThread().getName() +" ### GridNodeServer (lookupKsession):  return =" + clientSessionId);

                            }
                            con.respond(clientSessionId);
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
                            logger.debug(" ### GridNodeServer (lookupKsessionId): SessionID???????: " + sessionId);
                            String gnodeInternalSessionId = clientSessions.get(sessionId);
                            if (logger.isDebugEnabled()) {
                                logger.debug(" ### GridNodeServer (lookupKsessionId): Available Client Sessions: " + clientSessions);
                                logger.debug(" ### GridNodeServer (lookupKsessionId): Available Sessions: " + sessions);
                                logger.debug(" ### GridNodeServer (lookupKsessionId): Instance Id Found inside gnode: (" + gnode.getId() + ") for session (" + sessionId + ") - " + gnodeInternalSessionId);


                            }
                            if (gnodeInternalSessionId == null || gnodeInternalSessionId.equals("")) {
                                for(String key : clientSessions.keySet()){
                                    if(clientSessions.get(key).equals(sessionId)){
                                        gnodeInternalSessionId = key;
                                    }
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug(" ### GridNodeServer (lookupKsessionId): gnodeInternalSessionId found using reverse lookup: " + gnodeInternalSessionId );
                                }
                            }
                            if (gnodeInternalSessionId == null || gnodeInternalSessionId.equals("")) {
                                for(String key : internalSessionsExposed.keySet()){
                                    if(internalSessionsExposed.get(key).equals(sessionId)){
                                        gnodeInternalSessionId = key;
                                    }
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug(" ### GridNodeServer (lookupKsessionId): gnodeInternalSessionId found using reverse lookup in internalSessionExposed: " + gnodeInternalSessionId );
                                }
                            }
                            if (gnodeInternalSessionId == null || gnodeInternalSessionId.equals("")) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(" ### GridNodeServer (lookupKsessionId): The session is in the local context: " + gnode.get(sessionId, String.class));
                                    logger.debug(
                                            " ### GridNodeServer (lookupKsessionId): I'm inside the node =" + gnode.getId() + " instance: " + gnode);
                                }
                                gnodeInternalSessionId = gnode.get(sessionId, String.class);
                                sessions.put(sessionId, gnodeInternalSessionId);
                            }
                            con.respond(gnodeInternalSessionId);
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