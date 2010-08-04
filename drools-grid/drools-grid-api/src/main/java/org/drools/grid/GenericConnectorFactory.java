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
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;

/**
 *
 * @author salaboy
 */
public class GenericConnectorFactory {

    public static GenericNodeConnector newNodeConnector(String connectorString) {
        
        if(connectorString == null || connectorString.equals("")){
          return null;
        }
        String[] connectorDetails = connectorString.split(":");

        String connectorType = connectorDetails[0];


        if (connectorType.equals("Distributed")) {
            String connectorBehavior = connectorDetails[1];
            String connectorId = connectorDetails[2];
            GenericNodeConnector connector = null;



            try {
                Class clazzLocator = Class.forName("org.drools.grid.distributed.util.RioResourceLocator");
                Method locate = clazzLocator.getMethod("locateResource", String.class);
                ExecutionNodeService execNode = (ExecutionNodeService)locate.invoke(clazzLocator, connectorString);

                Class clazz = Class.forName("org.drools.grid.distributed.DistributedRioNodeConnector");
                Constructor constructor = clazz.getConstructor(String.class, SystemEventListener.class, ExecutionNodeService.class);
                connector = (GenericNodeConnector) constructor.newInstance("node1", SystemEventListenerFactory.getSystemEventListener(),
                                            execNode);

              
            } catch (InstantiationException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }

            return connector;
        }


        if (connectorType.equals("Mina")) {
            String connectorName = connectorDetails[1];
            String connectorAddress = connectorDetails[2];
            String connectorPort = connectorDetails[3];

            GenericNodeConnector connector = null;
            try {
                Class clazz = Class.forName("org.drools.grid.remote.mina.RemoteMinaNodeConnector");
                Constructor constructor = clazz.getConstructor(String.class, String.class, Integer.class, SystemEventListener.class);
                connector = (GenericNodeConnector) constructor.newInstance(connectorName, connectorAddress,
                        Integer.valueOf(connectorPort), SystemEventListenerFactory.getSystemEventListener());

                //return new MinaRemoteNodeConnector(connectorName, connectorAddress,
                //         Integer.valueOf(connectorPort), SystemEventListenerFactory.getSystemEventListener());
            } catch (InstantiationException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }

            return connector;
        }




        if (connectorType.equals("Local")) {
            GenericNodeConnector connector = null;
            try {
                Class clazz = Class.forName("org.drools.grid.local.LocalNodeConnector");
                connector = (GenericNodeConnector) clazz.newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }

            return connector;

            //return new LocalNodeConnector();
        }

        return null;
    }

    public static GenericNodeConnector newDirectoryConnector(String connectorString) {
        String[] connectorDetails = connectorString.split(":");
        String connectorType = connectorDetails[0];



        if (connectorType.equals("Mina")) {

            String connectorName = connectorDetails[1];
            String connectorAddress = connectorDetails[2];
            String connectorPort = connectorDetails[3];
            GenericNodeConnector connector = null;
            try {
                Class clazz = Class.forName("org.drools.grid.remote.directory.RemoteMinaDirectoryConnector");
                Constructor constructor = clazz.getConstructor(String.class, String.class, Integer.class, SystemEventListener.class);
                connector = (GenericNodeConnector) constructor.newInstance(connectorName, connectorAddress,
                        Integer.valueOf(connectorPort), SystemEventListenerFactory.getSystemEventListener());

                //return new MinaRemoteNodeConnector(connectorName, connectorAddress,
                //         Integer.valueOf(connectorPort), SystemEventListenerFactory.getSystemEventListener());
            } catch (InstantiationException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }

            return connector;
        }

        if (connectorType.equals("Local")) {
            GenericNodeConnector connector = null;
            try {
                Class clazz = Class.forName("org.drools.grid.local.LocalDirectoryConnector");
                Class clazzDirectoryNodeServiceImpl = Class.forName("org.drools.grid.local.DirectoryNodeLocalImpl");
                Constructor constructor = clazz.getConstructor(DirectoryNodeService.class);
                connector = (GenericNodeConnector) constructor.newInstance(
                        (DirectoryNodeService) clazzDirectoryNodeServiceImpl.newInstance());

            } catch (InstantiationException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(GenericConnectorFactory.class.getName()).log(Level.SEVERE, null, ex);
            }

            return connector;
            
        }

        return null;
    }

    public static GenericNodeConnector newTaskConnector(String connectorString) {
        String[] connectorDetails = connectorString.split(":");
        String connectorType = connectorDetails[0];
        String connectorName = connectorDetails[1];
        String connectorAddress = connectorDetails[2];
        String connectorPort = connectorDetails[3];





        return null;
    }


}
