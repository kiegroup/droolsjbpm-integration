package org.kie.server.services.impl;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.drools.compiler.kie.builder.impl.InternalKieScanner;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;

public class KieContainerInstance {

    private KieContainerResource resource;
    private InternalKieContainer kieContainer;
    private InternalKieScanner   scanner;

    public KieContainerInstance(String containerId, KieContainerStatus status) {
        this(containerId, status, null);
    }

    public KieContainerInstance(String containerId, KieContainerStatus status, InternalKieContainer kieContainer) {
        super();
        this.resource = new KieContainerResource(containerId, kieContainer != null ? new ReleaseId(kieContainer.getContainerReleaseId()) : null, status);
        this.kieContainer = kieContainer;
    }

    public String getContainerId() {
        return resource.getContainerId();
    }

    public void setContainerId(String containerId) {
        this.resource.setContainerId(containerId);
    }

    public InternalKieContainer getKieContainer() {
        return kieContainer;
    }

    public void setKieContainer(InternalKieContainer kieContainer) {
        this.kieContainer = kieContainer;
        if (kieContainer != null) {
            this.resource.setReleaseId(new ReleaseId(kieContainer.getContainerReleaseId()));
            this.resource.setResolvedReleaseId(new ReleaseId(kieContainer.getReleaseId()));
        }
    }

    public KieContainerStatus getStatus() {
        return resource.getStatus();
    }

    public void setStatus(KieContainerStatus status) {
        this.resource.setStatus(status);
    }

    public KieContainerResource getResource() {
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

    @Override
    public String toString() {
        return resource.toString();
    }

}
