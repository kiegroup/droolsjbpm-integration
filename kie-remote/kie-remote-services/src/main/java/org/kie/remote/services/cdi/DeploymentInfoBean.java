/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.cdi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.cdi.Deploy;
import org.jbpm.services.cdi.Undeploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of the list of (deployed) deployment ids, since this is used by multiple resources
 */
@ApplicationScoped
public class DeploymentInfoBean {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentInfoBean.class);

    private Set<String> deploymentIds = new CopyOnWriteArraySet<String>();

    // Observer methods -----------------------------------------------------------------------------------------------------------

    /**
     * Called when the workbench/console/business-central deploys a new deployment.
     * @param event
     */
    public void addOnDeploy(@Observes @Deploy DeploymentEvent event) {
        deploymentIds.add(event.getDeploymentId());
    }

    /**
     * Called when the workbench/console/business-central *un*deploys (removes) a deployment.
     * @param event
     */
    public void removeOnUnDeploy(@Observes @Undeploy DeploymentEvent event) {
        deploymentIds.remove(event.getDeploymentId());
    }

     public Collection<String> getDeploymentIds() {
         return new ArrayList<String>(deploymentIds);
     }

     public static boolean emptyDeploymentId(String deploymentId) {
         return deploymentId == null || deploymentId.trim().isEmpty();
     }
}
