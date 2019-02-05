/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name = "list-containers")
@XStreamAlias("list-containers")
@XmlAccessorType(XmlAccessType.NONE)
public class ListContainersCommand implements KieServerCommand {

    private static final long serialVersionUID = -1803374525440238478L;

    @XmlElement(name = "kie-container-filter")
    @XStreamAlias("kie-container-filter")
    private final KieContainerResourceFilter kieContainerResourceFilter;

    public ListContainersCommand() {
        kieContainerResourceFilter = KieContainerResourceFilter.ACCEPT_ALL;
    }

    public ListContainersCommand(KieContainerResourceFilter kieContainerResourceFilter) {
        this.kieContainerResourceFilter = kieContainerResourceFilter;
    }

    public KieContainerResourceFilter getKieContainerResourceFilter() {
        return kieContainerResourceFilter;
    }

    @Override
    public String toString() {
        return "ListContainersCommand{" +
                "kieContainerResourceFilter=" + kieContainerResourceFilter +
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

        ListContainersCommand that = (ListContainersCommand) o;

        return kieContainerResourceFilter != null ? kieContainerResourceFilter.equals(that.kieContainerResourceFilter) : that.kieContainerResourceFilter == null;
    }

    @Override
    public int hashCode() {
        return kieContainerResourceFilter != null ? kieContainerResourceFilter.hashCode() : 0;
    }
}
