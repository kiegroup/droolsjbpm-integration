/*
 * Copyright 2012 JBoss Inc
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
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.test.impl.ReflectiveMatcherFactory;
import org.drools.io.ResourceFactory;
import org.junit.Test;

public class StandardjBPM5FluentTest {

    @Test
    public void testUsingImplicit() {
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
                "rule setTime\n" +
                "  when\n" +
                "  then\n" +
                "    list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n" +
                "end\n" +
                "rule updateAge no-loop\n" +
                "  when\n" +
                "    $p : Person()\n" +
                "  then\n" +
                "    list.add( kcontext.getKnowledgeRuntime().getSessionClock().getCurrentTime() );\n" +
                "    modify( $p ) {\n" +
                "      setAge( $p.getAge() + 10 )\n" +
                "    };\n" +
                "end\n";
        String strProcess = "<definitions id='Definition' "
                + "targetNamespace='http://www.jboss.org/drools' "
                + "typeLanguage='http://www.java.com/javaTypes' "
                + "expressionLanguage='http://www.mvel.org/2.0' "
                + "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd' "
                + "xmlns:g='http://www.jboss.org/drools/flow/gpd' "
                + "xmlns:bpmndi='http://www.omg.org/spec/BPMN/20100524/DI' "
                + "xmlns:dc='http://www.omg.org/spec/DD/20100524/DC' "
                + "xmlns:di='http://www.omg.org/spec/DD/20100524/DI' "
                + "xmlns:tns='http://www.jboss.org/drools'>"
                            + " <process id='DummyProcess' name='Sample Process'>"
                                    + "<startEvent id='_1' name='StartProcess' />"
                                    + "<scriptTask id='_2' name='Script 1' >"
                                       + "<script>System.out.println('Script 1 - Executing .. ');</script> "
                                    + "</scriptTask>"
                                    + "<scriptTask id='_3' name='Script 2' >"
                                        + "<script>System.out.println('Script 2 - Executing .. ');</script>"
                                    + "</scriptTask>"
                                    + "<endEvent id='_4' name='End' >"
                                        + "<terminateEventDefinition/>"
                                    + "</endEvent>"
                                    + "<sequenceFlow id='_1-_2' sourceRef='_1' targetRef='_2' />"
                                    + "<sequenceFlow id='_2-_3' sourceRef='_2' targetRef='_3' />"
                                    + "<sequenceFlow id='_3-_4' sourceRef='_3' targetRef='_4' />"
                            + "</process>"
                + "</definitions>";
        
        List list = new ArrayList();
        
        VariableContext<?> vc = f.getVariableContext();
        // @formatter:off          
        f.newPath("init")
            .newStep( 0 )
                .newKnowledgeBuilder()
                    .add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                          ResourceType.DRL )
                    .add( ResourceFactory.newByteArrayResource( strProcess.getBytes() ),
                          ResourceType.BPMN2 )
                    .end(World.ROOT, KnowledgeBuilder.class.getName() )
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(World.ROOT, KnowledgeBase.class.getName() )
                .newStatefulKnowledgeSession()
                    .setGlobal( "list", list ).set( "list" )
                    .startProcess("DummyProcess")
                    .fireAllRules()
                    .end()
                .runSimulation();
        // @formatter:on
    }

}
