/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.controller.api;

import java.util.List;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerInstance;

/**
 * KieServer controller administration that deals with management tasks of the controller assets - kie server instances.
 */
public interface KieServerControllerAdmin {

    /**
     * Add new KieServer described as KieServerInfo (which is minimal set of information). This operation does not
     * require actual KieServerInstance to be accessible but is more for making the controller aware of server(s)
     * that will be representing this instance.
     * @param kieServerInfo - minimal set of information to create KieServerInstance on controller
     * @return representation of newly created KieServerInstance
     * @throws KieServerControllerException thrown in case KieServerInstance is already created with given id
     */
    KieServerInstance addKieServerInstance(KieServerInfo kieServerInfo) throws KieServerControllerException;

    /**
     * Removes defined KieServerInstance identified by <code>identifier</code> making it not being managed any more.
     * Does not have any impact on running servers besides they will be notified about being unmanaged so they
     * will not attempt to connect to the controller
     * @param identifier unique identifier of KieServerInstance
     * @return removed definition of the KieServerInstance
     * @throws KieServerControllerException thrown in case there is no such KieServerInstance with given identifier
     */
    KieServerInstance removeKieServerInstance(String identifier) throws KieServerControllerException;

    /**
     * Returns all managed KieServerInstance by this controller
     * @return list of all known KieServerInstances
     */
    List<KieServerInstance> listKieServerInstances();

    /**
     * Returns details of given KieServerInstance
     * @param identifier unique identifier of KieServerInstance
     * @return details of KieServerInstance
     * @throws KieServerControllerException thrown in case there is no such KieServerInstance with given identifier
     */
    KieServerInstance getKieServerInstance(String identifier) throws KieServerControllerException;

    /**
     * Creates container within given kie server(s)
     * @param id kie server identifier
     * @param containerId container identifier
     * @param container KieContainerResource instance representing the actual container
     * @return
     */
    KieContainerResource createContainer(String id, String containerId, KieContainerResource container);

    /**
     * Deletes given container from kie server(s)
     * @param id kie server identifier
     * @param containerId container identifier
     */
    void deleteContainer(String id, String containerId);

    /**
     * Returns given container from kie server(s)
     * @param id kie server identifier
     * @param containerId container identifier
     */
    KieContainerResource getContainer(String id, String containerId);
}
