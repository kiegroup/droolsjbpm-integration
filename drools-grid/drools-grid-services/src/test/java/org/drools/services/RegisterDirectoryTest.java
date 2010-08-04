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
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.services.DirectoryInstance;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.GridTopology;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.strategies.DirectoryInstanceByPrioritySelectionStrategy;
import org.drools.grid.services.strategies.ExecutionEnvByPrioritySelectionStrategy;
import org.drools.io.ResourceFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author salaboy
 */
public class RegisterDirectoryTest {

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
     public void directoryLocalTest() throws ConnectorException, RemoteException {
         System.out.println("First Test!!");
        GridTopology grid = new GridTopology("MyBusinessUnit");
        GenericProvider localDirProvider = new LocalProvider();
        GenericProvider localEnvProvider = new LocalProvider();

        grid.registerDirectoryInstance("MyLocalDir", localDirProvider);
        grid.registerExecutionEnvironment("MyLocalEnv", localEnvProvider);



        DirectoryInstance directory = grid.getBestDirectoryInstance(new DirectoryInstanceByPrioritySelectionStrategy());
        Assert.assertNotNull("Directory Instance null", directory);

        DirectoryNodeService dir = directory.getDirectoryService().get(DirectoryNodeService.class);;
        Assert.assertNotNull("Dir Null", dir);

        Assert.assertEquals(2, dir.getExecutorsMap().size());

        grid.dispose();
     }

     @Test
     public void multiDirectoryLocalTest() throws ConnectorException, RemoteException {
         
        GridTopology grid = new GridTopology("MyBusinessUnit");
        GenericProvider localDirProvider = new LocalProvider();
        GenericProvider localDirProvider2 = new LocalProvider();
        GenericProvider localEnvProvider = new LocalProvider();
        GenericProvider localEnvProvider2 = new LocalProvider();

        grid.registerDirectoryInstance("MyLocalDir", localDirProvider);
        grid.registerDirectoryInstance("MyLocalDir2", localDirProvider2);
        grid.registerExecutionEnvironment("MyLocalEnv", localEnvProvider);
        grid.registerExecutionEnvironment("MyLocalEnv2", localEnvProvider2);



        //DirectoryInstance directory = grid.getBestDirectoryInstance(new DirectoryInstanceByPrioritySelectionStrategy());
        DirectoryInstance directory = grid.getDirectoryInstance("MyLocalDir");
        Assert.assertNotNull("DirInstance is null!",directory);

        DirectoryNodeService dir = directory.getDirectoryService().get(DirectoryNodeService.class);;
        Assert.assertNotNull("Dir is null!",dir);
        //This assertion is not deterministic
        //Assert.assertEquals(4, dir.getExecutorsMap().size());

        DirectoryInstance directory2 = grid.getDirectoryInstance("MyLocalDir2");
        Assert.assertNotNull("DirInstance 2 is null!",directory2);

        DirectoryNodeService dir2 = directory2.getDirectoryService().get(DirectoryNodeService.class);;
        Assert.assertNotNull("Dir 2 is null!", dir2);
        //This assertion is not deterministic
        //Assert.assertEquals(3, dir2.getExecutorsMap().size());

        // the only thing that is for sure is
        System.out.println("dir1 exec map"+dir.getExecutorsMap());
        System.out.println("dir2 exec map"+dir2.getExecutorsMap());
        Assert.assertTrue( (dir2.getExecutorsMap().size() + dir.getExecutorsMap().size()) > 4 );

        grid.dispose();

     }

      @Test
     public void registerKbaseInLocalDirectoryTest() throws ConnectorException, RemoteException {
        
        GridTopology grid = new GridTopology("MyBusinessUnit");
        GenericProvider localDirProvider = new LocalProvider();
        GenericProvider localEnvProvider = new LocalProvider();

        grid.registerDirectoryInstance("MyLocalDir", localDirProvider);
        grid.registerExecutionEnvironment("MyLocalEnv", localEnvProvider);


       

         ExecutionEnvironment ee = grid.getBestExecutionEnvironment(new ExecutionEnvByPrioritySelectionStrategy());
        Assert.assertNotNull(ee);
        System.out.println("EE Name = "+ee.getName());

        ExecutionNode node = ee.getExecutionNode();
        Assert.assertNotNull(node);

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
                node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()),
                ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            System.out.println("Errors: " + kbuilder.getErrors());
        }

        KnowledgeBase kbase =
                node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase("DoctorsKBase");
        Assert.assertNotNull(kbase);

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

         DirectoryInstance directory = grid.getBestDirectoryInstance(new DirectoryInstanceByPrioritySelectionStrategy());
        Assert.assertNotNull("Directory Instance null", directory);

       DirectoryNodeService dirService = directory.getDirectoryService().get(DirectoryNodeService.class);;
       kbase = dirService.lookupKBase("DoctorsKBase");
       Assert.assertNotNull(kbase);

       grid.dispose();
       

     }
     

}