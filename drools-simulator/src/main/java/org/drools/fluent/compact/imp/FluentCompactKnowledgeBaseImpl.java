/*
 * Copyright 2011 JBoss Inc
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

package org.drools.fluent.compact.imp;

import org.drools.KnowledgeBaseConfiguration;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.KnowledgeContextResolveFromContextCommand;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.compact.FluentCompactKnowledgeBase;
import org.drools.fluent.compact.FluentCompactStatefulKnowledgeSession;
import org.drools.fluent.compact.InternalSimulation;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;

public class FluentCompactKnowledgeBaseImpl extends AbstractFluentTest<FluentCompactKnowledgeBase>
    implements
    FluentCompactKnowledgeBase {

    private FluentCompactStatefulKnowledgeSession ksession;

    public FluentCompactKnowledgeBaseImpl(InternalSimulation sim,
                                          FluentCompactStatefulKnowledgeSession ksession) {
        super();
        setSim( sim );
        this.ksession = ksession;
    }
    
    public FluentCompactKnowledgeBase setKnowledgeBuilderconfiguration(KnowledgeBaseConfiguration kbaseConf) {
        // TODO Auto-generated method stub
        return null;
    }    
    
    public FluentCompactKnowledgeBase addKnowledgePackages() {
        getSim().addCommand(  new KnowledgeBaseAddKnowledgePackagesCommand() );
        return this;
    }    

    public FluentCompactKnowledgeBase addKnowledgePackages(Resource resource,
                                                           ResourceType type) {
        getSim().addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        return this;
    }

    public FluentCompactKnowledgeBase addKnowledgePackages(Resource resource,
                                                           ResourceType type,
                                                           ResourceConfiguration configuration) {
        getSim().addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );

        return this;
    }

    public FluentCompactStatefulKnowledgeSession end() {
        return ksession;
    }

}
