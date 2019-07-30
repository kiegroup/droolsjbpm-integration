/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.impl;

import static org.kie.api.builder.ReleaseIdComparator.SortDirection.DESCENDING;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.ReleaseIdComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a KieContainer to deploy.
 * Single string format is containerId(containerAlias)=groupId:artifactId:version
 * Multiple string format is c0(n0)=g0:a0:v0|c1(n1)=g1:a1:v1|c2=g2:a2:v2
 * @see org.kie.server.services.impl.storage.file.KieServerStateFileInit
 */
public class KieServerContainerDeployment {

    private static final Logger LOG = LoggerFactory.getLogger(KieServerContainerDeployment.class);

    private final String containerId;
    private final String containerAlias;
    private final ReleaseId releaseId;

    public KieServerContainerDeployment(String containerId, ReleaseId releaseId) {
        this(containerId, null, releaseId);
    }

    public KieServerContainerDeployment(String containerId, String containerAlias, ReleaseId releaseId) {
        if (containerId == null || containerId.trim().isEmpty()) {
            throw new IllegalArgumentException("illegal containerId: " + containerId);
        }
        if (releaseId == null) {
            throw new IllegalArgumentException("null releaseId");
        }
        this.containerId = containerId;
        if (containerAlias != null) {
            containerAlias = containerAlias.trim();
            if (containerAlias.length() == 0) {
                containerAlias = null;
            }
        }
        this.containerAlias = containerAlias;
        this.releaseId = releaseId;
    }

    public String getContainerId() {
        return containerId;
    }

    public String getContainerAlias() {
        return containerAlias;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KieServerContainerDeployment)) {
            return false;
        }
        KieServerContainerDeployment other = (KieServerContainerDeployment) obj;
        if (containerId == null) {
            if (other.containerId != null) {
                return false;
            }
        } else if (!containerId.equals(other.containerId)) {
            return false;
        }
        if (containerAlias == null) {
            if (other.containerAlias != null) {
                return false;
            }
        } else if (!containerAlias.equals(other.containerAlias)) {
            return false;
        }
        if (releaseId == null) {
            if (other.releaseId != null) {
                return false;
            }
        } else if (!releaseId.equals(other.releaseId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((containerId == null) ? 0 : containerId.hashCode());
        result = prime * result + ((containerAlias == null) ? 0 : containerAlias.hashCode());
        result = prime * result + ((releaseId == null) ? 0 : releaseId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(containerId);
        if (containerAlias != null) {
            sb.append('(');
            sb.append(containerAlias);
            sb.append(')');
        }
        return sb
                .append('=')
                .append(releaseId.getGroupId())
                .append(':')
                .append(releaseId.getArtifactId())
                .append(':')
                .append(releaseId.getVersion())
                .toString();
    }

    public static String toString(Collection<KieServerContainerDeployment> deployments) {
        StringBuilder sb = new StringBuilder();
        if (deployments != null) {
            Iterator<KieServerContainerDeployment> iter = deployments.iterator();
            while (iter.hasNext()) {
                KieServerContainerDeployment deployment = iter.next();
                sb.append(deployment.toString());
                if (iter.hasNext()) {
                    sb.append('|');
                }
            }
        }
        return sb.toString();

    }

    public static Set<KieServerContainerDeployment> fromString(String str) {
        return fromString(str, true);
    }

    public static Set<KieServerContainerDeployment> fromString(String str, boolean onlyLatest) {
        Set<KieServerContainerDeployment> deployments = new LinkedHashSet<KieServerContainerDeployment>();
        if (str != null && !str.isEmpty()) {
            Map<String,String> containerIds_containerAliases = new TreeMap<String,String>();
            Map<String,Set<ReleaseId>> containerIds_releaseIds = new TreeMap<String,Set<ReleaseId>>();
            for (String unit : str.split("\\|")) {
                boolean recognized = false;
                String[] split = unit.split("=");
                if (split.length == 2) {
                    String containerId = null;
                    String containerAlias = null;
                    String identification = split[0].trim();
                    int left_paren_pos = identification.indexOf("(");
                    if (left_paren_pos > 0 && identification.endsWith(")")) {
                        containerId = identification.substring(0, left_paren_pos);
                        containerAlias = identification.substring(left_paren_pos+1, identification.length()-1);
                    } else {
                        containerId = identification;
                    }
                    String[] gav = split[1].trim().split(":");
                    if (!containerId.isEmpty() && gav.length == 3) {
                        recognized = true;
                        containerIds_containerAliases.put(containerId, containerAlias);
                        Set<ReleaseId> releaseIds = containerIds_releaseIds.get(containerId);
                        if (releaseIds == null) {
                            // the descending sort means the first releaseId will be the most recent (latest) version
                            releaseIds = new TreeSet<ReleaseId>(new ReleaseIdComparator(DESCENDING));
                            containerIds_releaseIds.put(containerId, releaseIds);
                        }
                        releaseIds.add(KieServices.Factory.get().newReleaseId(gav[0], gav[1], gav[2]));
                    }
                }
                if (!recognized && LOG.isWarnEnabled()) {
                    LOG.warn(String.format("skipping unrecognized deployment: %s", unit));
                }
            }
            for (Entry<String,Set<ReleaseId>> entry : containerIds_releaseIds.entrySet()) {
                String containerId = entry.getKey();
                String containerAlias = containerIds_containerAliases.get(containerId);
                Set<ReleaseId> releaseIds = entry.getValue();
                Iterator<ReleaseId> releaseIdIter = releaseIds.iterator();
                releaseIdLoop: while (releaseIdIter.hasNext()) {
                    ReleaseId releaseId = releaseIdIter.next();
                    KieServerContainerDeployment deployment = new KieServerContainerDeployment(containerId, containerAlias, releaseId);
                    deployments.add(deployment);
                    if (onlyLatest) {
                        // we will only use the most recent (latest) version when there are duplicate container ids
                        break releaseIdLoop;
                    }
                }
            }
        }
        return deployments;
    }

}
