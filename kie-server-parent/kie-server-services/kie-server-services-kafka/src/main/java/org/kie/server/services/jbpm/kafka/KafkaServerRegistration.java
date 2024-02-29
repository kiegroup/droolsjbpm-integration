/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processMessages;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processSignals;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.topicFromSignal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.MessageDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalDescBase;
import org.jbpm.workflow.core.node.StartNode;
import org.kie.api.builder.ReleaseIdComparator.ComparableVersion;
import org.kie.api.definition.process.Node;

class KafkaServerRegistration {
    
    private Map<String, Map<SignalDesc, Map<DeploymentId, SortedSet<VersionedDeploymentId>>>> topic2Signal = new ConcurrentHashMap<>();
    private Map<String, Map<MessageDesc, Map<DeploymentId, SortedSet<VersionedDeploymentId>>>> topic2Message = new ConcurrentHashMap<>();

    void close() {
        topic2Signal.clear();
        topic2Message.clear();
    }

    boolean isEmpty() {
        return topic2Signal.isEmpty() && topic2Message.isEmpty();
    }

    Set<String> addRegistration(DeploymentEvent event) {
        for (DeployedAsset asset : event.getDeployedUnit().getDeployedAssets()) {
            updateTopics(new DeploymentIdFactory(event), (ProcessDefinition) asset);
        }
        return getTopicsRegistered();
    }

    Set<String> removeRegistration(DeploymentEvent event, Consumer<String> topicProcessed) {
        for (DeployedAsset asset : event.getDeployedUnit().getDeployedAssets()) {
            removeTopics(new DeploymentIdFactory(event), (ProcessDefinition) asset, topicProcessed);
        }
        return getTopicsRegistered();
    }
    
   
    private Set<String> getTopicsRegistered() {
        Set<String> topics = new HashSet<>();
        topics.addAll(topic2Signal.keySet());
        topics.addAll(topic2Message.keySet());
        return topics;
    }

