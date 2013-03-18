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

package org.drools.simulation.fluent.simulation;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.simulation.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.simulation.fluent.test.impl.ReflectiveMatcherFactory;
import org.drools.simulation.impl.Person;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.internal.fluent.VariableContext;

public class SimulationFluentTest extends SimulateTestBase {

    @Test
    public void testUsingImplicit() throws IOException {
        SimulationFluent f = new DefaultSimulationFluent();
        
        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.hamcrest.MatcherAssert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str = "package org.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "global java.util.List list\n" +
                     "rule setTime when then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n end\n " +
                     "rule updateAge no-loop when  $p : Person() then list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";
        
        ReleaseId releaseId = createKJar( "org.test.KBase1", str );
        
        List list = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
        .newStep( 0 )
        .newKieSession( releaseId, "org.test.KBase1.KSession1" )
            .setGlobal( "list", list ).set( "list" )
            .fireAllRules()
            .end()
        .newPath( "path1" )
        .newStep( 1000 )
        .newKieSession( releaseId, "org.test.KBase1.KSession1")
            .setGlobal( "list", list ).set( "list" )
            .insert( new Person( "yoda", 150 ) ).set( "y" )
            .fireAllRules()
             // show testing inside of ksession execution
            .test( "y.name == 'yoda'" )
            .test( "y.age == 160" )
            .test( "list[list.size()-1] - list[0] == 1000" )
            .end()
        .newStep( 2000 )
        .getKieSession()
            .insert( new Person( "darth", 110 ) ).set( "d" )
            .fireAllRules()
             // show testing inside of ksession execution
            .test( "d.name == 'darth'" )
            .test( "d.age == 120" )
            .test( "y.name == 'yoda'" )
            .test( "y.age == 160" )
            .test( "list[list.size()-1] - list[0] == 2000" )
            .end()
        .newPath(  "path2" )
        .newStep( 1500 )
        .newKieSession( releaseId, "org.test.KBase1.KSession1")
            .setGlobal( "list", list ).set( "list" )
            .insert( new Person( "bobba", 75 ) ).set( "b" )
            .fireAllRules()
             // show testing inside of ksession execution
            .test( "y == null" )
            .test( "b.name == 'bobba'" )
            .test( "b.age == 85" )
            .test( "list[list.size()-1] - list[0] == 1500" )
            .end()
        .getPath(  "path1" )
        .newStep( 1300 )
        .newKieSession( releaseId, "org.test.KBase1.KSession1")
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
        .runSimulation();
        // @formatter:on
    }
    
    @Test
    public void testUsingExplicit() throws IOException {
        SimulationFluent f = new DefaultSimulationFluent();
        
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
        
        ReleaseId releaseId = createKJar( "org.test.KBase1", str1,
                                          "org.test.KBase2", str2 );
        
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
        .newStep(0)
        .newKieSession( releaseId, "org.test.KBase1.KSession1" )
            .setGlobal("list", list1).set("list")
            .fireAllRules()
            .end()
        .newKieSession( releaseId, "org.test.KBase2.KSession1" )
            .setGlobal("list", list2).set("list")
            .fireAllRules()
            .end("ks2.1")
        .newPath("path1")
        .newStep(1000)
        .newKieSession( releaseId, "org.test.KBase1.KSession1" )
            .setGlobal("list", list1).set("list")
            .insert(new Person("yoda", 150)).set("y")
            .fireAllRules()
             // show testing inside of ksession execution
            .test("y.name == 'yoda'")
            .test("y.age == 170")
            .test( "list[list.size()-1] - list[0] == 1000" )
            .end()
        .newStep(2000)
        .newKieSession( releaseId, "org.test.KBase2.KSession1" )
            .setGlobal("list", list1).set("list")
            .insert(new Person("yoda", 150)).set("y")
            .fireAllRules()
             // show testing inside of ksession execution
            .test("y.name == 'yoda'")
            .test("System.out.println( y.age ); return true")
            .test( "list[list.size()-1] - list[0] == 2000" )
            .end()
        .runSimulation();
        // @formatter:on
    }
    
    
    @Test
    public void testUsingDifferentPosAsserts() throws IOException {
        SimulationFluent f = new DefaultSimulationFluent();
        
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
        
        ReleaseId releaseId = createKJar( "org.test.KBase1", str );

        List list = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
        .newStep(0)
        .newKieSession( releaseId, "org.test.KBase1.KSession1" )
            .setGlobal("list", list).set("list")
            .fireAllRules()
            .end()
        .newPath("path1")
        .newStep(1000)
        .newKieSession( releaseId, "org.test.KBase1.KSession1" )
            .setGlobal("list", list).set("list")
            .insert(new Person("yoda", 150)).set("y")
            .fireAllRules()
             // show testing inside of ksession execution
            .test("y.name == 'yoda'")
            .test("y.age == 160")
            .test( "list[list.size()-1] - list[0] == 1000" )
            .end()
        .test("y.name == 'yoda'")
        .test("y.name == 'yoda'")
        .runSimulation();
        // @formatter:on
    }

}
