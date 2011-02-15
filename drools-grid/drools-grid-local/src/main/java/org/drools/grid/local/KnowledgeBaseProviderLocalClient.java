package org.drools.grid.local;

import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.grid.ConnectorType;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.GenericNodeConnector;
import org.drools.impl.KnowledgeBaseFactoryServiceImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;

public class KnowledgeBaseProviderLocalClient
    implements
    KnowledgeBaseFactoryService {

    private GenericNodeConnector        nodeConnector;
    private KnowledgeBaseFactoryService decoratee = new KnowledgeBaseFactoryServiceImpl();

    public KnowledgeBaseProviderLocalClient(GenericNodeConnector connector) {
        this.nodeConnector = connector;
    }

    public Environment newEnvironment() {
        return KnowledgeBaseFactory.newEnvironment();
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( (KnowledgeBaseConfiguration) null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        return KnowledgeBaseFactory.newKnowledgeBase( conf );
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        return KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader classLoader) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        KnowledgeBase kbase = this.decoratee.newKnowledgeBase( kbaseId );
        registerKbaseInDirectories( kbaseId,
                                    kbase );
        return kbase;
    }

    private void registerKbaseInDirectories(String kbaseId,
                                            KnowledgeBase kbase) throws IllegalStateException {
        try {
            ConnectorType connectorType = this.nodeConnector.getConnectorType();
            DirectoryNodeService directory = this.nodeConnector.getConnection().getDirectoryNode().get( DirectoryNodeService.class );
            if ( connectorType == ConnectorType.LOCAL ) {
                directory.registerKBase( kbaseId,
                                         kbase );
            } else {
                directory.registerKBase( kbaseId,
                                         this.nodeConnector.getId() );
            }
            directory.dispose();
        } catch ( Exception e ) {
            throw new IllegalStateException( "Unable to register kbase " + kbaseId + " in directory",
                                             e );
        }
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        KnowledgeBase kbase = this.decoratee.newKnowledgeBase( kbaseId,
                                                               conf );
        registerKbaseInDirectories( kbaseId,
                                    kbase );
        return kbase;
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader... classLoader) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
