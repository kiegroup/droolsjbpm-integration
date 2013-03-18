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

package org.drools.grid.remote;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.drools.core.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.core.command.KnowledgeContextResolveFromContextCommand;
import org.drools.core.command.SetVariableCommandFromCommand;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.impl.CollectionClient;
import org.drools.grid.io.impl.CommandImpl;
import org.drools.grid.remote.command.NewStatefulKnowledgeSessionFromKAgentRemoteCommand;
import org.drools.grid.remote.command.RegisterKAgentRemoteCommand;
import org.kie.KnowledgeBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.KnowledgePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatefulKnowledgeSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.StatelessKnowledgeSession;

public class KnowledgeBaseRemoteClient
    implements
    KnowledgeBase {

    private String                           instanceId;
    private ConversationManager              cm;
    private GridServiceDescription<GridNode> gsd;
    private KnowledgeBaseConfigurationRemoteClient conf;
    
    private Long timeout;
    private Long minWaitTime;

    public KnowledgeBaseRemoteClient(String localId,
                                     GridServiceDescription gsd,
                                     ConversationManager cm,
                                     KnowledgeBaseConfigurationRemoteClient conf) {
        this.instanceId = localId;
        this.cm = cm;
        this.gsd = gsd;
        this.conf = conf;
        
        //Configure timeouts
        if (this.conf != null){
            String configuredTimeout = this.conf.getProperty(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_TIMEOUT);
            if (configuredTimeout != null){
                timeout = Long.parseLong(configuredTimeout);
            }
            String configuredMinWaitTime = this.conf.getProperty(KnowledgeBaseConfigurationRemoteClient.PROPERTY_MESSAGE_MINIMUM_WAIT_TIME);
            if (configuredMinWaitTime != null){
                minWaitTime = Long.parseLong(configuredMinWaitTime);
            }
        }
        
    }

    public void addKnowledgePackages(Collection<KnowledgePackage> kpackages) {
        String kuilderInstanceId = ((CollectionClient<KnowledgePackage>) kpackages).getParentInstanceId();
        String kresultsId = "kresults_" + this.cm.toString();
        String localId = UUID.randomUUID().toString();

        CommandImpl cmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new KnowledgeContextResolveFromContextCommand( new KnowledgeBaseAddKnowledgePackagesCommand(),
                                                                                                                       kuilderInstanceId,
                                                                                                                       this.instanceId,
                                                                                                                       null,
                                                                                                                       kresultsId )} ) );

        this.sendMessage(cmd);
        
    }

    public Collection<KnowledgePackage> getKnowledgePackages() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KnowledgePackage getKnowledgePackage(String packageName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeKnowledgePackage(String packageName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Rule getRule(String packageName,
                        String ruleName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeRule(String packageName,
                           String ruleName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Query getQuery(String packageName,
                          String queryName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeQuery(String packageName,
                            String queryName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeFunction(String packageName,
                               String ruleName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public FactType getFactType(String packageName,
                                String typeName) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Process getProcess(String processId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeProcess(String processId) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<Process> getProcesses() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KieSession newKieSession(KieSessionConfiguration conf, Environment environment) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public KieSession newKieSession() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<? extends KieSession> getKieSessions() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public StatelessKieSession newStatelessKieSession(KieSessionConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public StatelessKieSession newStatelessKieSession() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public StatefulKnowledgeSession newStatefulKnowledgeSession(KieSessionConfiguration conf,
                                                                Environment environment) {
        String kresultsId = "kresults_" + this.cm.toString();
        String localId = UUID.randomUUID().toString();
        
        CommandImpl registerKAgentCmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommandFromCommand( "__TEMP__",
                                                                                                localId+"_kAgent",
                                                                                                new KnowledgeContextResolveFromContextCommand( new RegisterKAgentRemoteCommand( localId ),
                                                                                                                                               null,
                                                                                                                                               this.instanceId,
                                                                                                                                               null,
                                                                                                                                               kresultsId ) )} ) );
        
        this.sendMessage(registerKAgentCmd);
        
        String ksessionConfId = null;
        if (conf != null){
            ((KnowledgeSessionConfigurationRemoteClient)conf).getId();
        }
         
        CommandImpl newSessionCmd = new CommandImpl( "execute",
                                           Arrays.asList( new Object[]{new SetVariableCommandFromCommand( "__TEMP__",
                                                                                                localId,
                                                                                                new KnowledgeContextResolveFromContextCommand( new NewStatefulKnowledgeSessionFromKAgentRemoteCommand( ksessionConfId , environment, localId),
                                                                                                                                               null,
                                                                                                                                               this.instanceId,
                                                                                                                                               null,
                                                                                                                                               kresultsId ) )} ) );

        this.sendMessage(newSessionCmd);

        return new StatefulKnowledgeSessionRemoteClient( localId,
                                                         this.gsd,
                                                         this.cm,
                                                         (KnowledgeSessionConfigurationRemoteClient)conf);

    }

    public StatefulKnowledgeSession newStatefulKnowledgeSession() {
        return newStatefulKnowledgeSession( null,
                                            null );
    }

    public Collection<StatefulKnowledgeSession> getStatefulKnowledgeSessions() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession(KieSessionConfiguration conf) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void addEventListener(KieBaseEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void removeEventListener(KieBaseEventListener listener) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Collection<KieBaseEventListener> getKieBaseEventListeners() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public Set<String> getEntryPointIds() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    
    private Object sendMessage(Object body){
        
        //send the message
        return ConversationUtil.sendMessage(this.cm,
                (InetSocketAddress) this.gsd.getAddresses().get("socket").getObject(),
                this.gsd.getId(),
                body,
                minWaitTime,
                timeout);
        
        
    }

    public Collection<KiePackage> getKiePackages() {
        return getKiePackages();
    }

    public KiePackage getKiePackage(String packageName) {
        return getKnowledgePackage(packageName);
    }

    public void removeKiePackage(String packageName) {
        removeKnowledgePackage(packageName);
    }
}
