package org.drools.grid.distributed;

import org.drools.definition.rule.Query;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Collection;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.command.FinishedCommand;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.SetVariableCommand;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.Process;
import org.drools.definition.rule.Rule;
import org.drools.definition.type.FactType;
import org.drools.event.knowledgebase.KnowledgeBaseEventListener;
import org.drools.grid.GenericConnection;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.grid.internal.CollectionClient;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageSession;
import org.drools.grid.distributed.command.NewStatefulKnowledgeSessionGridCommand;

public class KnowledgeBaseGridClient
        implements
        KnowledgeBase {

    private GenericNodeConnector connector;
    private MessageSession messageSession;
    private String instanceId;
    private GenericConnection connection;

    public KnowledgeBaseGridClient(String instanceId,
            GenericNodeConnector client, MessageSession messageSession, GenericConnection connection) {
        this.instanceId = instanceId;

        this.connector = client;
        this.messageSession = messageSession;
        this.connection = connection;
    }

    public void addKnowledgePackages(Collection<KnowledgePackage> kpackages) {
        String kresultsId = "kresults_" + messageSession.getSessionId();

        String kuilderInstanceId = ((CollectionClient<KnowledgePackage>) kpackages).getParentInstanceId();
        Message msg = new Message(messageSession.getSessionId(),
                messageSession.counter.incrementAndGet(),
                false,
                new KnowledgeContextResolveFromContextCommand(new KnowledgeBaseAddKnowledgePackagesCommand(),
                kuilderInstanceId,
                instanceId,
                null,
                kresultsId));

        //I should register the kbase ID??
        DirectoryNodeService directory = null;
        try {
            //get best directory????????/
            directory = connection.getDirectoryNode().get(DirectoryNodeService.class);
        } catch (ConnectorException ex) {
            Logger.getLogger(KnowledgeBaseGridClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (directory != null) {
            try {
                try {
                    directory.registerKBase(instanceId, connector.getId());
                } catch (RemoteException ex) {
                    Logger.getLogger(KnowledgeBaseGridClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ConnectorException ex) {
                Logger.getLogger(KnowledgeBaseGridClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }



        try {
            Object object = connector.write(msg).getPayload();

            if (!(object instanceof FinishedCommand)) {
                throw new RuntimeException("Response was not correctly ended");
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to execute message",
                    e);
        }
    }

    public FactType getFactType(String packageName,
            String typeName) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgePackage getKnowledgePackage(String packageName) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<KnowledgePackage> getKnowledgePackages() {
        // TODO Auto-generated method stub
        return null;
    }

    public Process getProcess(String processId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Rule getRule(String packageName,
            String ruleName) {
        // TODO Auto-generated method stub
        return null;
    }

    public StatefulKnowledgeSession newStatefulKnowledgeSession() {
        return newStatefulKnowledgeSession(null,
                null);
    }

    public StatefulKnowledgeSession newStatefulKnowledgeSession(KnowledgeSessionConfiguration conf,
            Environment environment) {
        String kresultsId = "kresults_" + messageSession.getSessionId();

        String localId = UUID.randomUUID().toString();

        Message msg = new Message(messageSession.getSessionId(),
                messageSession.counter.incrementAndGet(),
                false,
                new SetVariableCommand("__TEMP__",
                localId,
                new KnowledgeContextResolveFromContextCommand(new NewStatefulKnowledgeSessionGridCommand(null, instanceId),
                null,
                instanceId,
                null,
                kresultsId)));

        try {
            Object object = connector.write(msg).getPayload();

            if (!(object instanceof FinishedCommand)) {
                throw new RuntimeException("Response was not correctly ended");
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to execute message",
                    e);
        }

        return new StatefulKnowledgeSessionGridClient(localId,
                connector, messageSession);
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession(KnowledgeSessionConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

    public StatelessKnowledgeSession newStatelessKnowledgeSession() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeKnowledgePackage(String packageName) {
        // TODO Auto-generated method stub
    }

    public void removeProcess(String processId) {
        // TODO Auto-generated method stub
    }

    public void removeRule(String packageName,
            String ruleName) {
        // TODO Auto-generated method stub
    }

    public void addEventListener(KnowledgeBaseEventListener listener) {
        // TODO Auto-generated method stub
    }

    public Collection<KnowledgeBaseEventListener> getKnowledgeBaseEventListeners() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeEventListener(KnowledgeBaseEventListener listener) {
        // TODO Auto-generated method stub
    }

    public void removeFunction(String packageName, String ruleName) {
        // TODO Auto-generated method stub
    }

    public Query getQuery(String packageName, String queryName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeQuery(String packageName, String queryName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<StatefulKnowledgeSession> getStatefulKnowledgeSessions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
