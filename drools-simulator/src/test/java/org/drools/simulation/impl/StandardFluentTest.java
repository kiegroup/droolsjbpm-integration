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

package org.drools.simulation.impl;

import static org.drools.fluent.test.impl.ReflectiveMatcherFactory.matcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.fluent.VariableContext;
import org.drools.fluent.compact.FluentCompactSimulation;
import org.drools.fluent.compact.imp.FluentCompactSimulationImpl;
import org.drools.fluent.standard.FluentStandardSimulation;
import org.drools.fluent.standard.imp.FluentStandardSimulationImpl;
import org.drools.fluent.test.impl.ReflectiveMatcherFactory;
import org.drools.io.ResourceFactory;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class StandardFluentTest {

    @Test
    public void testUsingImplicit() {
        FluentStandardSimulation f = new FluentStandardSimulationImpl();
        
        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.hamcrest.MatcherAssert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str = "package org.drools.simulation.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "global java.util.List list\n" +
                     "rule setTime when then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n end\n " +
                     "rule updateAge no-loop when  $p : Person() then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";        
        
        List list = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
            .newStep( 0 )
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                          ResourceType.DRL )
                    .end(World.ROOT, KnowledgeBuilder.class.getName() )
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, KnowledgeBase.class.getName() )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list ).set( "list" )
                    .fireAllRules()
                    .end()
                .end()
        .newPath( "path1" )
            .newStep( 1000 )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list ).set( "list" )
                    .insert( new Person( "yoda", 150 ) ).set( "y" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "y.name == 'yoda'" )
                    .test( "y.age == 160" )
                    .test( "list[list.size()-1] - list[0] == 1000" )
                    .end()
                .end()
            .newStep( 2000 )
                .getStatefulKnowledgeSession()
                    .insert( new Person( "darth", 110 ) ).set( "d" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "d.name == 'darth'" )
                    .test( "d.age == 120" )
                    .test( "y.name == 'yoda'" )
                    .test( "y.age == 160" )
                    .test( "list[list.size()-1] - list[0] == 2000" )
                    .end()
                .end()
        .newPath(  "path2" )
             .newStep( 1500 )
                 .newStatefulKnowledgeSession()
                     .setGlobal( "list", list ).set( "list" )
                     .insert( new Person( "bobba", 75 ) ).set( "b" )
                     .fireAllRules()
                      // show testing inside of ksession execution
                     .test( "y == null" )
                     .test( "b.name == 'bobba'" )
                     .test( "b.age == 85" )
                     .test( "list[list.size()-1] - list[0] == 1500" )
                     .end()
                 .end()
        .getPath(  "path1" )
            .newStep( 1300 )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list )
                    .insert( new Person( "luke", 35 ) ).set( "b" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "y.name == 'yoda'" )
                    .test( "y.age == 160" )
                    .test( "b.name == 'luke'" )
                    .test( "b.age == 45" )
                    .test( "list[list.size()-1] - list[0] == 1300" )
                    .end()
                .end()
            .end();
        // @formatter:on

        runSimulation( f );
    }
    
    @Test
    public void testUsingExplicit() {
        FluentStandardSimulation f = new FluentStandardSimulationImpl();        
        
        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.hamcrest.MatcherAssert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str1 = "package org.drools.simulation.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "global java.util.List list\n" +
                     "rule setTime when then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n end\n " +
                     "rule updateAge no-loop when  $p : Person() then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n modify( $p ) { setAge( $p.getAge() + 20 ) }; end\n";        

        String str2 = "package org.drools.simulation.test\n" +
                "import " + Person.class.getName() + "\n" +
                "global java.util.List list\n" +
                "rule setTime when then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n end\n " +
                "rule updateAge no-loop when  $p : Person() then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";        
        
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
            .newStep( 0 )
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newByteArrayResource( str1.getBytes() ),
                          ResourceType.DRL )
                    .end(World.ROOT, KnowledgeBuilder.class.getName() )
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT,"kb1" )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list1 ).set( "list" )
                    .fireAllRules()
                    .end()
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newByteArrayResource( str2.getBytes() ),
                          ResourceType.DRL )
                    .end(World.ROOT, KnowledgeBuilder.class.getName() )
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, "kb2" )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list2 ).set( "list" )
                    .fireAllRules()
                    .end( "kb1" )
                .end()
        .newPath( "path1" )
            .newStep( 1000 )
                .getKnowledgeBase( "kb1" )
                    .end()
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list1 ).set( "list" )
                    .insert( new Person( "yoda", 150 ) ).set( "y" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "y.name == 'yoda'" )
                    .test( "y.age == 170" )
                    .test( "list[list.size()-1] - list[0] == 1000" )
                    .end()
                .end()
            .newStep( 2000 )
                .getKnowledgeBase( "kb2" )
                    .end()
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list1 ).set( "list" )
                    .insert( new Person( "yoda", 150 ) ).set( "y" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "y.name == 'yoda'" )
                    .test( "System.out.println( y.age ); return true")
                    .test( "list[list.size()-1] - list[0] == 2000" )
                    .end()
                .end()
            .end();
        // @formatter:on
            
        runSimulation( f );
    }    
    
    
    @Test
    public void testUsingDifferentPosAsserts() {
        FluentStandardSimulation f = new FluentStandardSimulationImpl();        
        
        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.hamcrest.MatcherAssert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str = "package org.drools.simulation.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "global java.util.List list\n" +
                     "rule setTime when then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n end\n " +
                     "rule updateAge no-loop when  $p : Person() then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";        
        
        List list = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
            .newStep( 0 )
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                          ResourceType.DRL )
                    .end(World.ROOT, KnowledgeBuilder.class.getName() )
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, KnowledgeBase.class.getName() )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list ).set( "list" )
                    .fireAllRules()
                    .end()
                .end()
        .newPath( "path1" )
            .newStep( 1000 )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list ).set( "list" )
                    .insert( new Person( "yoda", 150 ) ).set( "y" )
                    .fireAllRules()
                     // show testing inside of ksession execution
                    .test( "y.name == 'yoda'" )
                    .test( "y.age == 160" )
                    .test( "list[list.size()-1] - list[0] == 1000" )
                    .end()
                .test( "y.name == 'yoda'" )
                .end()
            .test( "y.name == 'yoda'" )
            .end();
        // @formatter:on

        runSimulation( f );
    }
    
    private void runSimulation(FluentStandardSimulation f) {
        SimulationImpl sim = (SimulationImpl) ((FluentStandardSimulationImpl) f).getSimulation();
    
        Simulator simulator = new Simulator( sim,
                                             new Date().getTime() );
        simulator.run();
    }       

}
