/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.grid;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;

/**
 *
 * @author salaboy
 */
public class GenericConnectorFactory {

    private static final String SEPARATOR                                 = ":";
    private static final String LOCAL_NODE_CONNECTOR_CLASS                = "org.drools.grid.local.LocalNodeConnector";
    private static final String LOCAL_DIRECTORY_CONNECTOR_CLASS           = "org.drools.grid.local.LocalDirectoryConnector";
    private static final String REMOTE_MINA_NODE_CONNECTOR_CLASS          = "org.drools.grid.remote.mina.RemoteMinaNodeConnector";
    private static final String REMOTE_MINA_DIRECTORY_CONNECTOR_CLASS     = "org.drools.grid.remote.directory.RemoteMinaDirectoryConnector";
    private static final String REMOTE_MINA_TASK_CONNECTOR_CLASS          = "org.drools.grid.task.RemoteMinaHumanTaskConnector";
    private static final String REMOTE_HORNETQ_NODE_CONNECTOR_CLASS       = "";
    private static final String REMOTE_HORNETQ_DIRECTORY_CONNECTOR_CLASS  = "";
    private static final String REMOTE_HORNETQ_TASK_CONNECTOR_CLASS       = "";
    private static final String DISTRIBUTED_RIO_NODE_CONNECTOR_CLASS      = "org.drools.grid.distributed.connectors.DistributedRioNodeConnector";
    private static final String DISTRIBUTED_RIO_DIRECTORY_CONNECTOR_CLASS = "org.drools.grid.distributed.connectors.DistributedRioDirectoryConnector";
    private static final String DISTRIBUTED_RIO_TASK_CONNECTOR_CLASS      = "";

    /* connectorString format:
     *  [0] -> Environment Type: Local, Remote, Distributed
     *  [1] -> Implementation: Local, Mina, HornetQ, Rio
     *  [2] -> Connector Type: Node, Directory, Task
     *  [3] -> Connector Id: <ID of the Execution, Directory or Task node>,
     *         The connector ID let us create the connector to a specific,
     *         usually remote/distributed node,directory or taskserver.
     *
     *  examples:
     *  Local Directory Connector String: Local:Local:Directory
     *  Remote Mina Node Connector String: Remote:Mina:Node:127.0.0.1:9123
     *  Distributed Rio Directory Connector String: Distributed:Rio:Directory:<ID>
     */
    public static GenericNodeConnector newConnector(String connectorString) {
        GenericNodeConnector connector = null;
        String[] connectorDetails = getConnectorDetails( connectorString );
        String environmentType = connectorDetails[0];
        if ( environmentType.equals( "Local" ) ) {
            connector = newLocalConnector( connectorString );
        }
        if ( environmentType.equals( "Remote" ) ) {
            connector = newRemoteConnector( connectorString );
        }
        if ( environmentType.equals( "Distributed" ) ) {
            connector = newDistributedConnector( connectorString );
        }

        return connector;
    }

    private static GenericNodeConnector newLocalConnector(String connectorString) {
        GenericNodeConnector connector = null;
        String[] connectorDetails = getConnectorDetails( connectorString );
        if ( connectorDetails[1].equals( "Local" ) && connectorDetails[2].equals( "Node" ) ) {
            //TODO: add ID to the local connector (connectorString)
            connector = newLocalNodeConnector();
        }
        if ( connectorDetails[1].equals( "Local" ) && connectorDetails[2].equals( "Directory" ) ) {
            connector = newLocalDirectoryConnector();
        }
        if ( connectorDetails[1].equals( "Local" ) && connectorDetails[2].equals( "Task" ) ) {
            throw new UnsupportedOperationException( "We don't have a Local Task implementation! Sorry!" );
        }

        return connector;
    }

    private static GenericNodeConnector newLocalNodeConnector() {
        GenericNodeConnector connector = null;
        try {
            Class clazz = Class.forName( LOCAL_NODE_CONNECTOR_CLASS );
            connector = (GenericNodeConnector) clazz.newInstance();
        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static GenericNodeConnector newLocalDirectoryConnector() {
        GenericNodeConnector connector = null;
        try {
            Class clazz = Class.forName( LOCAL_DIRECTORY_CONNECTOR_CLASS );
            connector = (GenericNodeConnector) clazz.newInstance();
        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static GenericNodeConnector newRemoteConnector(String connectorString) {
        GenericNodeConnector connector = null;
        String[] connectorDetails = getConnectorDetails( connectorString );
        if ( connectorDetails[1].equals( "Mina" ) && connectorDetails[2].equals( "Node" ) ) {
            connector = newRemoteMinaNodeConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "Mina" ) && connectorDetails[2].equals( "Directory" ) ) {
            connector = newRemoteMinaDirectoryConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "Mina" ) && connectorDetails[2].equals( "Task" ) ) {
            connector = newRemoteMinaTaskConnector( connectorString );
        }

        if ( connectorDetails[1].equals( "HornetQ" ) && connectorDetails[2].equals( "Node" ) ) {
            connector = newRemoteHornetQNodeConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "HornetQ" ) && connectorDetails[2].equals( "Directory" ) ) {
            connector = newRemoteHornetQDirectoryConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "HornetQ" ) && connectorDetails[2].equals( "Task" ) ) {
            connector = newRemoteHornetQTaskConnector( connectorString );
        }

        return connector;
    }

