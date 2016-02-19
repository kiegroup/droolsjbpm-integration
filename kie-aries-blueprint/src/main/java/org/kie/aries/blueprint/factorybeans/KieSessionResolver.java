/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.aries.blueprint.factorybeans;

import org.kie.api.builder.ReleaseId;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.runtime.KieSession;

import java.util.List;

public class KieSessionResolver extends AbstractKieObjectsResolver {

    private final List<KieListenerAdaptor> listeners;
    private final List<KieLoggerAdaptor> loggers;
    private final List<?> commands;
    private final KSessionOptions kSessionOptions;

    public KieSessionResolver( ReleaseId releaseId, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands, KSessionOptions kSessionOptions ) {
        super( releaseId );
        this.listeners = listeners;
        this.loggers = loggers;
        this.commands = commands;
        this.kSessionOptions = kSessionOptions;
    }

    @Override
    public Object call() throws Exception {
        Object obj;
        if ("stateless".equalsIgnoreCase(kSessionOptions.getType())) {
            obj = newStatelessSession(kSessionOptions.getkBaseRef(), releaseId, null);
        } else {
            obj = newStatefulSession(kSessionOptions.getkBaseRef(), releaseId, null);
            KieSessionFactoryBeanHelper.executeCommands( (KieSession)obj, commands );
        }

        KieSessionFactoryBeanHelper.addListeners( (KieRuntimeEventManager) obj, listeners );
        KieSessionFactoryBeanHelper.attachLoggers((KieRuntimeEventManager) obj, loggers);

        return obj;
    }
}
