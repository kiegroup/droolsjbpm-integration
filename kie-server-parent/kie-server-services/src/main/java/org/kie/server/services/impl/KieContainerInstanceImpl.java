package org.kie.server.services.impl;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.api.KieContainerInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KieContainerInstanceImpl implements KieContainerInstance {

    private KieContainerResource               resource;
    private InternalKieContainer               kieContainer;
    private InternalKieScanner                 scanner;
    private Map<MarshallingFormat, Marshaller> marshallers;

    private Map<String, Object> serviceContainer;

    public KieContainerInstanceImpl(String containerId, KieContainerStatus status) {
        this( containerId, status, null );
    }

    public KieContainerInstanceImpl(String containerId, KieContainerStatus status, InternalKieContainer kieContainer) {
        super();
        this.kieContainer = kieContainer;
        this.resource = new KieContainerResource( containerId, null, status );
        this.marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>();
        this.serviceContainer = new ConcurrentHashMap<String, Object>();
        updateReleaseId();
    }

    public String getContainerId() {
        return resource.getContainerId();
    }

    public void setContainerId(String containerId) {
        this.resource.setContainerId( containerId );
    }

    public InternalKieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(InternalKieContainer kieContainer) {
        this.kieContainer = kieContainer;
        updateReleaseId();
    }

    public KieContainerStatus getStatus() {
        return resource.getStatus();
    }

    public void setStatus(KieContainerStatus status) {
        this.resource.setStatus( status );
    }

    public KieContainerResource getResource() {
        updateReleaseId();
        return resource;
    }

    public void setResource(KieContainerResource resource) {
        this.resource = resource;
    }

    public void setScanner(InternalKieScanner scanner) {
        this.scanner = scanner;
    }

    public InternalKieScanner getScanner() {
        return this.scanner;
    }

    private void updateReleaseId() {
        if ( kieContainer != null ) {
            this.resource.setReleaseId( new ReleaseId( kieContainer.getContainerReleaseId() ) );
            this.resource.setResolvedReleaseId( new ReleaseId( kieContainer.getReleaseId() ) );
        }
        disposeMarshallers();
    }

    public Marshaller getMarshaller(MarshallingFormat format) {
        synchronized ( marshallers ) {
            Marshaller marshaller = marshallers.get( format );
            if ( marshaller == null ) {
                marshaller = MarshallerFactory.getMarshaller( format, this.kieContainer.getClassLoader() );
                this.marshallers.put( format, marshaller );
            }
            return marshaller;
        }
    }

    public void disposeMarshallers() {
        synchronized ( marshallers ) {
            for ( Marshaller marshaller : this.marshallers.values() ) {
                marshaller.dispose();
            }
            this.marshallers.clear();
        }
    }

    @Override
    public void addService(Object service) {
        if (service == null) {
            return;
        }
        if (serviceContainer.containsKey(service.getClass().getName())) {
            throw new IllegalStateException("Service " + service.getClass().getName() + " already exists");
        }
        serviceContainer.put(service.getClass().getName(), service);
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        return (T) this.serviceContainer.get(serviceType.getName());
    }

    @Override
    public <T> T removeService(Class<T> serviceType) {
        return (T) this.serviceContainer.remove(serviceType.getName());
    }

    @Override
    public String toString() {
        return resource.toString();
    }

}
