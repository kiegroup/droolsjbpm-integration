/*
 * Copyright 2010 JBoss Inc
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
 */

package org.drools.grid.remote;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;

public class NewKnowledgeBuilderRemoteCommand
    implements
    GenericCommand<KnowledgeBuilder> {

    private String kbuilderConfId;

    public NewKnowledgeBuilderRemoteCommand(String kbuilderConfId) {
        this.kbuilderConfId = kbuilderConfId;
    }

    public KnowledgeBuilder execute(Context context) {
        KnowledgeBuilder kbuilder = null;
        KnowledgeBuilderConfiguration kconf = 
                        (KnowledgeBuilderConfiguration) context
                                                        .getContextManager()
                                                        .getDefaultContext().get(kbuilderConfId);
        if ( kconf == null ) {
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        } else {
            
            kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder( kconf );
            
        }
        
        return kbuilder;
    }

}
