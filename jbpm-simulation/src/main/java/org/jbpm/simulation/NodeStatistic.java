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

package org.jbpm.simulation;

public class NodeStatistic {

    private String nodeId;
    
    private Long minTimeStamp;
    private Long maxTimeStamp;
    
    private Long instances;
    
    

    public NodeStatistic(String nodeId, Long minTimeStamp, Long maxTimeStamp,
            Long instances) {
        this.nodeId = nodeId;
        this.minTimeStamp = minTimeStamp;
        this.maxTimeStamp = maxTimeStamp;
        this.instances = instances;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Long getMinTimeStamp() {
        return minTimeStamp;
    }

    public void setMinTimeStamp(Long minTimeStamp) {
        this.minTimeStamp = minTimeStamp;
    }

    public Long getMaxTimeStamp() {
        return maxTimeStamp;
    }

    public void setMaxTimeStamp(Long maxTimeStamp) {
        this.maxTimeStamp = maxTimeStamp;
    }

    public Long getInstances() {
        return instances;
    }

    public void setInstances(Long instances) {
        this.instances = instances;
    }
    
    
}
