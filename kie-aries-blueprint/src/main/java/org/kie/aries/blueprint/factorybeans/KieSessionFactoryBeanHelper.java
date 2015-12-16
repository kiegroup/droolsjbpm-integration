/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieLoggers;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieSession;

import java.util.List;

public class KieSessionFactoryBeanHelper {

    protected static void addListeners(KieRuntimeEventManager kieRuntimeEventManager, List<KieListenerAdaptor> listeners){
        if (listeners == null || listeners.size() == 0){
            //do nothing
            return;
        }
        for (KieListenerAdaptor listenerConfig : listeners){
            String type = listenerConfig.getType();
            if ( type.equalsIgnoreCase(RuleRuntimeEventListener.class.getName())){
                kieRuntimeEventManager.addEventListener((RuleRuntimeEventListener) listenerConfig.getObjectRef());
            } else  if ( type.equalsIgnoreCase(ProcessEventListener.class.getName())){
                kieRuntimeEventManager.addEventListener((ProcessEventListener) listenerConfig.getObjectRef());
            } else if ( type.equalsIgnoreCase(AgendaEventListener.class.getName())){
                kieRuntimeEventManager.addEventListener((AgendaEventListener) listenerConfig.getObjectRef());
            }
        }
    }

    protected static void attachLoggers(KieRuntimeEventManager ksession, List<KieLoggerAdaptor> loggerAdaptors) {
        if (loggerAdaptors != null && loggerAdaptors.size() > 0) {
            KieServices ks = KieServices.Factory.get();
            KieLoggers loggers = ks.getLoggers();
            for (KieLoggerAdaptor adaptor : loggerAdaptors) {
                KieRuntimeLogger runtimeLogger;
                switch (KieLoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.valueOf(adaptor.getLoggerType())) {
                    case LOGGER_TYPE_FILE:
                        runtimeLogger = loggers.newFileLogger(ksession, adaptor.getFile());
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                    case LOGGER_TYPE_THREADED_FILE:
                        runtimeLogger = loggers.newThreadedFileLogger(ksession, adaptor.getFile(), adaptor.getInterval());
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                    case LOGGER_TYPE_CONSOLE:
                        runtimeLogger = loggers.newConsoleLogger(ksession);
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                }
            }
        }
    }


    public static void executeCommands(KieSession kieSession, List<?> commands) {
        if ( commands != null && kieSession != null) {
            for (Object cmd : commands) {
                kieSession.execute((Command)cmd);
            }
        }
    }
}
