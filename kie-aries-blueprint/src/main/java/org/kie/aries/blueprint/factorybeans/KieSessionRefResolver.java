/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.aries.blueprint.factorybeans;

import org.kie.api.builder.ReleaseId;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.runtime.KieSession;
import org.osgi.service.blueprint.container.ComponentDefinitionException;

import java.util.List;

public class KieSessionRefResolver extends AbstractKieObjectsResolver {
    private final String id;
    private final List<KieListenerAdaptor> listeners;
    private final List<KieLoggerAdaptor> loggers;
    private final List<?> commands;

    public KieSessionRefResolver( ReleaseId releaseId, String id, List<KieListenerAdaptor> listeners, List<KieLoggerAdaptor> loggers, List<?> commands ) {
        super( releaseId );
        this.id = id;
        this.listeners = listeners;
        this.loggers = loggers;
        this.commands = commands;
    }

    @Override
    public Object call() throws Exception {
        Object obj = resolveKSession(id, releaseId);
        if ( obj != null) {
            KieSessionFactoryBeanHelper.addListeners((KieRuntimeEventManager) obj, listeners);
            KieSessionFactoryBeanHelper.attachLoggers((KieRuntimeEventManager) obj, loggers);
            if (obj instanceof KieSession){
                KieSessionFactoryBeanHelper.executeCommands((KieSession)obj, commands);
            }
            return obj;
        }
        throw new ComponentDefinitionException( "No KSession found in kmodule.xml with id '" + id + "'.");
    }
}
