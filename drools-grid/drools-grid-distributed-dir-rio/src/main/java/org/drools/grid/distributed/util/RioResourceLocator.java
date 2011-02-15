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

package org.drools.grid.distributed.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.ServiceDiscoveryManager;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNodeService;

public class RioResourceLocator {
    public static ExecutionNodeService locateExecutionNodeById(String id) throws IOException, InterruptedException{
        IDEntry[] ids = new IDEntry[]{new IDEntry(id)};
        Class[] classes = new Class[]{ExecutionNodeService.class};
        ServiceTemplate tmpl = new ServiceTemplate(null, classes, ids);
        LookupDiscoveryManager ldm =
                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
                null,
                null);
        ServiceDiscoveryManager sdm =
                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
        /* Wait no more then 10 seconds to discover the service */
        ServiceItem item = sdm.lookup(tmpl, null, 30000);
        if(item == null){
            throw new IllegalStateException("No Execution Node Found");
        }
        ExecutionNodeService executionNode = (ExecutionNodeService) item.service;
        
        return executionNode;
    }
    
    public static DirectoryNodeService locateDirectoryNodeById(String id) throws IOException, InterruptedException{
        IDEntry[] ids = new IDEntry[]{new IDEntry(id)};
        Class[] classes = new Class[]{DirectoryNodeService.class};
        ServiceTemplate tmpl = new ServiceTemplate(null, classes, ids);
        LookupDiscoveryManager ldm =
                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
                null,
                null);
        ServiceDiscoveryManager sdm =
                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
        /* Wait no more then 10 seconds to discover the service */
        ServiceItem item = sdm.lookup(tmpl, null, 30000);
        if(item == null){
            throw new IllegalStateException("No Directory Node Found");
        }
        DirectoryNodeService directoryNode = (DirectoryNodeService) item.service;
        
        return directoryNode;
    }



    public static List<ExecutionNodeService> locateExecutionNodes() throws IOException, InterruptedException{
        List<ExecutionNodeService> executionNodes = new ArrayList<ExecutionNodeService>();
        Class[] classes = new Class[]{ExecutionNodeService.class};
        ServiceTemplate tmpl = new ServiceTemplate(null, classes, null);
        LookupDiscoveryManager ldm =
                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
                null,
                null);
        System.out.println("Discovering ExecutionNodeService  services ...");
        ServiceDiscoveryManager sdm =
                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
        /* Wait no more then 10 seconds to discover the service */
        ServiceItem[] items = sdm.lookup(tmpl, 1,100, null, 30000);
        

        for (int i = 0; i < items.length; i++) {
            if (items[i].service instanceof ExecutionNodeService) {
                executionNodes.add((ExecutionNodeService) items[i].service);
            }
        }
        return executionNodes;

    }

    public static List<DirectoryNodeService> locateDirectoryNodes() throws IOException, InterruptedException{
        List<DirectoryNodeService> directoryNodes = new ArrayList<DirectoryNodeService>();
        Class[] classes = new Class[]{DirectoryNodeService.class};
        ServiceTemplate tmpl = new ServiceTemplate(null, classes, null);
        LookupDiscoveryManager ldm =
                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
                null,
                null);
        System.out.println("Discovering DirectoryNodeService services ...");
        ServiceDiscoveryManager sdm =
                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
        /* Wait no more then 10 seconds to discover the service */
        ServiceItem[] items = sdm.lookup(tmpl, 1,100, null, 30000);
       

        for (int i = 0; i < items.length; i++) {

            if (items[i].service instanceof DirectoryNodeService) {
                directoryNodes.add((DirectoryNodeService) items[i].service);
            }
        }
        return directoryNodes;

    }

}
