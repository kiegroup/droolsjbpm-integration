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
import org.drools.SystemEventListenerFactory;
import org.drools.grid.distributed.DistributedRioNodeConnector;
import org.drools.grid.local.LocalNodeConnector;
import org.drools.grid.remote.mina.RemoteMinaNodeConnector;
import org.drools.grid.services.ExecutionEnvironment;
import org.drools.grid.services.configuration.GenericProvider;
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

        @Override
        public ExecutionEnvironment onLocalProvider() {
            return new ExecutionEnvironment(name, new LocalNodeConnector());
        }

        @Override
        public ExecutionEnvironment onMinaProvider(MinaProvider provider) {
            return new ExecutionEnvironment(name,
                    new RemoteMinaNodeConnector(name,
                     provider.getProviderAddress(),
                     provider.getProviderPort(),
                    SystemEventListenerFactory.getSystemEventListener()));
        }

        @Override
        public ExecutionEnvironment onRioProvider(RioProvider rioProvider) {
            try {
                rioProvider.lookupExecutionNodeServices();
            } catch (IOException ex) {
                Logger.getLogger(ExecutionEnvironmentFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExecutionEnvironmentFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new ExecutionEnvironment(name, 
                    new DistributedRioNodeConnector(name,
                        SystemEventListenerFactory.getSystemEventListener(),
                        rioProvider.getExecutionNode()));
        }

        @Override
        public ExecutionEnvironment onHornetQProvider() {
            return null;
        }
    }
}
