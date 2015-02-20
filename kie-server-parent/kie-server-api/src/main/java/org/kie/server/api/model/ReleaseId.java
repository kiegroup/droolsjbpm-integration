package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * This is a JAXB friendly ReleaseId implementation
 * used for JAXB marshalling/unmarshalling only 
 */
@XmlRootElement(name="release-id")
@XStreamAlias( "release-id" )
@JsonIgnoreProperties({"snapshot"})
public class ReleaseId implements org.kie.api.builder.ReleaseId {

    @XStreamAlias( "group-id" )
    private String groupId;

    @XStreamAlias( "artifact-id" )
    private String artifactId;

    @XStreamAlias( "version" )
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        ReleaseId other = (ReleaseId) obj;
        if (artifactId == null) {
            if (other.artifactId != null)
                return false;
        } else if (!artifactId.equals(other.artifactId))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
