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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.builder.ResourceType;
import org.drools.fluent.VariableContext;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.fluent.test.impl.AssertThatImpl;
import org.drools.fluent.test.impl.ReflectiveMatcherFactory;
import org.drools.io.ResourceFactory;
import org.hamcrest.Matcher;
//import static org.hamsandwich.core.ReplayMatcher.on;
//import static org.hamsandwich.core.ReplayMatcher.replayMatcher;
//import org.hamsandwich.core.AdaptingMatcher;
//import org.hamsandwich.core.CannotAdaptException;
//import org.hamsandwich.core.HamSandwichFactory;
import org.junit.Ignore;
import org.junit.Test;

public class CompactFluentTest {

    //    public static class PersonMatchers {
    //
    //        @HamSandwichFactory
    //        public static Matcher<Person> name(Matcher<String>... nameMatchers) {
    //            return replayMatcher( on( Person.class ).getName(),
    //                                  nameMatchers );
    //        }
    //
    //        @HamSandwichFactory
    //        public static Matcher<Person> age(Matcher< ? super Integer>... ageMatchers) {
    //            return new AdaptingMatcher<Person, Integer>( ageMatchers ) {
    //
    //                public Integer get(Person in) throws CannotAdaptException {
    //                    return in.getAge();
    //                }
    //            };
    //        }
    //    }

    @Test
    public void testSimpleForAllAssertionsTypes() {
        SimulationFluent f = new DefaultSimulationFluent();

        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.junit.Assert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );
        //        imports.add( PersonMatchers.class.getName() + ".name" );
        //        imports.add( PersonMatchers.class.getName() + ".age" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str = "package org.drools.simulation.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "rule updateAge no-loop when  $p : Person() then modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";

