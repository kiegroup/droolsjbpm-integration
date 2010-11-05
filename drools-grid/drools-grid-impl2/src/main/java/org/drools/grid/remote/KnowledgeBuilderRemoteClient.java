/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */

package org.drools.grid.remote;

import java.util.Collection;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.grid.io.ConversationManager;
import org.drools.io.Resource;

/**
 *
 * @author salaboy
 */
public class KnowledgeBuilderRemoteClient implements KnowledgeBuilder{

    private String localId;
    private ConversationManager cm;
    
    public KnowledgeBuilderRemoteClient(String localId, ConversationManager cm) {
        this.localId = localId;
        this.cm = cm;
    }

    
    
    public void add(Resource resource, ResourceType type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(Resource resource, ResourceType type, ResourceConfiguration configuration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<KnowledgePackage> getKnowledgePackages() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBase newKnowledgeBase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasErrors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public KnowledgeBuilderErrors getErrors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
