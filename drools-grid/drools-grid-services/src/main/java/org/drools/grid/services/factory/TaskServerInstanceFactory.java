/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.grid.services.factory;

import org.drools.SystemEventListenerFactory;
import org.drools.grid.services.TaskServerInstance;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.configuration.RioProvider;
import org.drools.grid.task.RemoteMinaHumanTaskConnector;

public class TaskServerInstanceFactory {

    public static TaskServerInstance newTaskServerInstance(String name, GenericProvider provider) {
        return GenericProviderContainerFactoryHelper.doOnGenericProvider(provider, new TaskServerInstanceBuilder(name));
    }

    private static class TaskServerInstanceBuilder implements GenericProviderContainerBuilder<TaskServerInstance> {

        private String name;

        public TaskServerInstanceBuilder(String taskServerInstanceName) {
            this.name = taskServerInstanceName;
        }

        public TaskServerInstance onHornetQProvider() {
            throw new UnsupportedOperationException("We don't have a HortnetQ implementation for the Task Service. Yet!");
        }

        public TaskServerInstance onLocalProvider(LocalProvider provider) {
            throw new UnsupportedOperationException("We don't have a local implementation for the Task Service. Yet!");
        }

        public TaskServerInstance onMinaProvider(MinaProvider provider) {
            return new TaskServerInstance(name,
                    new RemoteMinaHumanTaskConnector(name,
                    ((MinaProvider) provider).getProviderAddress(),
                    ((MinaProvider) provider).getProviderPort(),
                    SystemEventListenerFactory.getSystemEventListener()));
        }

        public TaskServerInstance onRioProvider(RioProvider rioProvider) {
            throw new UnsupportedOperationException("We don't have a Distributed Rio implementation for the Task Service. Yet!");
        }
    }
}
