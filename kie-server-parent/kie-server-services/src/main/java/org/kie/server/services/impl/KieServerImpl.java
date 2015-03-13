package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.kie.api.KieServices;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.Results;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.Version;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.security.JACCIdentityProvider;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerImpl {

    private static final Logger             logger               = LoggerFactory.getLogger(KieServerImpl.class);

    private static final ServiceLoader<KieServerExtension> serverExtensions = ServiceLoader.load(KieServerExtension.class);

    private final KieServerRegistry context;

    private final KieServerStateRepository repository;


    public KieServerImpl() {
        this.context = new KieServerRegistryImpl();
        this.context.registerIdentityProvider(new JACCIdentityProvider());
        repository = new KieServerStateFileRepository();

        KieServerState currentState = repository.load(KieServerEnvironment.getServerId());

        for (KieServerExtension extension : serverExtensions) {
            if (!extension.isActive()) {
                continue;
            }
            try {
                extension.init(this, this.context);

                this.context.registerServerExtension(extension);

                logger.info("{} has been successfully registered as server extension", extension);
            } catch (Exception e) {
                logger.error("Error when initializing server extension of type {}", extension, e);
            }
        }

        // once all extensions are loaded connect and sync with controller if exists
        Set<String> controllers = currentState.getControllers();
        boolean controllerSynced = false;
        for (String controllerUrl : controllers ) {

            if (controllerUrl != null && !controllerUrl.isEmpty()) {
                String connectAndSyncUrl = controllerUrl + "/controller/server/" + KieServerEnvironment.getServerId();

                try {
                    KieContainerResourceList containerResourceList = makeHttpGetRequestAndCreateServiceResponse(connectAndSyncUrl, KieContainerResourceList.class);

                    if (containerResourceList != null) {

                        for (KieContainerResource containerResource : containerResourceList.getContainers()) {
//                            if (containerResource.getStatus() != KieContainerStatus.STARTED) {
//                                continue;
//                            }

                            createContainer(containerResource.getContainerId(), containerResource);
                        }
                    }
                    controllerSynced = true;
                    break;
                } catch (IllegalStateException e) {
                    // let's check all other controllers in case of running in cluster of controllers
                }

            }
        }

        if ( !controllerSynced ) {
            // no controller or no controller available proceed with local info only

            for (KieContainerResource containerResource : currentState.getContainers()) {
//                if (containerResource.getStatus() != KieContainerStatus.STARTED) {
//                    continue;
//                }
                createContainer(containerResource.getContainerId(), containerResource);
            }

            repository.store(KieServerEnvironment.getServerId(), currentState);
        }
    }

    public ServiceResponse<KieServerInfo> registerController(String controller, KieServerConfig kieServerConfig) {
        if (controller != null && !controller.isEmpty()) {
            this.context.registerController(controller);

            KieServerState currentState = repository.load(KieServerEnvironment.getServerId());
            currentState.setControllers(this.context.getControllers());
            currentState.setConfiguration(kieServerConfig);

            repository.store(KieServerEnvironment.getServerId(), currentState);
        }

        return getInfo();
    }

    public void destroy() {
        for (KieServerExtension extension : context.getServerExtensions()) {

            try {
                extension.destroy(this, this.context);

                this.context.unregisterServerExtension(extension);

                logger.info("{} has been successfully unregistered as server extension", extension);
            } catch (Exception e) {
                logger.error("Error when destroying server extension of type {}", extension, e);
            }
        }

    }


    public List<KieServerExtension> getServerExtensions() {
        return this.context.getServerExtensions();
    }



    public ServiceResponse<KieServerInfo> getInfo() {
        try {
            Version version = KieServerEnvironment.getVersion();
            String serverId = KieServerEnvironment.getServerId();
            String versionStr = version != null ? version.toString() : "Unknown-Version";
            return new ServiceResponse<KieServerInfo>(ServiceResponse.ResponseType.SUCCESS, "Kie Server info", new KieServerInfo(serverId, versionStr));
        } catch (Exception e) {
            logger.error("Error retrieving server info:", e);
            return new ServiceResponse<KieServerInfo>(ServiceResponse.ResponseType.FAILURE, "Error retrieving kie server info: " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<KieContainerResource> createContainer(String containerId, KieContainerResource container) {
        if (container == null || container.getReleaseId() == null) {
            logger.error("Error creating container. Release Id is null: " + container);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Failed to create container " + containerId + ". Release Id is null: " + container + ".");
        }
        ReleaseId releaseId = container.getReleaseId();
        try {
            KieContainerInstanceImpl ci = new KieContainerInstanceImpl(containerId, KieContainerStatus.CREATING);
            KieContainerInstanceImpl previous = null;
            // have to synchronize on the ci or a concurrent call to dispose may create inconsistencies
            synchronized (ci) {
                previous = context.registerContainer(containerId, ci);
                if (previous == null) {
                    try {
                        KieServices ks = KieServices.Factory.get();
                        InternalKieContainer kieContainer = (InternalKieContainer) ks.newKieContainer(releaseId);
                        if (kieContainer != null) {
                            ci.setKieContainer(kieContainer);
                            logger.debug("Container {} (for release id {}) general initialization: DONE", containerId, releaseId);
                            // process server extensions
                            List<KieServerExtension> extensions = context.getServerExtensions();
                            for (KieServerExtension extension : extensions) {
                                extension.createContainer(containerId, ci, new HashMap<String, Object>());
                                logger.debug("Container {} (for release id {}) {} initialization: DONE", containerId, releaseId, extension);
                            }

                            ci.getResource().setStatus(KieContainerStatus.STARTED);
                            logger.info("Container {} (for release id {}) successfully started", containerId, releaseId);


                            // store the current state of the server
                            KieServerState currentState = repository.load(KieServerEnvironment.getServerId());
                            container.setStatus(KieContainerStatus.STARTED);
                            currentState.getContainers().add(container);

                            repository.store(KieServerEnvironment.getServerId(), currentState);

                            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully deployed with module " + releaseId + ".", ci.getResource());
                        } else {
                            ci.getResource().setStatus(KieContainerStatus.FAILED);
                            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Failed to create container " + containerId + " with module " + releaseId + ".");
                        }
                    } catch (Exception e) {
                        logger.error("Error creating container '" + containerId + "' for module '" + releaseId + "'", e);
                        ci.getResource().setStatus(KieContainerStatus.FAILED);
                        return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Failed to create container " + containerId + " with module " + releaseId + ": " + e.getClass().getName() + ": " + e.getMessage());
                    }
                } else {
                    return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " already exists.", previous.getResource());
                }
            }
        } catch (Exception e) {
            logger.error("Error creating container '" + containerId + "' for module '" + releaseId + "'", e);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Error creating container " + containerId +
                    " with module " + releaseId + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<KieContainerResourceList> listContainers() {
        try {
            List<KieContainerResource> containers = new ArrayList<KieContainerResource>();
            for (KieContainerInstanceImpl instance : context.getContainers()) {
                containers.add(instance.getResource());
            }
            KieContainerResourceList cil = new KieContainerResourceList(containers);
            return new ServiceResponse<KieContainerResourceList>(ServiceResponse.ResponseType.SUCCESS, "List of created containers", cil);
        } catch (Exception e) {
            logger.error("Error retrieving list of containers", e);
            return new ServiceResponse<KieContainerResourceList>(ServiceResponse.ResponseType.FAILURE, "Error listing containers: " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        try {
            KieContainerInstanceImpl ci = context.getContainer(id);
            if (ci != null) {
                if( ci.getResource().getScanner() == null ) {
                    ci.getResource().setScanner( getScannerResource( ci ) );
                }
                return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.SUCCESS, "Info for container " + id, ci.getResource());
            }
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Container " + id + " is not instantiated.");
        } catch (Exception e) {
            logger.error("Error retrieving info for container '" + id + "'", e);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Error retrieving container info: " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<Void> disposeContainer(String containerId) {
        try {
            KieContainerInstanceImpl kci = (KieContainerInstanceImpl) context.unregisterContainer(containerId);
            if (kci != null) {
                synchronized (kci) {
                    kci.setStatus(KieContainerStatus.DISPOSING); // just in case
                    if (kci.getKieContainer() != null) {
                        InternalKieContainer kieContainer = kci.getKieContainer();
                        kci.setKieContainer(null); // helps reduce concurrent access issues
                        try {
                            // this may fail, but we already removed the container from the registry
                            kieContainer.dispose();

                            logger.debug("Container {} (for release id {}) general shutdown: DONE", containerId, kci.getResource().getReleaseId());
                            // process server extensions
                            List<KieServerExtension> extensions = context.getServerExtensions();
                            for (KieServerExtension extension : extensions) {
                                extension.disposeContainer(containerId, new HashMap<String, Object>());
                                logger.debug("Container {} (for release id {}) {} shutdown: DONE", containerId, kci.getResource().getReleaseId(), extension);
                            }
                        } catch (Exception e) {
                            logger.warn("Container '" + containerId + "' disposed, but an unexpected exception was raised", e);
                            return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId +
                                    " disposed, but exception was raised: " + e.getClass().getName() + ": " + e.getMessage());
                        }
                        logger.info("Container {} (for release id {}) successfully stopped", containerId, kci.getResource().getReleaseId());

                        // store the current state of the server
                        KieServerState currentState = repository.load(KieServerEnvironment.getServerId());

                        List<KieContainerResource> containers = new ArrayList<KieContainerResource>();
                        for (KieContainerResource containerResource : currentState.getContainers()) {
                            if ( !containerId.equals(containerResource.getContainerId()) ) {
                                containers.add(containerResource);
                            }
                        }
                        currentState.setContainers(new HashSet<KieContainerResource>(containers));

                        repository.store(KieServerEnvironment.getServerId(), currentState);

                        return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully disposed.");
                    } else {
                        return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " was not instantiated.");
                    }
                }
            } else {
                return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " was not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error disposing Container '" + containerId + "'", e);
            return new ServiceResponse<Void>(ServiceResponse.ResponseType.FAILURE, "Error disposing container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        try {
            KieContainerInstanceImpl kci = context.getContainer(id);
            if (kci != null && kci.getKieContainer() != null) {
                KieScannerResource info = getScannerResource( kci );
                kci.getResource().setScanner( info );
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS, "Scanner info successfully retrieved", info);
            } else {
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                        "Unknown container " + id + ".");
            }
        } catch (Exception e) {
            logger.error("Error retrieving scanner info for container '" + id + "'.", e);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error retrieving scanner info for container '" + id + "': " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private KieScannerResource getScannerResource(KieContainerInstanceImpl kci) {
        InternalKieScanner scanner = kci.getScanner();
        KieScannerResource info = null;
        if (scanner != null) {
            info = new KieScannerResource(mapStatus(scanner.getStatus()), scanner.getPollingInterval());
        } else {
            info = new KieScannerResource( KieScannerStatus.DISPOSED);
        }
        return info;
    }

    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        if (resource == null || resource.getStatus() == null) {
            logger.error("Error updating scanner for container " + id + ". Status is null: " + resource);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error updating scanner for container " + id + ". Status is null: " + resource);
        }
        KieScannerStatus status = resource.getStatus();
        try {
            KieContainerInstanceImpl kci = context.getContainer(id);
            if (kci != null && kci.getKieContainer() != null) {
                ServiceResponse<KieScannerResource> result = null;
                switch (status) {
                    case CREATED:
                        // create the scanner
                        result = createScanner(id, kci);
                        break;
                    case STARTED:
                        // start the scanner
                        result = startScanner(id, resource, kci);
                        break;
                    case STOPPED:
                        // stop the scanner
                        result = stopScanner(id, resource, kci);
                        break;
                    case SCANNING:
                        // scan now
                        result = scanNow(id, resource, kci);
                        break;
                    case DISPOSED:
                        // dispose
                        result = disposeScanner(id, resource, kci);
                        break;
                    default:
                        // error
                        result = new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                                "Unknown status '" + status + "' for scanner on container " + id + ".");
                        break;
                }
                kci.getResource().setScanner( result.getResult() ); // might be null, but that is ok
                return result;
            } else {
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                        "Unknown container " + id + ".");
            }
        } catch (Exception e) {
            logger.error("Error updating scanner for container '" + id + "': " + resource, e);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error updating scanner for container '" + id +
                    "': " + resource + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<KieScannerResource> startScanner(String id, KieScannerResource resource, KieContainerInstanceImpl kci) {
        if (kci.getScanner() == null) {
            ServiceResponse<KieScannerResource> response = createScanner(id, kci);
            if (ResponseType.FAILURE.equals(response.getType())) {
                return response;
            }
        }
        if (KieScannerStatus.STOPPED.equals(mapStatus(kci.getScanner().getStatus())) &&
                resource.getPollInterval() != null) {
            kci.getScanner().start(resource.getPollInterval());
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfully created.",
                    getScannerResource(kci));
        } else if (!KieScannerStatus.STOPPED.equals(mapStatus(kci.getScanner().getStatus()))) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: " + mapStatus(kci.getScanner().getStatus()),
                    getScannerResource(kci));
        } else if (resource.getPollInterval() == null) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid polling interval: " + resource.getPollInterval(),
                    getScannerResource(kci));
        }
        return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                "Unknown error starting scanner. Scanner was not started." + resource,
                getScannerResource(kci));
    }

    private ServiceResponse<KieScannerResource> stopScanner(String id, KieScannerResource resource, KieContainerInstanceImpl kci) {
        if (kci.getScanner() == null) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid call. Scanner is not instantiated. ",
                    getScannerResource(kci));
        }
        if (KieScannerStatus.STARTED.equals(mapStatus(kci.getScanner().getStatus())) ||
                KieScannerStatus.SCANNING.equals(mapStatus(kci.getScanner().getStatus()))) {
            kci.getScanner().stop();
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfully stopped.",
                    getScannerResource(kci));
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: " + mapStatus(kci.getScanner().getStatus()),
                    getScannerResource(kci));
        }
    }

    private ServiceResponse<KieScannerResource> scanNow(String id, KieScannerResource resource, KieContainerInstanceImpl kci) {
        if (kci.getScanner() == null) {
            createScanner( id, kci );
        }
        KieScannerStatus kss = mapStatus( kci.getScanner().getStatus() );
        if (KieScannerStatus.STOPPED.equals( kss ) || KieScannerStatus.CREATED.equals( kss ) || KieScannerStatus.STARTED.equals( kss )) {
            kci.getScanner().scanNow();
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Scan successfully executed.",
                    getScannerResource(kci));
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: " + kss,
                    getScannerResource(kci));
        }
    }

    private ServiceResponse<KieScannerResource> disposeScanner(String id, KieScannerResource resource, KieContainerInstanceImpl kci) {
        if (kci.getScanner() == null) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Invalid call. Scanner already disposed. ",
                    getScannerResource(kci));
        }
        if (KieScannerStatus.STARTED.equals(mapStatus(kci.getScanner().getStatus())) ||
                KieScannerStatus.SCANNING.equals(mapStatus(kci.getScanner().getStatus()))) {
            ServiceResponse<KieScannerResource> response = stopScanner(id, resource, kci);
            if (ResponseType.FAILURE.equals(response.getType())) {
                return response;
            }
        }
        kci.getScanner().shutdown();
        kci.setScanner(null);
        return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                "Kie scanner successfully shutdown.",
                getScannerResource(kci));
    }

    private ServiceResponse<KieScannerResource> createScanner(String id, KieContainerInstanceImpl kci) {
        if (kci.getScanner() == null) {
            InternalKieScanner scanner = (InternalKieScanner) KieServices.Factory.get().newKieScanner(kci.getKieContainer());
            kci.setScanner(scanner);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfully created.",
                    getScannerResource(kci));
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Error creating the scanner for container " + id + ". Scanner already exists.");

        }
    }

    public ServiceResponse<ReleaseId> getContainerReleaseId(String id) {
        try {
            KieContainerInstanceImpl ci = context.getContainer(id);
            if (ci != null) {
                return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.SUCCESS, "ReleaseId for container " + id, ci.getResource().getReleaseId());
            }
            return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Container " + id + " is not instantiated.");
        } catch (Exception e) {
            logger.error("Error retrieving releaseId for container '" + id + "'", e);
            return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Error retrieving container releaseId: " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<ReleaseId> updateContainerReleaseId(String id, ReleaseId releaseId) {
        if( releaseId == null ) {
            logger.error("Error updating releaseId for container '" + id + "'. ReleaseId is null.");
            return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Error updating releaseId for container " + id + ". ReleaseId is null. ");
        }
        try {
            KieContainerInstanceImpl kci = context.getContainer(id);
            // the following code is subject to a concurrent call to dispose(), but the cost of synchronizing it
            // would likely not be worth it. At this point a decision was made to fail the execution if a concurrent 
            // call do dispose() is executed.
            if (kci != null && kci.getKieContainer() != null) {
                Results results = kci.getKieContainer().updateToVersion(releaseId);
                if (results.hasMessages(Level.ERROR)) {
                    logger.error("Error updating releaseId for container " + id + " to version " + releaseId + "\nMessages: " + results.getMessages());
                    return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Error updating release id on container " + id + " to " + releaseId, kci.getResource().getReleaseId());
                } else {
                    return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.SUCCESS, "Release id successfully updated.", kci.getResource().getReleaseId());
                }
            } else {
                return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Container " + id + " is not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error updating releaseId for container '" + id + "'", e);
            return new ServiceResponse<ReleaseId>(ServiceResponse.ResponseType.FAILURE, "Error updating releaseId for container " + id + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private KieScannerStatus mapStatus(InternalKieScanner.Status status) {
        switch (status) {
            case STARTING:
                return KieScannerStatus.CREATED;
            case RUNNING:
                return KieScannerStatus.STARTED;
            case SCANNING:
            case UPDATING:
                return KieScannerStatus.SCANNING;
            case STOPPED:
                return KieScannerStatus.STOPPED;
            case SHUTDOWN:
                return KieScannerStatus.DISPOSED;
            default:
                return KieScannerStatus.UNKNOWN;
        }
    }

    private <T> T makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).get();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            KieContainerResourceList serviceResponse = deserialize( response.body(), KieContainerResourceList.class );

            return (T)serviceResponse;
        } else {
            throw new IllegalStateException("No response from controller server");
        }
    }

    private KieRemoteHttpRequest newRequest(String uri) {
        KieRemoteHttpRequest httpRequest =
                KieRemoteHttpRequest.newRequest( uri ).followRedirects( true ).timeout( 5000 );
        httpRequest.accept(MediaType.APPLICATION_JSON);
        httpRequest.basicAuthorization( KieServerEnvironment.getUsername(), KieServerEnvironment.getPassword() );

        return httpRequest;
    }

    private <T> T deserialize(String content, Class<T> type) {
        try {
            return MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader()).unmarshall(content, type);
        } catch ( MarshallingException e ) {
            throw new IllegalStateException( "Error while deserializing data received from server!", e );
        }
    }

}
