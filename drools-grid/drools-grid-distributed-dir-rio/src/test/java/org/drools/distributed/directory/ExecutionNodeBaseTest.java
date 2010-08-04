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

package org.drools.distributed.directory;


import java.rmi.RemoteException;
import junit.framework.Assert;
import org.drools.grid.ConnectorException;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNode;
import org.drools.grid.GridConnection;
import org.junit.Test;
/**
 *
 * @author salaboy
 */

public abstract class ExecutionNodeBaseTest {

    protected DirectoryNodeService directory;
    protected ExecutionNode node;
    protected GridConnection connection = new GridConnection();
    public ExecutionNodeBaseTest() {
        
    }


    @Test
    public void directoryTest() throws ConnectorException, RemoteException{
        directory.register("blah","blash");

        Assert.assertEquals("blash", directory.lookupId("blah"));

        
    }

   


}