        // @formatter:off        
        f.newKnowledgeBuilder()
            .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                   ResourceType.DRL )
            .end()
        .newStatefulKnowledgeSession()
            .insert( new Person( "yoda", 150 ) ).set( "y" )
            .fireAllRules()
            // show testing inside of ksession execution
            .test( "y.name == 'yoda'" )
            .test( "y.age == 160" )

            // test hamcrest
            .test( rf.assertThat( "y.name",
                                  matcher( "equalTo",
                                           "'yoda'" ) ) )
            .test( rf.assertThat( "y.name, equalTo('yoda')" ) )
            .test( rf.assertThat( "y.age",
                                  matcher( "equalTo",
                                           "160" ) ) )
            .test( rf.assertThat( "y.age, equalTo(160)" ) )

            // @ FIXME commented out until hamsandwich works in the build
            //             // test hamsandwich
            //             .test( rf.assertThat( "y", matcher( "allOf", matcher("name",  matcher( "equalTo", "'yoda'" ) ),
            //                                                          matcher("age",  matcher( "equalTo", "160" ) )
            //                                                )) )
            .end()

        // show complex testing after the ksession has finished
        .test( "y.name == 'yoda'" )
        .test( "y.age == 160" )

        // test hamcrest
        .test( rf.assertThat( "y.name",
                              matcher( "equalTo",
                                       "'yoda'" ) ) )
        .test( rf.assertThat( "y.age",
                              matcher( "equalTo",
                                       "160" ) ) )
        .runSimulation();

        // @ FIXME commented out until hamsandwich works in the build      
        //         // test hamsandwich
        //         .test( rf.assertThat( "y", matcher( "allOf", matcher("name",  matcher( "equalTo", "'yoda'" ) ),
        //                                                      matcher("age",  matcher( "equalTo", "160" ) )
        //                                            )) );                 
        // @formatter:on
    }

    @Test
    public void testAssertionsFail() {
        SimulationFluent f = new DefaultSimulationFluent();

        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.junit.Assert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str = "package org.drools.simulation.test\n" +
                     "import " + Person.class.getName() + "\n" +
                     "rule updateAge no-loop when  $p : Person() then modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";

        // @formatter:off        
        f.newKnowledgeBuilder()
            .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                   ResourceType.DRL )
            .end()
        .newStatefulKnowledgeSession()
            .insert( new Person( "yoda", 150 ) ).set( "y" )
            .fireAllRules()
            // show testing inside of ksession execution
            .test( "y.age == 110" );
        // @formatter:on    

        boolean fail = false;
        try {
            f.runSimulation();
        } catch ( AssertionError e ) {
            fail = true;
        }
        assertTrue( "Assertion should have failed",
                    fail );

        f = new DefaultSimulationFluent();
        // @formatter:off        
        f.newKnowledgeBuilder()
            .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                                    ResourceType.DRL )
            .end()
        .newStatefulKnowledgeSession()
             .insert( new Person( "yoda", 150 ) ).set( "y" )
             .fireAllRules()
             // show testing inside of ksession execution
             .test( rf.assertThat( "y.age", matcher( "equalTo", "120" ) ) )
             .test( rf.assertThat( "y.age, equalTo(120)" ) );        
        // @formatter:on    
        

        fail = false;
        try {
            f.runSimulation();
        } catch ( AssertionError e) {
            fail = true;
        } 
        assertTrue( "Assertion should have failed", fail ); 

        // @ FIXME commented out until hamsandwich works in the build
        //        f = new FluentCompactSimulationImpl();
        //        
        //        // @formatter:off        
        //        f.newStatefulKnowledgeSession()
        //             .getKnowledgeBase()
        //                 .addKnowledgePackages( ResourceFactory.newByteArrayResource( str.getBytes() ),
        //                                        ResourceType.DRL )
        //             .end()
        //             .insert( new Person( "yoda", 150 ) ).set( "y" )
        //             .fireAllRules()
        //             // show testing inside of ksession execution
        //         .test( rf.assertThat( "y", matcher( "allOf", matcher("name",  matcher( "equalTo", "'yoda'" ) ),
        //                                                      matcher("age",  matcher( "equalTo", "160" ) )
        //                                            )) );            
        //        // @formatter:on    
        //        
        //        try {
        //            f.runSimulation();
        //            fail( "age is 160, so should fail" );
        //        } catch ( AssertionError e) {
        //            System.out.println( e );
        //        }          
    }

    @Test @Ignore("Doing newKSession on the same path twice doesn't make the second one the active one") // TODO FIXME
    public void testMultipleKsessionsWithSteps() {
        SimulationFluent f = new DefaultSimulationFluent();

        VariableContext<Person> pc = f.<Person> getVariableContext();

        List<String> imports = new ArrayList<String>();
        imports.add( "org.junit.Assert.assertThat" );
        imports.add( "org.hamcrest.CoreMatchers.is" );
        imports.add( "org.hamcrest.CoreMatchers.equalTo" );
        imports.add( "org.hamcrest.CoreMatchers.allOf" );
        //        imports.add( PersonMatchers.class.getName() + ".name" );
        //        imports.add( PersonMatchers.class.getName() + ".age" );

        ReflectiveMatcherFactory rf = new ReflectiveMatcherFactory( imports );

        String str1 = "package org.drools.simulation.test\n" +
                      "import " + Person.class.getName() + "\n" +
                      "rule updateAge1 no-loop when  $p : Person() then modify( $p ) { setAge( $p.getAge() + 10 ) }; end\n";

        String str2 = "package org.drools.simulation.test\n" +
                      "import " + Person.class.getName() + "\n" +
                      "rule updateAge2 no-loop when  $p : Person() then modify( $p ) { setAge( $p.getAge() + 20 ) }; end\n";

        // @formatter:off
        f.newKnowledgeBuilder()
            .add( ResourceFactory.newByteArrayResource( str1.getBytes() ),
                                   ResourceType.DRL )
            .end()
        .newRelativeStep( 100 )
        .newStatefulKnowledgeSession()
            .insert( new Person( "yoda1",
                                 150 ) ).set( "y1" )
            .fireAllRules()
            .newRelativeStep( 200 )
            .insert( new Person( "darth1",
                                 70 ) ).set( "d1" )
            .fireAllRules()
            .end()
        .test( "y1.age == 160" )
        .test( "d1.age == 80" )
        .newRelativeStep( 100 )
        .newKnowledgeBuilder()
            .add( ResourceFactory.newByteArrayResource( str2.getBytes() ),
                                   ResourceType.DRL )
            .end()
        .newStatefulKnowledgeSession()
            .insert( new Person( "yoda2",
                                 150 ) ).set( "y2" )
            .fireAllRules()
            .newRelativeStep( 200 )
            .insert( new Person( "darth2",
                                 70 ) ).set( "d2" )
            .fireAllRules()
            .end()
        .test( "y2.age == 170" )
        .test( "d2.age == 90" )
        .test( "y1 == null" )
        .test( "d1 == null" )
        .runSimulation();
        // @formatter:on
    }

}
