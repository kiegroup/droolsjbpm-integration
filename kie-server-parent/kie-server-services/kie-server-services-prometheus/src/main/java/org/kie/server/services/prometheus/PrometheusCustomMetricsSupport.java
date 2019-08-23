/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.prometheus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.jbpm.executor.AsynchronousJobListener;
import org.jbpm.services.api.DeploymentEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.server.services.api.KieContainerInstance;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;

class PrometheusCustomMetricsSupport {

    private final Map<Class, List<?>> customMetricsInstances;

    private final Map<MultiKey, List<AgendaEventListener>> agendaEventListeners;
    private final Map<String, List<DMNRuntimeEventListener>> dmnRuntimeEventListeners;
    private final Map<String, List<PhaseLifecycleListener>> phaseLifecycleListeners;

    private ServiceLoader<PrometheusMetricsProvider> loader;

    PrometheusCustomMetricsSupport(PrometheusKieServerExtension extension) {
        loader = ServiceLoader.load(PrometheusMetricsProvider.class);
        customMetricsInstances = new HashMap<>();
        agendaEventListeners = new HashMap<>();
        dmnRuntimeEventListeners = new HashMap<>();
        phaseLifecycleListeners = new HashMap<>();
    }

    boolean hasCustomMetrics() {
        return loader.iterator().hasNext();
    }

    List<PrometheusMetricsProvider> customMetricsProviders() {
        List<PrometheusMetricsProvider> providers = new ArrayList<>();
        loader.forEach(p -> providers.add(p));
        return providers;
    }

    List<DMNRuntimeEventListener> getDMNRuntimeEventListener(KieContainerInstance kContainer) {
        synchronized (dmnRuntimeEventListeners) {
            if (!dmnRuntimeEventListeners.containsKey(kContainer.getContainerId())) {
                List<DMNRuntimeEventListener> customListeners = new ArrayList<>();
                //customer listeners, if any
                loader.forEach(p -> {
                    DMNRuntimeEventListener l = p.createDMNRuntimeEventListener(kContainer);
                    if (l != null) {
                        customListeners.add(l);
                    }
                });
                dmnRuntimeEventListeners.put(kContainer.getContainerId(), customListeners);
            }
            return dmnRuntimeEventListeners.get(kContainer.getContainerId());
        }
    }

    List<AgendaEventListener> getAgendaEventListener(String kieSessionId, KieContainerInstance kContainer) {
        final MultiKey key = new MultiKey(kieSessionId, kContainer);
        synchronized (agendaEventListeners) {
            if (!agendaEventListeners.containsKey(key)) {
                List<AgendaEventListener> customListeners = new ArrayList<>();
                // customs listeners, if any
                loader.forEach(p -> {
                    AgendaEventListener l = p.createAgendaEventListener(kieSessionId, kContainer);
                    if (l != null) {
                        customListeners.add(l);
                    }
                });
                agendaEventListeners.put(key, customListeners);
            }
            return agendaEventListeners.get(key);
        }
    }

    List<PhaseLifecycleListener> getPhaseLifecycleListener(String solverId) {
        synchronized (phaseLifecycleListeners) {
            if (!phaseLifecycleListeners.containsKey(solverId)) {
                List<PhaseLifecycleListener> customListeners = new ArrayList<>();
                //customer listeners, if any
                loader.forEach(p -> {
                    PhaseLifecycleListener l = p.createPhaseLifecycleListener(solverId);
                    if (l != null) {
                        customListeners.add(l);
                    }
                });
                phaseLifecycleListeners.put(solverId, customListeners);
            }
            return phaseLifecycleListeners.get(solverId);
        }
    }

    List<AsynchronousJobListener> getAsynchronousJobListener() {
        return getListener(AsynchronousJobListener.class);
    }

    List<DeploymentEventListener> getDeploymentEventListener() {
        return getListener(DeploymentEventListener.class);
    }

    private <T> List<T> getListener(Class<T> clazz) {
        synchronized (customMetricsInstances) {
            if (!customMetricsInstances.containsKey(clazz)) {
                List<T> customMetricsTargets = new ArrayList<>();
                loader.forEach(p -> {
                    T l = createListener(p, clazz);
                    if (l != null) {
                        customMetricsTargets.add(l);
                    }
                });
                customMetricsInstances.put(clazz, customMetricsTargets);
            }
            return (List<T>) customMetricsInstances.get(clazz);
        }
    }

    private <T> T createListener(PrometheusMetricsProvider p , Class<T> clazz) {
        T l = null;
        if (DeploymentEventListener.class.isAssignableFrom(clazz)) {
            l = (T) p.createDeploymentEventListener();
        }
        if (AsynchronousJobListener.class.isAssignableFrom(clazz)) {
            l = (T) p.createAsynchronousJobListener();
        }
        return l;
    }

}
