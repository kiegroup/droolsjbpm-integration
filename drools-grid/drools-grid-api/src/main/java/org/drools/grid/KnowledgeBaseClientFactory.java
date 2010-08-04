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

import org.drools.KnowledgeBase;

/**
 *
 * @author salaboy
 */
public class KnowledgeBaseClientFactory {
    public static KnowledgeBase newKnowledgeBaseClient(String connectorString){
        
        String[] connectorDetails = connectorString.split(":");
        String connectorType = connectorDetails[0];

        if (connectorType.equals("Remote")) {
           // I need to use reflection to create this remote client, that contain a node conector that can be create using
            //the node connector factorynew KnowledgeBaseRemoteClient();

        }
        return null;

    }
}
