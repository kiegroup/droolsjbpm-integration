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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.grid.ConnectorType;
import org.drools.grid.services.ExecutionEnvironment;

/**
 *
 * @author salaboy
 */
public class ExecutionEnvByPrioritySelectionStrategy
    implements
    ExecutionEnvironmentSelectionStrategy {

    private List<ExecutionEnvironment> executionEnvironments;

    public ExecutionEnvironment getBestExecutionEnvironment() {
        Collections.sort( this.executionEnvironments,
                          new Comparator<ExecutionEnvironment>() {

                              private Map<ConnectorType, Integer> priorities = new HashMap<ConnectorType, Integer>() {

                                                                                 {
                                                                                     put( ConnectorType.LOCAL,
                                                                                          1 );
                                                                                     put( ConnectorType.DISTRIBUTED,
                                                                                          2 );
                                                                                     // put("HornetQEnvironmentProvider", 3);
                                                                                     put( ConnectorType.REMOTE,
                                                                                          4 );
                                                                                 }
                                                                             };

                              public int compare(ExecutionEnvironment o1,
                                                 ExecutionEnvironment o2) {
                                  return this.priorities.get( o1.getConnector().getConnectorType() ).compareTo( this.priorities.get( o2.getConnector().getConnectorType() ) );
                              }
                          } );

        return this.executionEnvironments.get( 0 );
    }

    public void setExecutionEnvironments(Map<String, ExecutionEnvironment> executionEnvironments) {
        
        List<ExecutionEnvironment> eeList = new ArrayList<ExecutionEnvironment>();
        for ( ExecutionEnvironment ee : executionEnvironments.values() ) {
            eeList.add( ee );
        }
        this.executionEnvironments = eeList;

    }

    public ExecutionEnvironment getBestExecutionEnvironment(Map<String, ExecutionEnvironment> executionEnvironments) {
        setExecutionEnvironments( executionEnvironments );
        return getBestExecutionEnvironment();
    }

}
