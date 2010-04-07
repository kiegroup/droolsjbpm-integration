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


import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import junit.framework.Assert;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.base.MapGlobalResolver;
import org.drools.builder.JPAKnowledgeFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.local.LocalConnection;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.grid.JPAKnowledgeProviderLocalClient;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class LocalExecutionNodeTest extends ExecutionNodeBaseTest{

    private static EntityManagerFactory entityManagerFactory;
    private static PoolingDataSource dataSource;
    public LocalExecutionNodeTest() {
    }

    @BeforeClass
    public static void initializeEntityManager() {
        System.out.println("Initializing Entity Manager");
        entityManagerFactory = Persistence.createEntityManagerFactory("org.drools.persistence.jpa");
    }

    @BeforeClass
    public static void initializeDataSource() {
        System.out.println("Initializing Datasource");
        dataSource = new PoolingDataSource();
        dataSource.setUniqueName( "jdbc/testDS1" );
        dataSource.setClassName( "org.h2.jdbcx.JdbcDataSource" );
        dataSource.setMaxPoolSize( 3 );
        dataSource.setAllowLocalTransactions( true );
        dataSource.getDriverProperties().put( "user",
                                       "sa" );
        dataSource.getDriverProperties().put( "password",
                                       "sasa" );
        dataSource.getDriverProperties().put( "URL",
                                       "jdbc:h2:mem:mydb" );
        dataSource.init();
    }

    @Before
    public void configureNode() {
        connection = new LocalConnection();

        node = connection.getExecutionNode(null);
        node.set(JPAKnowledgeFactoryService.class, new JPAKnowledgeProviderLocalClient());
    }
    
    private Environment newEnvironment() {
        Environment environment = KnowledgeBaseFactory.newEnvironment();
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
        environment.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
        environment.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());

        return environment;
    }

    @Test
    public void persistenceTest(){
        Environment env = newEnvironment();
         String str = "";
        str += "package org.drools \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello1!!!\" ); \n";
        str += "end \n";
        str += "rule rule2 \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "then \n";
        str += "    System.out.println( \"hello2!!!\" ); \n";
        str += "end \n";


        KnowledgeBuilder kbuilder =
                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase =
                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );


        StatefulKnowledgeSession ksession = node
                                                .get(JPAKnowledgeFactoryService.class)
                                                .newStatefulKnowledgeSession(kbase, null, env);

        Assert.assertNotNull(ksession);

    }
   


}