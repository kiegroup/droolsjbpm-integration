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
package org.drools.grid.services.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import net.jini.core.lookup.ServiceItem;
//import net.jini.core.lookup.ServiceTemplate;
//import net.jini.discovery.LookupDiscoveryManager;
//import net.jini.lease.LeaseRenewalManager;
//import net.jini.lookup.ServiceDiscoveryManager;
import org.drools.grid.DirectoryNodeService;
import org.drools.grid.ExecutionNodeService;

/**
 *
 * @author salaboy
 */
public class RioProvider extends GenericProvider {

    private List<ExecutionNodeService> executionNodes;
    private List<DirectoryNodeService> directoryNodes;

    public RioProvider(List<ExecutionNodeService> executionNodes, List<DirectoryNodeService> directoryNodes) {
        this.executionNodes = executionNodes;
        this.directoryNodes = directoryNodes;
    }

    public RioProvider() {
        this.executionNodes = new ArrayList<ExecutionNodeService>();
        this.directoryNodes = new ArrayList<DirectoryNodeService>();
    }

    @Override
    public String getId() {
        return "RioProvider:";
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.DistributedRio;
    }

    public ExecutionNodeService getExecutionNode() {
        return executionNodes.get(0);
    }

    public DirectoryNodeService getDirectoryNode() {
        return directoryNodes.get(0);
    }

    public List<ExecutionNodeService> getExecutionNodes() {
        return executionNodes;
    }

    public List<DirectoryNodeService> getDirectoryNodes() {
        return directoryNodes;
    }

    public void lookupExecutionNodeServices() throws IOException, InterruptedException {

        throw new UnsupportedOperationException("Uncomment RIO DEPS and this method!");
//        Class[] classes = new Class[]{ExecutionNodeService.class};
//        ServiceTemplate tmpl = new ServiceTemplate(null, classes, null);
//        LookupDiscoveryManager ldm =
//                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
//                null,
//                null);
//        System.out.println("Discovering ExecutionNodeService  services ...");
//        ServiceDiscoveryManager sdm =
//                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
//        /* Wait no more then 10 seconds to discover the service */
//        ServiceItem[] items = sdm.lookup(tmpl, 1,100, null, 30000);
//        System.out.println("Service  items.lenght"+items.length);
//
//        for (int i = 0; i < items.length; i++) {
//            if (items[i].service instanceof ExecutionNodeService) {
//                executionNodes.add((ExecutionNodeService) items[i].service);
//            }
//        }
    }

    public void lookupDirectoryNodeServices() throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Uncomment RIO DEPS and this method!");
//        Class[] classes = new Class[]{DirectoryNodeService.class};
//        ServiceTemplate tmpl = new ServiceTemplate(null, classes, null);
//        LookupDiscoveryManager ldm =
//                new LookupDiscoveryManager(LookupDiscoveryManager.ALL_GROUPS,
//                null,
//                null);
//        System.out.println("Discovering DirectoryNodeService services ...");
//        ServiceDiscoveryManager sdm =
//                new ServiceDiscoveryManager(ldm, new LeaseRenewalManager());
//        /* Wait no more then 10 seconds to discover the service */
//        ServiceItem[] items = sdm.lookup(tmpl, 1,100, null, 30000);
//        System.out.println("Service  items.lenght"+items.length);
//
//        for (int i = 0; i < items.length; i++) {
//
//            if (items[i].service instanceof DirectoryNodeService) {
//                directoryNodes.add((DirectoryNodeService) items[i].service);
//            }
//        }
    }
}