    private static GenericNodeConnector newDistributedConnector(String connectorString) {
        GenericNodeConnector connector = null;
        String[] connectorDetails = getConnectorDetails( connectorString );
        if ( connectorDetails[1].equals( "Rio" ) && connectorDetails[2].equals( "Node" ) ) {
            connector = newDistributedRioNodeConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "Rio" ) && connectorDetails[2].equals( "Directory" ) ) {
            connector = newDistributedRioDirectoryConnector( connectorString );
        }
        if ( connectorDetails[1].equals( "Rio" ) && connectorDetails[2].equals( "Task" ) ) {
            throw new UnsupportedOperationException( "Not Implemented yet, we are working on it!" );
        }
        return connector;
    }

    private static GenericNodeConnector newRemoteMinaNodeConnector(String connectorString) {
        GenericNodeConnector connector = null;
        try {

            String[] connectorDetails = getConnectorDetails( connectorString );
            Class clazz = Class.forName( REMOTE_MINA_NODE_CONNECTOR_CLASS );
            Constructor constructor = clazz.getConstructor( String.class,
                                                            String.class,
                                                            Integer.class,
                                                            SystemEventListener.class );
            connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                        connectorDetails[3],
                                                                        Integer.valueOf( connectorDetails[4] ),
                                                                        SystemEventListenerFactory.getSystemEventListener() );

        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static GenericNodeConnector newRemoteMinaDirectoryConnector(String connectorString) {
        GenericNodeConnector connector = null;
        try {

            String[] connectorDetails = getConnectorDetails( connectorString );
            Class clazz = Class.forName( REMOTE_MINA_DIRECTORY_CONNECTOR_CLASS );
            Constructor constructor = clazz.getConstructor( String.class,
                                                            String.class,
                                                            Integer.class,
                                                            SystemEventListener.class );
            connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                        connectorDetails[3],
                                                                        Integer.valueOf( connectorDetails[4] ),
                                                                        SystemEventListenerFactory.getSystemEventListener() );

        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static GenericNodeConnector newRemoteMinaTaskConnector(String connectorString) {
        GenericNodeConnector connector = null;
        try {

            String[] connectorDetails = getConnectorDetails( connectorString );
            Class clazz = Class.forName( REMOTE_MINA_TASK_CONNECTOR_CLASS );
            Constructor constructor = clazz.getConstructor( String.class,
                                                            String.class,
                                                            Integer.class,
                                                            SystemEventListener.class );
            connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                        connectorDetails[3],
                                                                        Integer.valueOf( connectorDetails[4] ),
                                                                        SystemEventListenerFactory.getSystemEventListener() );

        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static String[] getConnectorDetails(String connectorString) {
        return connectorString.split( SEPARATOR );
    }

    private static GenericNodeConnector newRemoteHornetQNodeConnector(String connectorString) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    private static GenericNodeConnector newRemoteHornetQDirectoryConnector(String connectorString) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    private static GenericNodeConnector newRemoteHornetQTaskConnector(String connectorString) {
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    private static GenericNodeConnector newDistributedRioNodeConnector(String connectorString) {
        GenericNodeConnector connector = null;
        try {

            String[] connectorDetails = getConnectorDetails( connectorString );
            Class clazz = Class.forName( DISTRIBUTED_RIO_NODE_CONNECTOR_CLASS );
            if ( connectorDetails.length < 4 || !"".equals( connectorDetails[3] ) ) {
                Constructor constructor = clazz.getConstructor( String.class,
                                                                SystemEventListener.class,
                                                                String.class );
                connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                            SystemEventListenerFactory.getSystemEventListener(),
                                                                            connectorString );
            } else {
                Constructor constructor = clazz.getConstructor( String.class,
                                                                SystemEventListener.class );
                connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                            SystemEventListenerFactory.getSystemEventListener() );

            }

        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }

    private static GenericNodeConnector newDistributedRioDirectoryConnector(String connectorString) {
        GenericNodeConnector connector = null;
        try {

            String[] connectorDetails = getConnectorDetails( connectorString );
            Class clazz = Class.forName( DISTRIBUTED_RIO_DIRECTORY_CONNECTOR_CLASS );
            if ( connectorDetails.length > 3 && !"".equals( connectorDetails[3] ) ) {
                Constructor constructor = clazz.getConstructor( String.class,
                                                                SystemEventListener.class,
                                                                String.class );
                connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                            SystemEventListenerFactory.getSystemEventListener(),
                                                                            connectorString );
            } else {
                Constructor constructor = clazz.getConstructor( String.class,
                                                                SystemEventListener.class );
                connector = (GenericNodeConnector) constructor.newInstance( connectorString,
                                                                            SystemEventListenerFactory.getSystemEventListener() );

            }

        } catch ( InstantiationException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalAccessException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( IllegalArgumentException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( InvocationTargetException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( NoSuchMethodException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( SecurityException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        } catch ( ClassNotFoundException ex ) {
            Logger.getLogger( GenericConnectorFactory.class.getName() ).log( Level.SEVERE,
                                                                             null,
                                                                             ex );
        }
        return connector;
    }
}
