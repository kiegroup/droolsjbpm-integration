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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.MessageDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalDescBase;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processMessages;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processSignals;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.topicFromSignal;

class KafkaServerRegistration {
    private Map<String, Map<SignalDesc, Collection<String>>> topic2Signal = new HashMap<>();
    private Map<String, Map<MessageDesc, Collection<String>>> topic2Message = new HashMap<>();

    synchronized void close() {
        topic2Signal.clear();
        topic2Message.clear();
    }


    synchronized boolean isEmpty() {
        return topic2Signal.isEmpty() && topic2Message.isEmpty();
    }

    Set<String> addRegistration(DeploymentEvent event) {
        return updateRegistration(event, this::updateTopics);
    }

    Set<String> removeRegistration(DeploymentEvent event) {
        return updateRegistration(event, this::removeTopics);
    }

    private synchronized Set<String> updateRegistration(DeploymentEvent event,
                                                        BiConsumer<String, ProcessDefinition> updater) {
        for (DeployedAsset asset : event.getDeployedUnit().getDeployedAssets()) {
            updater.accept(event.getDeploymentId(), (ProcessDefinition) asset);
        }
        Set<String> topics = new HashSet<>();
        topics.addAll(topic2Signal.keySet());
        topics.addAll(topic2Message.keySet());
        return topics;
    }

    private void updateTopics(String deploymentId, ProcessDefinition processDefinition) {
        if (processSignals()) {
            addTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc());
        }
        if (processMessages()) {
            addTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc());
        }
    }

    private void removeTopics(String deploymentId, ProcessDefinition processDefinition) {
        removeTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc());
        removeTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc());
    }

    void forEachSignal(ConsumerRecord<String, byte[]> event, KafkaServerEventProcessor<SignalDesc> eventProcessor) {
        forEach(topic2Signal, event, eventProcessor);
    }

    void forEachMessage(ConsumerRecord<String, byte[]> event, KafkaServerEventProcessor<MessageDesc> eventProcessor) {
        forEach(topic2Message, event, eventProcessor);
    }


    private synchronized <T extends SignalDescBase> void forEach(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                    ConsumerRecord<String, byte[]> event,
                                                    KafkaServerEventProcessor<T> processor) {
        Map<T, Collection<String>> signalInfo = topic2SignalBase.get(event.topic());
        if (signalInfo != null) {
            for (Map.Entry<T, Collection<String>> entry : signalInfo.entrySet()) {
                T signal = entry.getKey();
                for (String deploymentId : entry.getValue()) {
                    processor.accept(event, deploymentId, signal);
                }
            }
        }
    }

    private <T extends SignalDescBase> void addTopics(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                      String deploymentId,
                                                      Collection<T> signals) {
        for (T signal : signals) {
            if (!signal.getIncomingNodes().isEmpty()) {
                topic2SignalBase.computeIfAbsent(topicFromSignal(signal), k -> new HashMap<>()).computeIfAbsent(
                        signal, k -> new ArrayList<>()).add(deploymentId);
            }
        }
    }

    private <T extends SignalDescBase> void removeTopics(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                         String deploymentId,
                                                         Collection<T> signalsDesc) {
        for (T signal : signalsDesc) {
            String topic = topicFromSignal(signal);
            Map<T, Collection<String>> signals = topic2SignalBase.get(topic);
            if (signals != null) {
                Collection<String> deploymentIds = signals.get(signal);
                if (deploymentIds != null) {
                    deploymentIds.remove(deploymentId);
                    if (deploymentIds.isEmpty()) {
                        signals.remove(signal);
                        if (signals.isEmpty()) {
                            topic2SignalBase.remove(topic);
                        }
                    }
                }
            }
        }
    }
}
