/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.admin;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import static java.util.Collections.emptyMap;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "migration-process-specification")
public class MigrationProcessSpecification {

    private String sourceProcessId;
    private String targetProcessId;

    @XmlElementWrapper(name = "migration-nodes-map")
    private Map<String, String> nodes;

    public String getSourceProcessId() {
        return sourceProcessId;
    }

    public void setSourceProcessId(String sourceProcessId) {
        this.sourceProcessId = sourceProcessId;
    }

    public String getTargetProcessId() {
        return targetProcessId;
    }

    public void setTargetProcessId(String targetProcessId) {
        this.targetProcessId = targetProcessId;
    }

    public Map<String, String> getNodes() {
        if(nodes == null) {
             return emptyMap();
        }
        return nodes;
    }
    
    
    public void setNodes(Map<String, String> nodes) {
        if(nodes == null) {
            this.nodes = emptyMap();
        }
        this.nodes = nodes;
    }
}
