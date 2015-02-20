package org.kie.server.client;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.*;

public interface KieServicesClient {
    ServiceResponse<KieServerInfo> getServerInfo();

    ServiceResponse<KieContainerResourceList> listContainers();

    ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource);

    ServiceResponse<KieContainerResource> getContainerInfo(String id);

    ServiceResponse<Void> disposeContainer(String id);

    ServiceResponse<String> executeCommands(String id, String payload);

    ServiceResponsesList executeScript(CommandScript script);

    ServiceResponse<KieScannerResource> getScannerInfo(String id);

    ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource);

    ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId);
}
