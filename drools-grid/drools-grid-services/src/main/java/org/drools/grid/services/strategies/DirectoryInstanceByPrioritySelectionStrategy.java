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
import org.drools.grid.services.DirectoryInstance;

public class DirectoryInstanceByPrioritySelectionStrategy
    implements
    DirectoryInstanceSelectionStrategy {

    private List<DirectoryInstance> directories;

    public DirectoryInstance getBestDirectoryInstance() {

        Collections.sort( this.directories,
                          new Comparator<DirectoryInstance>() {

                              private Map<ConnectorType, Integer> priorities
                                                                             = new HashMap<ConnectorType, Integer>() {
                                                                                 {
                                                                                     put( ConnectorType.LOCAL,
                                                                                          1 );
                                                                                     // put("RioEnvironmentProvider", 2);
                                                                                     // put("HornetQEnvironmentProvider", 3);
                                                                                     put( ConnectorType.REMOTE,
                                                                                          4 );
                                                                                 }
                                                                             };

                              public int compare(DirectoryInstance o1,
                                                 DirectoryInstance o2) {
                                  return this.priorities.get( o1.getConnector().getConnectorType() )
                                          .compareTo( this.priorities.get( o2.getConnector().getConnectorType() ) );
                              }
                          } );

        return this.directories.get( 0 );
    }

    public void setDirectoryInstances(Map<String, DirectoryInstance> directoryInstances) {
        List<DirectoryInstance> dirList = new ArrayList<DirectoryInstance>();
        for ( DirectoryInstance directory : directoryInstances.values() ) {

            dirList.add( directory );

        }
        this.directories = dirList;
    }

    public DirectoryInstance getBestDirectoryInstance(Map<String, DirectoryInstance> directoryInstances) {
        setDirectoryInstances( directoryInstances );
        return getBestDirectoryInstance();
    }

}
