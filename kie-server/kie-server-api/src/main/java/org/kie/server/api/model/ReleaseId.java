package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a JAXB friendly ReleaseId implementation
 * used for JAXB marshalling/unmarshalling only 
 */
@XmlRootElement
public class ReleaseId implements org.kie.api.builder.ReleaseId {

    private String groupId;
    private String artifactId;
    private String version;

    public ReleaseId() {
        super();
    }
    
    public ReleaseId( org.kie.api.builder.ReleaseId releaseId ) {
        this( releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion() );
    }

    public ReleaseId(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @XmlElement(required = true, name = "group-id")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @XmlElement(required = true, name = "artifact-id")
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @XmlElement(required = true, name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isSnapshot() {
        return version.endsWith("-SNAPSHOT");
    }

    public String toExternalForm() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public String toString() {
        return toExternalForm();
    }
    
    

}
