/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.springboot.jbpm;

import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.locator.ByCaseIdContainerLocator;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.jbpm.locator.ByProcessInstanceIdContainerLocator;
import org.kie.server.services.jbpm.locator.ByTaskIdContainerLocator;

/**
 * Helper class that provides easy access to find container ids for an alias.
 *
 */
public class ContainerAliasResolver {
   
    private KieServerRegistry registry;
    
    
    public ContainerAliasResolver(KieServerRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Looks up latest container id for given alias.
     * @param alias container alias
     * @return resolved latest container id
     * @throws IllegalArgumentException in case there are no containers for given alias
     */
    public String latest(String alias) {
        return registry.getContainerId(alias, ContainerLocatorProvider.get().getLocator());
    }
    
    /**
     * Looks up container id for given alias that is associated with case instance
     * @param alias container alias
     * @param caseId unique case instance id
     * @return
     * @throws IllegalArgumentException in case there are no containers for given alias
     */
    public String forCaseInstance(String alias, String caseId) {
        return registry.getContainerId(alias, new ByCaseIdContainerLocator(caseId));
    }
    
    /**
     * Looks up container id for given alias that is associated with process instance
     * @param alias container alias
     * @param processInstanceId unique process instance id
     * @return
     * @throws IllegalArgumentException in case there are no containers for given alias
     */
    public String forProcessInstance(String alias, long processInstanceId) {
        return registry.getContainerId(alias, new ByProcessInstanceIdContainerLocator(processInstanceId));
    }
    
    /**
     * Looks up container id for given alias that is associated with task instance
     * @param alias container alias
     * @param taskId unique task instance id
     * @return
     * @throws IllegalArgumentException in case there are no containers for given alias
     */
    public String forTaskInstance(String alias, long taskId) {
        return registry.getContainerId(alias, new ByTaskIdContainerLocator(taskId));
    }
}
