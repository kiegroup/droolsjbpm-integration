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

package org.drools.container.spring;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

//import org.kie.grid.ConnectionFactoryService;
//import org.kie.grid.Grid;
//import org.kie.grid.GridConnection;
//import org.kie.grid.GridNode;
//import org.kie.grid.GridServiceDescription;
//import org.kie.grid.SocketService;
//import org.kie.grid.service.directory.WhitePages;
//import org.kie.grid.service.directory.impl.JpaWhitePages;
//import org.kie.grid.service.directory.impl.WhitePagesClient;
//import org.kie.grid.service.directory.impl.WhitePagesImpl;
import org.drools.io.impl.ByteArrayResource;
import org.junit.Assert;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactoryService;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderError;
import org.kie.builder.KnowledgeBuilderErrors;
import org.kie.builder.KnowledgeBuilderFactoryService;
import org.kie.builder.ResourceType;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDroolsGridTest {
    
    @Test
    public void testDummy() {
        
    }

//    @Test
//    public void test1() {
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.kie.grid" );
//        WhitePages wp = new JpaWhitePages( emf );
//
//        wp.create( "s1" );
//        wp.create( "s2" );
//        wp.create( "s3" );
//    }
//
//    @Test
//    public void testGrid() throws Exception {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/grid.xml" );
//
//        Grid grid1 = (Grid) context.getBean( "grid1" );
//        assertTrue( grid1.get( WhitePages.class ) instanceof JpaWhitePages );
//
//        Grid grid2 = (Grid) context.getBean( "grid2" );
//        assertTrue( grid2.get( WhitePages.class ) instanceof WhitePagesClient );
//
//        Grid grid3 = (Grid) context.getBean( "grid3" );
//        assertTrue( grid3.get( WhitePages.class ) instanceof WhitePagesImpl );
//
//        GridServiceDescription<GridNode> n1Gsd = grid2.get( WhitePages.class ).lookup( "node1" );
//        GridConnection<GridNode> conn = grid2.get( ConnectionFactoryService.class ).createConnection( n1Gsd );
//        GridNode remoteN1 = conn.connect();
//
//        KnowledgeBuilder kbuilder = remoteN1.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
//
//        assertNotNull( kbuilder );
//
//        String rule = "package test\n"
//                      + "import org.kie.container.spring.SpringDroolsGridTest.MyObject;\n"
//                      + "global MyObject myGlobalObj;\n"
//                      + "rule \"test\""
//                      + "  when"
//                      + "       $o: MyObject()"
//                      + "  then"
//                      + "      System.out.println(\"My Global Object -> \"+myGlobalObj.getName());"
//                      + "      System.out.println(\"Rule Fired! ->\"+$o.getName());"
//                      + " end";
//
//        kbuilder.add( new ByteArrayResource( rule.getBytes() ),
//                          ResourceType.DRL );
//
//        KnowledgeBuilderErrors errors = kbuilder.getErrors();
//        if ( errors != null && errors.size() > 0 ) {
//            for ( KnowledgeBuilderError error : errors ) {
//                System.out.println( "Error: " + error.getMessage() );
//
//            }
//            fail( "KnowledgeBase did not build" );
//        }
//
//        KnowledgeBase kbase = remoteN1.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
//
//        assertNotNull( kbase );
//
//        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
//
//        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
//
//        assertNotNull( session );
//        session.setGlobal( "myGlobalObj",
//                           new MyObject( "myGlobalObj" ) );
//
//        FactHandle handle = session.insert( new MyObject( "myObj1" ) );
//        assertNotNull( handle );
//
//        int fired = session.fireAllRules();
//        assertEquals( 1,
//                                 fired );
//
//        session.retract( handle );
//
//        handle = session.insert( new MyObject( "myObj2" ) );
//
//        session.update( handle,
//                        new MyObject( "myObj3" ) );
//
//        fired = session.fireAllRules();
//
//        remoteN1.dispose();
//        grid1.get( SocketService.class ).close();
//    }

    public static class MyObject
        implements
        Serializable {
        private String name;

        public MyObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
