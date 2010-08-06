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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.configuration.RioProvider;

public class ExecutionEnvironmentFactory {

    public static ExecutionEnvironment newExecutionEnvironment(String name, GenericProvider provider) {
        return GenericProviderContainerFactoryHelper.doOnGenericProvider(provider, new ExecutionEnvironmentBuilder(name));
    }

    private static class ExecutionEnvironmentBuilder implements GenericProviderContainerBuilder<ExecutionEnvironment> {

        private String name;

        /**
         * @param executionEnvironmentName the name for all execution environments created by this builder
         */
        public ExecutionEnvironmentBuilder(String executionEnvironmentName) {
            this.name = executionEnvironmentName;
        }

        public ExecutionEnvironment onLocalProvider(LocalProvider provider) {
            return new ExecutionEnvironment(name,
                    //provider.getConnector("org.drools.grid.local.LocalNodeConnector"));
                    provider.getConnector("Local:Local:Node"));
        }

        public ExecutionEnvironment onMinaProvider(MinaProvider provider) {
            return new ExecutionEnvironment(name,
                    //provider.getConnector("org.drools.grid.remote.mina.RemoteMinaNodeConnector"));
                    provider.getConnector("Remote:Mina:Node"));
        }

        public ExecutionEnvironment onRioProvider(RioProvider provider) {
            
            return new ExecutionEnvironment(name,
                    //provider.getConnector("org.drools.grid.distributed.DistributedRioNodeConnector"));
                    provider.getConnector("Distributed:Rio:Node"));
        }

        public ExecutionEnvironment onHornetQProvider() {
            return null;
        }
    }
}
