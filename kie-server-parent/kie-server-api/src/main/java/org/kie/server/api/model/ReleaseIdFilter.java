/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enables filtering of {@link ReleaseId}s based on provided constraints (groupId, artifactId, version).
 * <p>
 * This implementation compares the expected parts for equality (e.g. the filter accepts the
 * releaseId in case the groupId/artifactId/version exactly matches the configured one). It is also possible
 * to filter only on a subset of the constraints (e.g. only on groupId).
 * <p>
 * The class comes with {@link Builder} which can be used to easily create all kinds of filters.
 */
@XmlRootElement(name = "release-id-filter")
@XStreamAlias("release-id-filter")
@XmlAccessorType(XmlAccessType.NONE)
public class ReleaseIdFilter {
    @XmlElement(name = "group-id")
    @XStreamAlias("group-id")
    private final String groupId;
    @XmlElement(name = "artifact-id")
    @XStreamAlias("artifact-id")
    private final String artifactId;
    @XmlElement(name = "version")
    @XStreamAlias("version")
    private final String version;

    public static final ReleaseIdFilter ACCEPT_ALL = new ReleaseIdFilter(null, null, null);

    private ReleaseIdFilter() {
        // passing NULLs is a bad practice, but JAXB needs no-arg constructor
        this(null, null, null);
    }

    public ReleaseIdFilter(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public ReleaseIdFilter(ReleaseId releaseId) {
        this(releaseId.getGroupId(), releaseId.getArtifactId(),releaseId.getVersion());
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Checks whether the specified releaseId matches (is accepted by) this filter.
     *
     * @param releaseId releaseId to match against
     * @return true if this filter accepts the specified releaseId, otherwise false
     */
    public boolean accept(ReleaseId releaseId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("ReleaseId can not be null!");
        }
        return matches(groupId, releaseId.getGroupId()) &&
                matches(artifactId, releaseId.getArtifactId()) &&
                matches(version, releaseId.getVersion());
    }

    /**
     * Checks whether the string filter matches the provided value.
     * <p>
     * Filter can be null, which means "match all".
     *
     * @param filter string filter
     * @param value  value to match against
     * @return true if the filter matches the value, otherwise false
     */
    private boolean matches(String filter, String value) {
        if (filter == null) {
            return true;
        }
        return filter.equals(value);
    }

    @Override
    public String toString() {
        return "ReleaseIdFilter{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReleaseIdFilter that = (ReleaseIdFilter) o;

        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) {
            return false;
        }
        if (artifactId != null ? !artifactId.equals(that.artifactId) : that.artifactId != null) {
            return false;
        }
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String groupId;
        private String artifactId;
        private String version;

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder releaseId(ReleaseId releaseId) {
            this.groupId = releaseId.getGroupId();
            this.artifactId = releaseId.getArtifactId();
            this.version = releaseId.getVersion();
            return this;
        }

        public ReleaseIdFilter build() {
            return new ReleaseIdFilter(groupId, artifactId, version);
        }
    }

}
