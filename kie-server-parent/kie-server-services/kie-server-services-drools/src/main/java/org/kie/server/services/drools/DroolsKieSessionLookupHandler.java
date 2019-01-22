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

package org.kie.server.services.drools;

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.event.rule.RuleRuntimeEventManager;
import org.kie.api.runtime.CommandExecutor;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieSessionLookupHandler;
import org.kie.server.services.prometheus.PrometheusKieServerExtension;
import org.kie.server.services.prometheus.PrometheusMetrics;
import org.kie.server.services.prometheus.PrometheusMetricsDroolsListener;

public class DroolsKieSessionLookupHandler implements KieSessionLookupHandler {

    @Override
    public CommandExecutor lookupKieSession(String kieSessionId, KieContainerInstance containerInstance, KieServerRegistry registry) {
        CommandExecutor ks = null;
        if( kieSessionId != null ) {
            KieSessionModel ksm = containerInstance.getKieContainer().getKieSessionModel(kieSessionId);
            if( ksm != null ) {
                switch (ksm.getType() ) {
                    case STATEFUL:
                        ks = ((KieContainerImpl)containerInstance.getKieContainer()).getKieSession(kieSessionId);
                        break;
                    case STATELESS:
                        ks = ((KieContainerImpl)containerInstance.getKieContainer()).getStatelessKieSession(kieSessionId);

                        break;
                }
            }

            KieServerExtension extension = registry.getServerExtension(PrometheusKieServerExtension.EXTENSION_NAME);
            if (extension != null && ks != null) {
                RuleRuntimeEventManager eventManager = (RuleRuntimeEventManager)ks;
                PrometheusMetrics metrics = PrometheusKieServerExtension.getMetrics();
                PrometheusMetricsDroolsListener listener = new PrometheusMetricsDroolsListener(metrics);
                eventManager.addEventListener(listener);
            }
            return ks;
        }

        return null;
    }
}
