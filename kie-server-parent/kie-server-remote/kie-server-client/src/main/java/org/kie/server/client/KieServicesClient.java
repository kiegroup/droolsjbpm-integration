/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import org.kie.api.command.Command;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.jms.ResponseHandler;

public interface KieServicesClient {

    <T> T getServicesClient(Class<T> serviceClient);

    ServiceResponse<KieServerInfo> getServerInfo();

    ServiceResponse<KieContainerResourceList> listContainers();

    ServiceResponse<KieContainerResourceList> listContainers(KieContainerResourceFilter containerFilter);

    ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource);
    
    ServiceResponse<KieContainerResource> activateContainer(String id);
    
    ServiceResponse<KieContainerResource> deactivateContainer(String id);

    ServiceResponse<KieContainerResource> getContainerInfo(String id);

    ServiceResponse<Void> disposeContainer(String id);

    ServiceResponsesList executeScript(CommandScript script);

    ServiceResponse<KieScannerResource> getScannerInfo(String id);

    ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource);

    ServiceResponse<ReleaseId> getReleaseId(String containerId);

    ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId);

    ServiceResponse<KieServerStateInfo> getServerState();

    // for backward compatibility reason

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     * @see RuleServicesClient#executeCommands(String, String)
     * @deprecated
     */
    @Deprecated
    ServiceResponse<String> executeCommands(String id, String payload);

    /**
     * This method is deprecated on KieServicesClient as it was moved to RuleServicesClient
     * @see RuleServicesClient#executeCommands(String, Command)
     * @deprecated
     */
    @Deprecated
    ServiceResponse<String> executeCommands(String id, Command<?> cmd);

    /**
     * Sets the classloader for user class unmarshalling
     * @param classLoader
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * Returns the current classloader in use for unmarshalling
     * @return
     */
    ClassLoader getClassLoader();

    String getConversationId();

    void completeConversation();

    void setResponseHandler(ResponseHandler responseHandler);
}
