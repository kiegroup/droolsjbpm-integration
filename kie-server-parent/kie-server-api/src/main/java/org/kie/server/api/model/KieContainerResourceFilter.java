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
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@XmlRootElement(name = "kie-container-filter")
@XStreamAlias("kie-container-filter")
@XmlAccessorType(XmlAccessType.NONE)
public class KieContainerResourceFilter {

    public static final KieContainerResourceFilter ACCEPT_ALL = new KieContainerResourceFilter();

    @XmlElement(name = "release-id-filter")
    @XStreamAlias("release-id-filter")
    private final ReleaseIdFilter releaseIdFilter;

    @XmlElement(name = "container-status-filter")
    @XStreamAlias("container-status-filter")
    private final KieContainerStatusFilter statusFilter;

    private KieContainerResourceFilter() {
        // needed for JAXB
        this.releaseIdFilter = ReleaseIdFilter.ACCEPT_ALL;
        this.statusFilter = KieContainerStatusFilter.ACCEPT_ALL;
    }

    public KieContainerResourceFilter(ReleaseIdFilter releaseIdFilter, KieContainerStatusFilter statusFilter) {
        this.releaseIdFilter = releaseIdFilter;
        this.statusFilter = statusFilter;
    }

    public KieContainerResourceFilter(ReleaseIdFilter releaseIdFilter) {
        this(releaseIdFilter, KieContainerStatusFilter.ACCEPT_ALL);
    }

    public ReleaseIdFilter getReleaseIdFilter() {
        return releaseIdFilter;
    }

    public KieContainerStatusFilter getStatusFilter() {
        return statusFilter;
    }

    public boolean accept(KieContainerResource kieContainerResource) {
        if (kieContainerResource == null) {
            throw new IllegalArgumentException("KieContainerResource can not be null!");
        }
        // in case resolved release id exists, check against that
        ReleaseId resolvedReleaseId = kieContainerResource.getResolvedReleaseId();
        if (resolvedReleaseId != null) {
            if (!releaseIdFilter.accept(resolvedReleaseId)) {
                return false;
            }
        } else {
            if (!releaseIdFilter.accept(kieContainerResource.getReleaseId())) {
                return false;
            }
        }
        KieContainerStatus status = kieContainerResource.getStatus();
        if (status != null && !statusFilter.accept(status)) {
            return false;
        }
        // all sub-filters accepted the container, so it is a match
        return true;
    }

    /**
     * Creates representation of this filter which can be used as part of the URL (e.g. the "?" query part).
     *
     * @return string representation that can be directly used in URL (as query params), without the leading '?'
     */
    public String toURLQueryString() {
        StringJoiner joiner = new StringJoiner("&");
        if (releaseIdFilter.getGroupId() != null) {
            joiner.add("groupId=" + releaseIdFilter.getGroupId());
        }
        if (releaseIdFilter.getArtifactId() != null) {
            joiner.add("artifactId=" + releaseIdFilter.getArtifactId());
        }
        if (releaseIdFilter.getVersion() != null) {
            joiner.add("version=" + releaseIdFilter.getVersion());
        }
        // don't send over the default status filter (e.g. one that accepts all the states) as it is not needed, it is
        // the default
        if (!statusFilter.equals(KieContainerStatusFilter.ACCEPT_ALL)) {
            String status = statusFilter.getAcceptedStatuses()
                    .stream()
                    .map(s -> s.toString())
                    .collect(Collectors.joining(","));
            joiner.add("status=" + status);
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        return "KieContainerResourceFilter{" +
                "releaseIdFilter=" + releaseIdFilter +
                ", statusFilter=" + statusFilter +
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

        KieContainerResourceFilter that = (KieContainerResourceFilter) o;

        if (releaseIdFilter != null ? !releaseIdFilter.equals(that.releaseIdFilter) : that.releaseIdFilter != null)
            return false;
        return statusFilter != null ? statusFilter.equals(that.statusFilter) : that.statusFilter == null;

    }

    @Override
    public int hashCode() {
        int result = releaseIdFilter != null ? releaseIdFilter.hashCode() : 0;
        result = 31 * result + (statusFilter != null ? statusFilter.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private ReleaseIdFilter releaseIdFilter = ReleaseIdFilter.ACCEPT_ALL;
        private KieContainerStatusFilter statusFilter = KieContainerStatusFilter.ACCEPT_ALL;

        public Builder() {
        }

        public Builder releaseId(ReleaseId releaseId) {
            this.releaseIdFilter = new ReleaseIdFilter(releaseId);
            return this;
        }

        public Builder releaseId(String groupId, String artifactId, String version) {
            this.releaseIdFilter = new ReleaseIdFilter(groupId, artifactId, version);
            return this;
        }

        public Builder status(KieContainerStatus containerStatus) {
            this.statusFilter = new KieContainerStatusFilter(containerStatus);
            return this;
        }

        public Builder statuses(KieContainerStatus... containerStatuses) {
            this.statusFilter = new KieContainerStatusFilter(containerStatuses);
            return this;
        }

        public KieContainerResourceFilter build() {
            return new KieContainerResourceFilter(releaseIdFilter, statusFilter);
        }
    }

}
