package org.kie.server.api;

public class Version {

    private final int    major;
    private final int    minor;
    private final int    revision;
    private final String classifier;

    public Version(int major, int minor, int revision, String classifier) {
        super();
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.classifier = classifier;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public String getClassifier() {
        return classifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + revision;
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
        Version other = (Version) obj;
        if (classifier == null) {
            if (other.classifier != null)
                return false;
        } else if (!classifier.equals(other.classifier))
            return false;
        if (major != other.major)
            return false;
        if (minor != other.minor)
            return false;
        if (revision != other.revision)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + revision + (classifier != null ? classifier : "");
    }

}
