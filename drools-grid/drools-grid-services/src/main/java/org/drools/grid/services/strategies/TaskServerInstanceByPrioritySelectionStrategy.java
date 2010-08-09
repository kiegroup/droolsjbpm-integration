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

package org.drools.grid.services.strategies;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.grid.ConnectorException;

import org.drools.grid.ConnectorType;
import org.drools.grid.services.DirectoryInstance;
import org.drools.grid.services.TaskServerInstance;


/**
 *
 * @author salaboy
 */
public class TaskServerInstanceByPrioritySelectionStrategy implements TaskServerInstanceSelectionStrategy{

    private List<TaskServerInstance> taskServers;

    public TaskServerInstance getBestTaskServerInstance() {



        Collections.sort(taskServers, new Comparator<TaskServerInstance>() {

            private Map<ConnectorType , Integer> priorities
                        = new HashMap<ConnectorType, Integer>() {
                {
                   // put(ConnectorType.LOCAL, 1);
                   // put("RioEnvironmentProvider", 2);
                   // put("HornetQEnvironmentProvider", 3);
                   put(ConnectorType.REMOTE, 4);
                }
            };

            public int compare(TaskServerInstance o1, TaskServerInstance o2) {
                    return priorities.get(o1.getConnector().getConnectorType()).compareTo(
                            priorities.get(o2.getConnector().getConnectorType()));

            }
        });



        return taskServers.get(0);
    }

    public void setTaskServerInstances(Map<String, TaskServerInstance> taskServerInstances) {
        List<TaskServerInstance> serverList = new ArrayList<TaskServerInstance>();
        for(TaskServerInstance taskServer : taskServerInstances.values()){
            
                serverList.add(taskServer);
            
        }
        this.taskServers = serverList;
    }

    public TaskServerInstance getBestTaskServerInstance(Map<String, TaskServerInstance> taskServerInstances) {
        setTaskServerInstances(taskServerInstances);
        return getBestTaskServerInstance();
    }

   

}
