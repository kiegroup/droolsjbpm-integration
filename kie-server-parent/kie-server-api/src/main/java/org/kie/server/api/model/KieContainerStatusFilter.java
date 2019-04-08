/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement(name = "kie-container-status-filter")
@XStreamAlias("kie-container-status-filter")
@XmlAccessorType(XmlAccessType.NONE)
public class KieContainerStatusFilter {

    public static final KieContainerStatusFilter ACCEPT_ALL = new KieContainerStatusFilter();

    @XmlElement(name = "accepted-status")
    @XStreamAlias("accepted-status")
    private final List<KieContainerStatus> acceptedStatuses;

    /**
     * Creates status filter from the specified string. The expected format is "status1,status2,status3,...".
     * <p>
     * Important: in case the specified string is empty or null, the default ACCEPT_ALL filter is created
     * @param inputStr string representation of the filter
     * @return new filter parsed from the string or ACCEPT_ALL filter in case the string is empty or null
     */
    public static KieContainerStatusFilter parseFromNullableString(String inputStr) {
        if (inputStr == null || inputStr.isEmpty()) {
            return ACCEPT_ALL;
        }
        List<KieContainerStatus> statuses = new ArrayList<KieContainerStatus>();
        String[] strStatuses = inputStr.split(",");
        for (String strStatus : strStatuses) {
            statuses.add(KieContainerStatus.valueOf(strStatus.toUpperCase()));
        }
        return new KieContainerStatusFilter(statuses);
    }

    public KieContainerStatusFilter() {
        // IMPORTANT: the list acceptedStatuses needs to be mutable in order to not break JAXB unmarshalling
        // jaxb calls clear() method on the list, which usually throws UnsupportedOperationException on immutable lists
        this.acceptedStatuses = new ArrayList<KieContainerStatus>(Arrays.asList(KieContainerStatus.values()));
    }

    public KieContainerStatusFilter(List<KieContainerStatus> acceptedStatuses) {
        this.acceptedStatuses = acceptedStatuses;
    }

    public KieContainerStatusFilter(KieContainerStatus... acceptedStatuses) {
        this.acceptedStatuses = Arrays.asList(acceptedStatuses);
    }

    public List<KieContainerStatus> getAcceptedStatuses() {
        return acceptedStatuses;
    }

    public boolean accept(KieContainerStatus status) {
        return this.acceptedStatuses.contains(status);
    }

    @Override
    public String toString() {
        return "KieContainerStatusFilter{" +
                "acceptedStatuses=" + acceptedStatuses +
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

        KieContainerStatusFilter that = (KieContainerStatusFilter) o;

        return acceptedStatuses != null ? acceptedStatuses.equals(that.acceptedStatuses) : that.acceptedStatuses == null;
    }

    @Override
    public int hashCode() {
        return acceptedStatuses != null ? acceptedStatuses.hashCode() : 0;
    }
}
