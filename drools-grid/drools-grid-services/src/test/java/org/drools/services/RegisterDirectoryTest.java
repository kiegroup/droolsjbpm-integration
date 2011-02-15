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
package org.drools.services;

import java.rmi.RemoteException;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.services.DirectoryInstance;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.DirectoryInstanceConfiguration;
import org.drools.grid.services.configuration.ExecutionEnvironmentConfiguration;
import org.drools.grid.services.configuration.GridTopologyConfiguration;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.factory.GridTopologyFactory;
import org.drools.grid.services.strategies.DirectoryInstanceByPrioritySelectionStrategy;
import org.drools.grid.services.strategies.ExecutionEnvByPrioritySelectionStrategy;
import org.drools.io.ResourceFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegisterDirectoryTest {

    private GridTopology grid;

    public RegisterDirectoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void directoryLocalTest() throws ConnectorException,
                                    RemoteException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration
                .addDirectoryInstance( new DirectoryInstanceConfiguration( "MyLocalDir",
                                                                           new LocalProvider() ) );
        gridTopologyConfiguration
                .addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyLocalEnv",
                                                                                 new LocalProvider() ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );
        Assert.assertNotNull( this.grid );

        DirectoryInstance directory = this.grid.getBestDirectoryInstance( new DirectoryInstanceByPrioritySelectionStrategy() );
        Assert.assertNotNull( "Directory Instance null",
                              directory );

        DirectoryNodeService dir = directory.getDirectoryNode().get( DirectoryNodeService.class );
        //directory.getConnector().disconnect();

        Assert.assertNotNull( "Dir Null",
                              dir );
        Assert.assertEquals( 2,
                             dir.getExecutorsMap().size() );

        this.grid.dispose();
    }

    @Test
    public void multiDirectoryLocalTest() throws ConnectorException,
                                         RemoteException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration.addDirectoryInstance( new DirectoryInstanceConfiguration( "MyLocalDir",
                                                                                            new LocalProvider() ) );
        gridTopologyConfiguration.addDirectoryInstance( new DirectoryInstanceConfiguration( "MyLocalDir2",
                                                                                            new LocalProvider() ) );

        gridTopologyConfiguration.addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyLocalEnv",
                                                                                                  new LocalProvider() ) );
        gridTopologyConfiguration.addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyLocalEnv2",
                                                                                                  new LocalProvider() ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );

        Assert.assertNotNull( this.grid );

        DirectoryInstance directory = this.grid.getDirectoryInstance( "MyLocalDir" );
        Assert.assertNotNull( "DirInstance is null!",
                              directory );

        DirectoryNodeService dir = directory.getDirectoryNode().get( DirectoryNodeService.class );

        Assert.assertNotNull( "Dir is null!",
                              dir );
        //This assertion is not deterministic
        //Assert.assertEquals(4, dir.getExecutorsMap().size());

        DirectoryInstance directory2 = this.grid.getDirectoryInstance( "MyLocalDir2" );
        Assert.assertNotNull( "DirInstance 2 is null!",
                              directory2 );

        DirectoryNodeService dir2 = directory2.getDirectoryNode().get( DirectoryNodeService.class );

        Assert.assertNotNull( "Dir 2 is null!",
                              dir2 );
        //This assertion is not deterministic
        //Assert.assertEquals(3, dir2.getExecutorsMap().size());

        // the only thing that is for sure is
        Assert.assertTrue( (dir2.getExecutorsMap().size() + dir.getExecutorsMap().size()) > 4 );

        this.grid.dispose();

    }

    @Test
    public void registerKbaseInLocalDirectoryTest() throws ConnectorException,
                                                   RemoteException {

        GridTopologyConfiguration gridTopologyConfiguration = new GridTopologyConfiguration( "MyTopology" );
        gridTopologyConfiguration.addDirectoryInstance( new DirectoryInstanceConfiguration( "MyLocalDir",
                                                                                            new LocalProvider() ) );
        gridTopologyConfiguration.addExecutionEnvironment( new ExecutionEnvironmentConfiguration( "MyLocalEnv",
                                                                                                  new LocalProvider() ) );

        this.grid = GridTopologyFactory.build( gridTopologyConfiguration );
        Assert.assertNotNull( this.grid );

        ExecutionEnvironment ee = this.grid.getBestExecutionEnvironment( new ExecutionEnvByPrioritySelectionStrategy() );
        Assert.assertNotNull( ee );

        ExecutionNode node = ee.getExecutionNode();
        Assert.assertNotNull( node );

        // Do a basic Runtime Test that register a ksession and fire some rules.
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
                node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( "Errors: " + kbuilder.getErrors() );
        }

        KnowledgeBase kbase =
                node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase( "DoctorsKBase" );
        Assert.assertNotNull( kbase );

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        DirectoryInstance directory = this.grid.getBestDirectoryInstance( new DirectoryInstanceByPrioritySelectionStrategy() );
        Assert.assertNotNull( "Directory Instance null",
                              directory );

        DirectoryNodeService dirService = directory.getDirectoryNode().get( DirectoryNodeService.class );

        kbase = dirService.lookupKBase( "DoctorsKBase" );
        Assert.assertNotNull( kbase );
        //directory.getConnector().disconnect();

        this.grid.dispose();

    }
}
