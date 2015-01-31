package org.kie.server.api.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "kie-container")
@XStreamAlias( "kie-container" )
public class KieContainerResource {

    @XStreamAlias( "container-id" )
    private String             containerId;

    @XStreamAlias( "release-id" )
    private ReleaseId          releaseId;

    @XStreamAlias( "resolved-release-id" )
    private ReleaseId          resolvedReleaseId;

    @XStreamAlias( "status" )
    private KieContainerStatus status;

    @XStreamAlias( "scanner" )
    private KieScannerResource scanner;

    public KieContainerResource() {
    }

    public KieContainerResource(ReleaseId releaseId) {
        this( null, releaseId, null, null );
    }
    
    public KieContainerResource(String containerId, ReleaseId releaseId) {
        this( containerId, releaseId, null, null );
    }
    
    public KieContainerResource(String containerId, ReleaseId releaseId, KieContainerStatus status) {
        this( containerId, releaseId, null, status );
    }
    
    public KieContainerResource(String containerId, ReleaseId releaseId, ReleaseId resolvedReleaseId, KieContainerStatus status) {
        this.containerId = containerId;
        this.releaseId = releaseId;
        this.resolvedReleaseId = resolvedReleaseId;
        this.status = status;
    }

    @XmlAttribute(name = "container-id")
    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @XmlAttribute(name = "status")
    public KieContainerStatus getStatus() {
        return status;
    }

    public void setStatus(KieContainerStatus status) {
        this.status = status;
    }

    @XmlElement(name = "release-id")
    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    @XmlElement(name = "resolved-release-id")
    public ReleaseId getResolvedReleaseId() {
        return resolvedReleaseId;
    }

    public void setResolvedReleaseId(ReleaseId resolvedReleaseId) {
        this.resolvedReleaseId = resolvedReleaseId;
    }
    
    @XmlElement
    public KieScannerResource getScanner() {
        return scanner;
    }
    
    public void setScanner(KieScannerResource scanner) {
        this.scanner = scanner;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
        result = prime * result + ((releaseId == null) ? 0 : releaseId.hashCode());
        result = prime * result + ((resolvedReleaseId == null) ? 0 : resolvedReleaseId.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KieContainerResource other = (KieContainerResource) obj;
        if (containerId == null) {
            if (other.containerId != null)
                return false;
        } else if (!containerId.equals(other.containerId))
            return false;
        if (releaseId == null) {
            if (other.releaseId != null)
                return false;
        } else if (!releaseId.equals(other.releaseId))
            return false;
        if (resolvedReleaseId == null) {
            if (other.resolvedReleaseId != null)
                return false;
        } else if (!resolvedReleaseId.equals(other.resolvedReleaseId))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KieContainerResource [containerId=" + containerId + ", releaseId=" + releaseId + ", resolvedReleaseId=" + resolvedReleaseId + ", status=" + status + "]";
    }

}