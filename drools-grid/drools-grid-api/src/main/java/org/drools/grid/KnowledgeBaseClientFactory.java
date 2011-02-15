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

public class KnowledgeBaseClientFactory {
    public static KnowledgeBase newKnowledgeBaseClient(String connectorString) {
        KnowledgeBase client =  null;
        String[] connectorDetails = connectorString.split( ":" );
        String connectorType = connectorDetails[0];

        if ( connectorType.equals( "Remote" ) ) {
            String provider = connectorDetails[1];
            if(provider.equals("Mina")){
                //@TODO use reflection to create a KnowledgeBaseRemoteClient
            }
        }
        if ( connectorType.equals( "Distributed" ) ) {
            String provider = connectorDetails[1];
            if(provider.equals("Rio")){
                //@TODO use reflection to create a KnowledgeBaseGridClient
            }
        }
        
        
        return client;

    }
}
