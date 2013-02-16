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

package org.drools.grid.remote.command;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.SystemEventListenerService;
import org.drools.agent.conf.NewInstanceOption;
import org.drools.agent.conf.UseKnowledgeBaseClassloaderOption;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.grid.GridNode;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.command.Context;
import org.drools.impl.SystemEventListenerServiceImpl;
public class RegisterKAgentRemoteCommand
    implements
    GenericCommand<KnowledgeAgent> {

    private String kAgentId;
    

    public RegisterKAgentRemoteCommand(String kAgentId) {
        this.kAgentId = kAgentId;
        
    }

    public KnowledgeAgent execute( Context context ) {
        KnowledgeBase kbase = ( (KnowledgeCommandContext) context ).getKnowledgeBase();
        KnowledgeAgentConfiguration kaConfig = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
        kaConfig.setProperty( NewInstanceOption.PROPERTY_NAME, "false" );
        kaConfig.setProperty( UseKnowledgeBaseClassloaderOption.PROPERTY_NAME, "true" );
        KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( this.kAgentId, kbase, kaConfig );
        SystemEventListener systemEventListener = new SystemEventListener() {

            public void info(String string) {
                System.out.println("INFO: "+string);
            }

            public void info(String string, Object o) {
                System.out.println("INFO: "+string +", "+o);
            }

            public void warning(String string) {
                System.out.println("WARN: "+string );
            }

            public void warning(String string, Object o) {
                System.out.println("WARN: "+string +", "+o);
            }

            public void exception(String string, Throwable thrwbl) {
                System.out.println("EXCEPTION: "+string +", "+thrwbl);
            }

            public void exception(Throwable thrwbl) {
                System.out.println("EXCEPTION: "+thrwbl);
            }

            public void debug(String string) {
                System.out.println("DEBUG: "+string );
            }

            public void debug(String string, Object o) {
                System.out.println("DEBUG: "+string +", "+o);
            }
        };
        
        kagent.setSystemEventListener( systemEventListener );

        GridNode gn = (GridNode) context.get("grid_node");
        gn.set( this.kAgentId + "_kAgent", kagent );

        return kagent;
    }

}
