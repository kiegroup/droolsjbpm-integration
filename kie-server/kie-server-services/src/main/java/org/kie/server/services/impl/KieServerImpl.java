package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.Version;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.ListContainersCommand;
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
import org.kie.server.services.api.KieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

@Path("/server")
public class KieServerImpl implements KieServer {

    private static final Pattern        LOOKUP = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
    private static final Logger         logger = LoggerFactory.getLogger(KieServerImpl.class);

    private final KieContainersRegistry context;

    public KieServerImpl() {
        this.context = new KieContainersRegistryImpl();
    }

    @Override
    public Response getInfo() {
        return Response.ok(getInfo(context)).build();
    }

    @Override
    public Response execute(CommandScript command) {
        return Response.ok(new GenericEntity<List<ServiceResponse<? extends Object>>>(executeScript(context, command)) {
        }).build();
    }

    @Override
    public Response listContainers() {
        return Response.ok(listContainers(context)).build();
    }

    @Override
    public Response createContainer(String id, KieContainerResource container) {
        return Response.status(Status.CREATED).entity(createContainer(context, id, container)).build();
    }

    @Override
    public Response getContainerInfo(String id) {
        return Response.ok(getContainerInfo(context, id)).build();
    }

    @Override
    public Response disposeContainer(String id) {
        return Response.ok(disposeContainer(context, id)).build();
    }

    @Override
    public Response execute(String id, String cmdPayload) {
        return Response.ok(callContainer(context, id, cmdPayload)).build();
    }
    
    @Override
    public Response getScannerInfo(String id) {
        return Response.ok(getScannerInfo(context, id)).build();
    }

    @Override
    public Response updateScanner(String id, KieScannerResource resource) {
        return Response.ok(updateScanner(context, id, resource)).build();
    };
    
    private List<ServiceResponse<? extends Object>> executeScript(KieContainersRegistry context, CommandScript commands) {
        List<ServiceResponse<? extends Object>> response = new ArrayList<ServiceResponse<? extends Object>>();
        for (KieServerCommand command : commands.getCommands()) {
            if (command instanceof CreateContainerCommand) {
                response.add(createContainer(context, ((CreateContainerCommand) command).getContainer().getContainerId(), ((CreateContainerCommand) command).getContainer()));
            } else if (command instanceof ListContainersCommand) {
                response.add(listContainers(context));
            } else if (command instanceof CallContainerCommand) {
                response.add(callContainer(context, ((CallContainerCommand) command).getContainerId(), ((CallContainerCommand) command).getPayload()));
            } else if (command instanceof DisposeContainerCommand) {
                response.add(disposeContainer(context, ((DisposeContainerCommand) command).getContainerId()));
            }
        }
        return response;
    }

