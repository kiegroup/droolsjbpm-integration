package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.Version;
import org.kie.server.api.commands.*;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.rest.KieServerRestImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerImpl {

    private static final String             CONTAINER_STATE_FILE = "container.xml";
    private static final Pattern            LOOKUP               = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
    private static final Logger             logger               = LoggerFactory.getLogger(KieServerRestImpl.class);

    private final KieContainersRegistryImpl context;

    public KieServerImpl() {
        this.context = new KieContainersRegistryImpl();
    }

    public ServiceResponsesList executeScript(CommandScript commands) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();
        if( commands != null ) {
            for (KieServerCommand command : commands.getCommands()) {
                if (command instanceof CreateContainerCommand) {
                    responses.add(createContainer(((CreateContainerCommand) command).getContainer().getContainerId(), ((CreateContainerCommand) command).getContainer()));
                } else if (command instanceof GetServerInfoCommand) {
                    responses.add(getInfo());
                } else if (command instanceof ListContainersCommand) {
                    responses.add(listContainers());
                } else if (command instanceof CallContainerCommand) {
                    responses.add(callContainer(((CallContainerCommand) command).getContainerId(), ((CallContainerCommand) command).getPayload()));
                } else if (command instanceof DisposeContainerCommand) {
                    responses.add(disposeContainer(((DisposeContainerCommand) command).getContainerId()));
                } else if (command instanceof GetContainerInfoCommand ) {
                    responses.add(getContainerInfo(((GetContainerInfoCommand) command).getContainerId()));
                } else if (command instanceof GetScannerInfoCommand ) {
                    responses.add(getScannerInfo(((GetScannerInfoCommand) command).getContainerId()));
                } else if (command instanceof UpdateScannerCommand ) {
                    responses.add(updateScanner(((UpdateScannerCommand) command).getContainerId(), ((UpdateScannerCommand) command).getScanner()));
                } else if (command instanceof UpdateReleaseIdCommand ) {
                    responses.add(updateContainerReleaseId(((UpdateReleaseIdCommand) command).getContainerId(), ((UpdateReleaseIdCommand) command).getReleaseId()));
                }
            }
        }
        return new ServiceResponsesList(responses);
    }

    public ServiceResponse<KieServerInfo> getInfo() {
        try {
            Version version = KieServerEnvironment.getVersion();
            String versionStr = version != null ? version.toString() : "Unknown-Version";
            return new ServiceResponse<KieServerInfo>(ServiceResponse.ResponseType.SUCCESS, "Kie Server info", new KieServerInfo(versionStr));
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
            KieContainerInstance ci = new KieContainerInstance(containerId, KieContainerStatus.CREATING);
            KieContainerInstance previous = null;
            // have to synchronize on the ci or a concurrent call to dispose may create inconsistencies
            synchronized (ci) {
                previous = context.addIfDoesntExist(containerId, ci);
                if (previous == null) {
                    try {
                        KieServices ks = KieServices.Factory.get();
                        InternalKieContainer kieContainer = (InternalKieContainer) ks.newKieContainer(releaseId);
                        if (kieContainer != null) {
                            ci.setKieContainer(kieContainer);
                            ci.getResource().setStatus(KieContainerStatus.STARTED);
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
            for (KieContainerInstance instance : context.getContainers()) {
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
            KieContainerInstance ci = context.getContainer(id);
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

    public ServiceResponse<String> callContainer(String containerId, String payload) {
        if( payload == null ) {
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ". Empty payload. ");
        }
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainer( containerId );
            // the following code is subject to a concurrent call to dispose(), but the cost of synchronizing it
            // would likely not be worth it. At this point a decision was made to fail the execution if a concurrent 
            // call do dispose() is executed.
            if (kci != null && kci.getKieContainer() != null) {
                String sessionId = null;
                // this is a weak way of finding the lookup, but it is the same used in kie-camel. Will keep it for now. 
                Matcher m = LOOKUP.matcher(payload);
                if (m.find()) {
                    sessionId = m.group(1);
                }

                // find the session
                CommandExecutor ks = null;
                if( sessionId != null ) {
                    KieSessionModel ksm = kci.getKieContainer().getKieSessionModel(sessionId);
                    if( ksm != null ) {
                        switch (ksm.getType() ) {
                            case STATEFUL:
                                ks = kci.getKieContainer().getKieSession(sessionId);
                                break;
                            case STATELESS:
                                ks = kci.getKieContainer().getStatelessKieSession(sessionId);
                                break;
                        }
                    }
                } else {
                    // if no session ID is defined, then the default is a stateful session
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    Command<?> cmd = kci.getMarshaller( MarshallingFormat.XSTREAM ).unmarshall(payload, Command.class);

                    if (cmd == null) {
                        return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    String result = kci.getMarshaller( MarshallingFormat.XSTREAM ).marshall(results);
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
                } else {
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error calling container '" + containerId + "'", e);
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public ServiceResponse<Void> disposeContainer(String containerId) {
        try {
            KieContainerInstance kci = (KieContainerInstance) context.removeContainer(containerId);
            if (kci != null) {
                synchronized (kci) {
                    kci.setStatus(KieContainerStatus.DISPOSING); // just in case
                    if (kci.getKieContainer() != null) {
                        InternalKieContainer kieContainer = kci.getKieContainer();
                        kci.setKieContainer(null); // helps reduce concurrent access issues
                        try {
                            // this may fail, but we already removed the container from the registry
                            kieContainer.dispose();
                        } catch (Exception e) {
                            logger.warn("Container '" + containerId + "' disposed, but an unexpected exception was raised", e);
                            return new ServiceResponse<Void>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId +
                                    " disposed, but exception was raised: " + e.getClass().getName() + ": " + e.getMessage());
                        }
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
            KieContainerInstance kci = context.getContainer(id);
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

    private KieScannerResource getScannerResource(KieContainerInstance kci) {
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
            KieContainerInstance kci = context.getContainer(id);
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

    private ServiceResponse<KieScannerResource> startScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
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

    private ServiceResponse<KieScannerResource> stopScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
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

    private ServiceResponse<KieScannerResource> scanNow(String id, KieScannerResource resource, KieContainerInstance kci) {
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

    private ServiceResponse<KieScannerResource> disposeScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
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

    private ServiceResponse<KieScannerResource> createScanner(String id, KieContainerInstance kci) {
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
            KieContainerInstance ci = context.getContainer(id);
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
            KieContainerInstance kci = context.getContainer(id);
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

    public static class KieContainersRegistryImpl implements KieContainersRegistry {

        private final ConcurrentMap<String, KieContainerInstance> containers;

        public KieContainersRegistryImpl() {
            this.containers = new ConcurrentHashMap<String, KieContainerInstance>();
        }

        public KieContainerInstance addIfDoesntExist(String containerId, KieContainerInstance ci) {
            synchronized ( containers ) {
                KieContainerInstance kci = containers.putIfAbsent(containerId, ci);
                if( kci != null && kci.getStatus() == KieContainerStatus.FAILED ) {
                    // if previous container failed, allow override
                    containers.put(containerId, ci);
                    return null;
                }
                return kci;
            }
        }

        public List<KieContainerInstance> getContainers() {
            // instantiating a new array list to prevent iteration problems when concurrently changing the map 
            return new ArrayList<KieContainerInstance>(containers.values());
        }

        public KieContainerInstance getContainer(String containerId) {
            return containers.get(containerId);
        }

        public KieContainerInstance removeContainer(String containerId) {
            return containers.remove(containerId);
        }
    }

}
