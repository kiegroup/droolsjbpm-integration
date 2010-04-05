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

package org.drools.grid.strategies;


import org.drools.grid.NodeSelectionStrategy;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.generic.GenericNodeConnector;


/**
 *
 * @author salaboy
 */
public class StaticIncrementalSelectionStrategy implements NodeSelectionStrategy{
    public static int counter = 0;
    private GenericConnection connection;
    public StaticIncrementalSelectionStrategy(GenericConnection connection) {
        this.connection = connection;
    }


    @Override
    public GenericNodeConnector getBestNode() {
        System.out.println("!!!!!GET BEST NODE = "+counter);
        GenericNodeConnector service = connection.getNodeConnectors().get(counter);
        StaticIncrementalSelectionStrategy.counter = counter +1;
        return service;
    }

    public void setConnection(GenericConnection connection) {
        this.connection = connection;
    }
    

}