    private ServiceResponse<KieServerInfo> getInfo(KieContainersRegistry context) {
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

    private ServiceResponse<KieContainerResource> createContainer(KieContainersRegistry context, String containerId, KieContainerResource container) {
        ReleaseId releaseId = container.getReleaseId();
        if( releaseId == null ) {
            logger.error("Error creating container. Release Id is null: "+container);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Failed to create container " + containerId + ". Release Id is null: " + container + ".");
        }
        try {
            KieContainerInstance ci = new KieContainerInstance(containerId, KieContainerStatus.CREATING);
            KieContainerInstance previous = null;
            // have to synchronize on the ci or a concurrent call to dispose may create inconsistencies
            synchronized (ci) {
                if ((previous = context.addIfDoesntExist(containerId, ci)) == null) {
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
                        logger.error("Error creating container '"+containerId+"' for module '"+releaseId+"'", e);
                        ci.getResource().setStatus(KieContainerStatus.FAILED);
                        return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Failed to create container " + containerId + " with module " + releaseId + ": " + e.getClass().getName() + ": " + e.getMessage());
                    }
                } else {
                    return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " already exists.", previous.getResource());
                }
            }
        } catch (Exception e) {
            logger.error("Error creating container '"+containerId+"' for module '"+releaseId+"'", e);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Error creating container " + containerId +
                    " with module " + releaseId + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<KieContainerResourceList> listContainers(KieContainersRegistry context) {
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

    private ServiceResponse<KieContainerResource> getContainerInfo(KieContainersRegistry context, String id) {
        try {
            KieContainerInstance ci = context.getContainer(id);
            if (ci != null) {
                return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.SUCCESS, "Info for container " + id, ci.getResource());
            }
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Container " + id + " is not instantiated.");
        } catch (Exception e) {
            logger.error("Error retrieving info for container '"+id+"'", e);
            return new ServiceResponse<KieContainerResource>(ServiceResponse.ResponseType.FAILURE, "Error retrieving container info: " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<String> callContainer(KieContainersRegistry context, String containerId, String payload) {
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainer(containerId);
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

                KieSession ks = null;
                if (sessionId != null) {
                    ks = kci.getKieContainer().getKieSession(sessionId);
                } else {
                    ks = kci.getKieContainer().getKieSession();
                }
                if (ks != null) {
                    ClassLoader moduleClassLoader = kci.getKieContainer().getClassLoader();
                    XStream xs = XStreamXml.newXStreamMarshaller(moduleClassLoader);
                    Command<?> cmd = (Command<?>) xs.fromXML(payload);

                    if (cmd == null) {
                        return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                    }
                    if (!(cmd instanceof BatchExecutionCommandImpl)) {
                        cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                    }

                    ExecutionResults results = ks.execute((BatchExecutionCommandImpl) cmd);
                    String result = xs.toXML(results);
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
                } else {
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Session '" + sessionId + "' not found on container '" + containerId + "'.");
                }
            } else {
                return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }
        } catch (Exception e) {
            logger.error("Error calling container '"+containerId+"'", e);
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<Void> disposeContainer(KieContainersRegistry context, String containerId) {
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
                            logger.warn("Container '"+containerId+"' disposed, but an unnexpected exception was raised", e);
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
            logger.error("Error disposing Container '"+containerId+"'", e);
            return new ServiceResponse<Void>(ServiceResponse.ResponseType.FAILURE, "Error disposing container " + containerId + ": " +
                    e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    private ServiceResponse<KieScannerResource> getScannerInfo(KieContainersRegistry context, String id) {
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainer(id);
            if (kci != null && kci.getKieContainer() != null) {
                InternalKieScanner scanner = kci.getScanner();
                KieScannerResource info = null;
                if( scanner != null ) {
                    info = new KieScannerResource(mapStatus(scanner.getStatus()));
                } else {
                    info = new KieScannerResource(KieScannerStatus.DISPOSED);
                }
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS, "Scanner info successfully retrieved", info ); 
            } else {
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, 
                        "Unknown container "+id+".");
            }
        } catch (Exception e) {
            logger.error("Error retrieving scanner info for container '"+id+"'.", e);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error retrieving scanner info for container '"+id+"': " + 
                         e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<KieScannerResource> updateScanner(KieContainersRegistry context, String id, KieScannerResource resource) {
        KieScannerStatus status = resource.getStatus();
        if( status == null ) {
            logger.error("Error updating scanner for container "+id+". Status is null: "+resource);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error updating scanner for container "+id+". Status is null: "+resource);
        }
        try {
            KieContainerInstance kci = (KieContainerInstance) context.getContainer(id);
            if (kci != null && kci.getKieContainer() != null) {
                switch( status ) {
                    case CREATED:
                        // create the scanner
                        return createScanner(id, kci);
                    case STARTED:
                        // start the scanner
                        return startScanner(id, resource, kci);
                    case STOPPED:
                        // stop the scanner
                        return stopScanner(id, resource, kci);
                    case SCANNING:
                        // scan now
                        return scanNow(id, resource, kci);
                    case DISPOSED:
                        // dispose
                        return disposeScanner(id, resource, kci);
                    default:
                        // error
                        return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, 
                                "Unknown status '"+status+"' for scanner on container "+id+".");
                }
            } else {
                return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, 
                        "Unknown container "+id+".");
            }
        } catch (Exception e) {
            logger.error("Error updating scanner for container '"+id+"': "+resource, e);
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, "Error updating scanner for container '"+id+
                    "': "+resource + ": " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private ServiceResponse<KieScannerResource> startScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
        if( kci.getScanner() == null ) {
            ServiceResponse<KieScannerResource> response = createScanner(id, kci);
             if( ResponseType.FAILURE.equals( response.getType() ) ) {
                 return response;
             }
        }
        if( KieScannerStatus.STOPPED.equals( mapStatus( kci.getScanner().getStatus() ) ) &&
            resource.getPollInterval() != null ) {
            kci.getScanner().start( resource.getPollInterval() );
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfuly created.",
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ) );
        } else if(!KieScannerStatus.STOPPED.equals( mapStatus( kci.getScanner().getStatus() ) )) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: "+mapStatus( kci.getScanner().getStatus() ),
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ));
        } else if( resource.getPollInterval() == null ) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid polling interval: "+resource.getPollInterval(),
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ));
        }
        return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                "Unknown error starting scanner. Scanner was not started."+resource,
                new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ));
    }

    private ServiceResponse<KieScannerResource> stopScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
        if( kci.getScanner() == null ) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid call. Scanner is not instantiated. ",
                    new KieScannerResource( KieScannerStatus.DISPOSED ));
        }
        if( KieScannerStatus.STARTED.equals( mapStatus( kci.getScanner().getStatus() ) ) ||
            KieScannerStatus.SCANNING.equals( mapStatus( kci.getScanner().getStatus() ) ) ) {
            kci.getScanner().stop();
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfuly stopped.",
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ) );
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: "+mapStatus( kci.getScanner().getStatus() ),
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ));
        }
    }

    private ServiceResponse<KieScannerResource> scanNow(String id, KieScannerResource resource, KieContainerInstance kci) {
        if( kci.getScanner() == null ) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid call. Scanner is not instantiated. ",
                    new KieScannerResource( KieScannerStatus.DISPOSED ));
        }
        if( KieScannerStatus.STOPPED.equals( mapStatus( kci.getScanner().getStatus() ) ) ) {
            kci.getScanner().scanNow();
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Scan successfully executed.",
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ) );
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE,
                    "Invalid kie scanner status: "+mapStatus( kci.getScanner().getStatus() ),
                    new KieScannerResource( mapStatus( kci.getScanner().getStatus() ) ));
        }
    }

    private ServiceResponse<KieScannerResource> disposeScanner(String id, KieScannerResource resource, KieContainerInstance kci) {
        if( kci.getScanner() == null ) {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Invalid call. Scanner already disposed. ",
                    new KieScannerResource( KieScannerStatus.DISPOSED ));
        }
        if( KieScannerStatus.STARTED.equals( mapStatus( kci.getScanner().getStatus() ) ) ||
            KieScannerStatus.SCANNING.equals( mapStatus( kci.getScanner().getStatus() ) ) ) {
            ServiceResponse<KieScannerResource> response = stopScanner(id, resource, kci);
            if( ResponseType.FAILURE.equals( response.getType() ) ) {
                return response;
            }
        }
        kci.getScanner().shutdown();
        kci.setScanner(null);
        return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                "Kie scanner successfuly shutdown.",
                new KieScannerResource( KieScannerStatus.DISPOSED ) );
    }

    private ServiceResponse<KieScannerResource> createScanner(String id, KieContainerInstance kci) {
        if( kci.getScanner() == null ) {
            InternalKieScanner scanner = (InternalKieScanner) KieServices.Factory.get().newKieScanner(kci.getKieContainer());
            kci.setScanner( scanner );
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.SUCCESS,
                    "Kie scanner successfuly created.",
                    new KieScannerResource( mapStatus( scanner.getStatus() ) ) );
        } else {
            return new ServiceResponse<KieScannerResource>(ServiceResponse.ResponseType.FAILURE, 
                    "Error creating the scanner for container "+id+". Scanner already exists.");
            
        }
    }
    
    private KieScannerStatus mapStatus( InternalKieScanner.Status status ) {
        switch( status ) {
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
            return containers.putIfAbsent(containerId, ci);
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
