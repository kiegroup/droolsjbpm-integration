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

package org.drools.fluent.standard.imp;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.command.GetVariableCommand;
import org.drools.command.KnowledgeBaseAddKnowledgePackagesCommand;
import org.drools.command.NewStatefulKnowledgeSessionCommand;
import org.drools.command.SetVariableCommandFromLastReturn;
import org.drools.command.World;
import org.drools.command.builder.KnowledgeBuilderAddCommand;
import org.drools.fluent.InternalSimulation;
import org.drools.fluent.standard.FluentStandardKnowledgeBase;
import org.drools.fluent.standard.FluentStandardStatefulKnowledgeSession;
import org.drools.fluent.standard.FluentStandardStep;
import org.drools.fluent.test.impl.AbstractFluentTest;
import org.drools.io.Resource;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

public class FluentStandardKnowledgeBaseImpl extends AbstractFluentTest<FluentStandardKnowledgeBase>
        implements FluentStandardKnowledgeBase {

    private FluentStandardStepImpl step;
    
    public FluentStandardKnowledgeBaseImpl(InternalSimulation sim,
                                           FluentStandardStepImpl step) {
        super();
        setSim( sim );
        this.step = step;
    }
    
    public FluentStandardKnowledgeBase addKnowledgePackages() {
        getSim().addCommand(  new KnowledgeBaseAddKnowledgePackagesCommand() );
        return this;
    }    
    
    
    public FluentStandardKnowledgeBase addKnowledgePackages(Resource resource,
                                                            ResourceType type) {
        getSim().addCommand(  new KnowledgeBuilderAddCommand( resource,
                                                              type,
                                                              null ) );
        return this;
    }

    public FluentStandardKnowledgeBase addKnowledgePackages(Resource resource,
                                                            ResourceType type,
                                                            ResourceConfiguration configuration) {
        getSim().addCommand( new KnowledgeBuilderAddCommand( resource,
                                                             type,
                                                             configuration ) );
        
        return this;
    }

    public FluentStandardStep end(String context, String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( context, name ) );
        return step;
    }
    
    public FluentStandardStep end(String name) {
        getSim().addCommand( new GetVariableCommand( KnowledgeBase.class.getName() ) );
        getSim().addCommand( new SetVariableCommandFromLastReturn( name ) );
        return step;
    }

    public FluentStandardStep end() {
        return step;
    }

    public FluentStandardStatefulKnowledgeSession newStatefulKnowledgeSession() {
        KnowledgeSessionConfiguration ksessionConf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksessionConf.setOption( ClockTypeOption.get( "pseudo" ) );
        getSim().addCommand( new NewStatefulKnowledgeSessionCommand( ksessionConf ) );             
        getSim().addCommand( new SetVariableCommandFromLastReturn( World.ROOT, StatefulKnowledgeSession.class.getName() ));

        return new FluentStandardStatefulKnowledgeSessionImpl(getSim(), step);
    }

}
