/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.api;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public interface KieServer {

    ServiceResponse<KieServerInfo> getInfo();

    ServiceResponse<KieContainerResource> createContainer(String containerId, KieContainerResource container);

    ServiceResponse<KieContainerResourceList> listContainers(KieContainerResourceFilter containerFilter);

    ServiceResponse<KieContainerResource> getContainerInfo(String id);

    ServiceResponse<Void> disposeContainer(String containerId);

    ServiceResponse<KieScannerResource> getScannerInfo(String id);

    ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource);

    ServiceResponse<ReleaseId> getContainerReleaseId(String id);

    ServiceResponse<ReleaseId> updateContainerReleaseId(String id, ReleaseId releaseId);

    ServiceResponse<KieServerStateInfo> getServerState();

    void addServerMessage(Message message);

    void addContainerMessage(String containerId, Message message);
}