    private void updateTopics(DeploymentIdFactory deploymentId, ProcessDefinition processDefinition) {
        if (processSignals()) {
            addTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc());
        }
        if (processMessages()) {
            addTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc());
        }
    }

    private void removeTopics(DeploymentIdFactory deploymentId,
                              ProcessDefinition processDefinition,
                              Consumer<String> topicProcessed) {
        removeTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc(), topicProcessed);
        removeTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc(), topicProcessed);
    }

    void forEachSignal(ConsumerRecord<String, byte[]> event, KafkaServerEventProcessor<SignalDesc> eventProcessor) {
        forEach(topic2Signal, event, eventProcessor);
    }

    void forEachMessage(ConsumerRecord<String, byte[]> event, KafkaServerEventProcessor<MessageDesc> eventProcessor) {
        forEach(topic2Message, event, eventProcessor);
    }

    private <T extends SignalDescBase> void forEach(Map<String, Map<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>>> topic2SignalBase,
                                                                 ConsumerRecord<String, byte[]> event,
                                                                 KafkaServerEventProcessor<T> processor) {
        Map<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>> signalInfo = topic2SignalBase.get(event.topic());
        if (signalInfo != null) {
            for (Map.Entry<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>> entry : signalInfo.entrySet()) {
                T signal = entry.getKey();
                boolean isStartNode = isStartNode(signal);
                    
                for (Map.Entry<DeploymentId, SortedSet<VersionedDeploymentId>> deploymentIdEntry : entry.getValue().entrySet()) {
                    if (isStartNode) {
                        processor.accept(event, deploymentIdEntry.getValue().first().getDeploymentId(), signal);
                    } else {
                        for (VersionedDeploymentId id : deploymentIdEntry.getValue()) {
                            processor.accept(event, id.getDeploymentId(), signal);
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isStartNode (SignalDescBase signalDesc) {
        for (Node node : signalDesc.getIncomingNodes()) {
            if (node instanceof StartNode) {
                return true;
            }
        }
        return false;
    }

    private <T extends SignalDescBase> void addTopics(Map<String, Map<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>>> topic2SignalBase,
                                                      DeploymentIdFactory deploymentIdFactory,
                                                      Collection<T> signals) {
        for (T signal : signals) {
            Collection<Node> nodes = signal.getIncomingNodes();
            if (!nodes.isEmpty()) {
                topic2SignalBase.
                    computeIfAbsent(topicFromSignal(signal), k -> new HashMap<>()).
                                computeIfAbsent(signal, k -> new HashMap<>()).computeIfAbsent(deploymentIdFactory.getUnversionDeploymentId(), k -> new TreeSet<>()).add(deploymentIdFactory.getVersionedDeploymentId());
            }
        }
    }

    private <T extends SignalDescBase> void removeTopics(Map<String, Map<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>>> topic2SignalBase,
                                                         DeploymentIdFactory deploymentIdFactory,
                                                         Collection<T> signalsDesc,
                                                         Consumer<String> topicProcessed) {
        Set<String> topicsPerDeployment = new HashSet<>();
        for (T signal : signalsDesc) {
            String topic = topicFromSignal(signal);
            Map<T, Map<DeploymentId, SortedSet<VersionedDeploymentId>>> signals = topic2SignalBase.get(topic);
            if (signals != null) {
                Map<DeploymentId, SortedSet<VersionedDeploymentId>> deploymentIds = signals.get(signal);
                if (deploymentIds != null) {
                    SortedSet<VersionedDeploymentId> deploymentFullIds = deploymentIds.get(deploymentIdFactory.getUnversionDeploymentId());

                    if (deploymentFullIds != null && deploymentFullIds.remove(deploymentIdFactory.getVersionedDeploymentId())) {

                        topicsPerDeployment.add(topic);
                        if (deploymentFullIds.isEmpty()) {
                            deploymentIds.remove(deploymentIdFactory.getUnversionDeploymentId());
                        }
                    }
                    if (deploymentIds.isEmpty()) {
                        signals.remove(signal);
                        if (signals.isEmpty()) {
                            topic2SignalBase.remove(topic);
                        }
                    }
                }
            }
        }
        for (String removed : topicsPerDeployment) {
            topicProcessed.accept(removed);
        }
    }
    
    private static class DeploymentIdFactory {

        private final VersionedDeploymentId versionedDeploymentId;
        private final DeploymentId unversionDeploymentId;

        public DeploymentIdFactory(DeploymentEvent event) {

            final String groupId;
            final String artifactId;
            final String version;
            final String deploymentId = event.getDeploymentId();

            if (event.getDeployedUnit().getDeploymentUnit() instanceof KModuleDeploymentUnit) {
                KModuleDeploymentUnit kModule = (KModuleDeploymentUnit) event.getDeployedUnit().getDeploymentUnit();
                groupId = kModule.getGroupId();
                artifactId = kModule.getArtifactId();
                version = kModule.getVersion();

            } else {
                String[] tokens = deploymentId.split(":");
                if (tokens.length >= 3) {
                    groupId = tokens[0];
                    artifactId = tokens[1];
                    version = tokens[2];
                } else {
                    groupId = null;
                    artifactId = deploymentId;
                    version = null;
                }
            }
            versionedDeploymentId = new VersionedDeploymentId(version, deploymentId);
            unversionDeploymentId = new DeploymentId(groupId, artifactId);
        }

        public VersionedDeploymentId getVersionedDeploymentId() {
            return versionedDeploymentId;
        }

        public DeploymentId getUnversionDeploymentId() {
            return unversionDeploymentId;
        }
    }

    private static class DeploymentId {

        private String groupId;
        private String artifactId;

        public DeploymentId(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(artifactId, groupId);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DeploymentId)) {
                return false;
            }
            DeploymentId other = (DeploymentId) obj;
            return Objects.equals(artifactId, other.artifactId) && Objects.equals(groupId, other.groupId);
        }

        @Override
        public String toString() {
            return "DeploymentId [groupId=" + groupId + ", artifactId=" + artifactId + "]";
        }

    }

    private static class VersionedDeploymentId implements Comparable<VersionedDeploymentId> {

        private final String deploymentId;
        private final ComparableVersion version;

        public VersionedDeploymentId(String version, String deploymentId) {
            this.deploymentId = deploymentId;
            this.version = version != null ? new ComparableVersion(version) : null;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof VersionedDeploymentId)) {
                return false;
            }
            VersionedDeploymentId other = (VersionedDeploymentId) obj;
            return Objects.equals(version, other.version);
        }

        @Override
        public String toString() {
            return "VersionedDeploymentId [deploymentId=" + deploymentId + ", version=" + version + "]";
        }

        @Override
        public int compareTo(VersionedDeploymentId o) {
            if (o.version == null) {
                return this.version == null ? 0 : -1;
            }
            return o.version.compareTo(this.version);
        }
    }
}
