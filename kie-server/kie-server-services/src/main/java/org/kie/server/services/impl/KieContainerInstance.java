package org.kie.server.services.impl;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.marshalling.Marshaller;
import org.kie.server.services.marshalling.MarshallerFactory;
import org.kie.server.services.marshalling.MarshallingFormat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KieContainerInstance {

    private KieContainerResource               resource;
    private InternalKieContainer               kieContainer;
    private InternalKieScanner                 scanner;
    private Map<MarshallingFormat, Marshaller> marshallers;

    public KieContainerInstance(String containerId, KieContainerStatus status) {
        this( containerId, status, null );
    }

    public KieContainerInstance(String containerId, KieContainerStatus status, InternalKieContainer kieContainer) {
        super();
        this.kieContainer = kieContainer;
        this.resource = new KieContainerResource( containerId, null, status );
        this.marshallers = new ConcurrentHashMap<MarshallingFormat, Marshaller>();
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
    public String toString() {
        return resource.toString();
    }

}
