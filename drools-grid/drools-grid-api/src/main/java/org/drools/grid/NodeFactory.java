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

public class NodeFactory {
    public static ExecutionNode newExecutionNode(NodeConnectionType type) {
        type.init();
        ExecutionNode node = new ExecutionNode();
        for ( Class serviceClass : type.getServicesKeys() ) {
            node.set( serviceClass,
                      type.getServiceImpl( serviceClass ) );
        }
        return node;
    }

    public static DirectoryNode newDirectoryNode(NodeConnectionType type) {
        type.init();
        DirectoryNode node = new DirectoryNode();
        for ( Class serviceClass : type.getServicesKeys() ) {
            node.set( serviceClass,
                      type.getServiceImpl( serviceClass ) );
        }
        return node;
    }

    public static HumanTaskNode newHumanTaskNode(NodeConnectionType type) {
        type.init();
        HumanTaskNode node = new HumanTaskNode();
        for ( Class serviceClass : type.getServicesKeys() ) {
            node.set( serviceClass,
                      type.getServiceImpl( serviceClass ) );
        }
        return node;
    }

}
