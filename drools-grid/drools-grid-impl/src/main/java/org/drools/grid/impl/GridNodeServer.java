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
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.GridNode;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.io.impl.NodeData;
import org.drools.runtime.impl.ExecutionResultImpl;

/**
 *
 * @author salaboy
 */
public class GridNodeServer
    implements
    MessageReceiverHandler {
    private GridNode gnode;
    private NodeData data;

    public GridNodeServer(GridNode gnode,
                          NodeData data) {
        this.gnode = gnode;
        this.data = data;
    }
    
    public void messageReceived(Conversation conversation,
                                Message msg) {
        final CommandImpl cmd = (CommandImpl) msg.getBody();
        this.execs.get( cmd.getName() ).execute( gnode,
                                                 conversation,
                                                 msg,
                                                 cmd );
    }

    private Map<String, Exec> execs = new HashMap<String, Exec>() {
                                        {
                                            put( "execute",
                                                 new Exec() {
                                                     public void execute(Object object,
                                                                         Conversation con,
                                                                         Message msg,
                                                                         CommandImpl cmd) {
                                                         GridNode gnode = (GridNode) object;
                                                         List list = cmd.getArguments();
                                                         GenericCommand command = (GenericCommand) list.get( 0 );

                                                         // Setup the evaluation context 
                                                         ContextImpl localSessionContext = new ContextImpl( "session_" + cmd.getName(),
                                                                                                            data.getContextManager(),
                                                                                                            data.getTemp() );
                                                         ExecutionResultImpl localKresults = new ExecutionResultImpl();
                                                         localSessionContext.set( "kresults_" + cmd.getName(),
                                                                                  localKresults );

                                                         Object result = command.execute( localSessionContext );

                                                         con.respond( result );
                                                     }
                                                 } );
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
