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


import org.drools.grid.services.DirectoryInstance;
import org.drools.grid.services.configuration.GenericProvider;
import org.drools.grid.services.configuration.LocalProvider;
import org.drools.grid.services.configuration.MinaProvider;
import org.drools.grid.services.configuration.RioProvider;

public class DirectoryInstanceFactory {

    public static DirectoryInstance newDirectoryInstance(String name, GenericProvider provider) {
        return GenericProviderContainerFactoryHelper.doOnGenericProvider(provider, new DirectoryInstanceBuilder(name));
    }

    private static class DirectoryInstanceBuilder implements GenericProviderContainerBuilder<DirectoryInstance> {

        private String name;

        /**
         * @param directoryInstanceName the name for all directory instances created by this builder
         */
        public DirectoryInstanceBuilder(String directoryInstanceName) {
            this.name = directoryInstanceName;
        }

        public DirectoryInstance onLocalProvider(LocalProvider provider) {
            return new DirectoryInstance(name,
                    //provider.getConnector("org.drools.grid.local.LocalDirectoryConnector"));
                    provider.getConnector("Local:Local:Directory"));
        }

        public DirectoryInstance onMinaProvider(MinaProvider provider) {
            return new DirectoryInstance(name,
                   //provider.getConnector("org.drools.grid.remote.directory.RemoteMinaDirectoryConnector"));
                   provider.getConnector("Remote:Mina:Directory"));
        }

        public DirectoryInstance onRioProvider(RioProvider provider) {
            return new DirectoryInstance(name,
                    //provider.getConnector("org.drools.distributed.directory.impl.DistributedRioDirectoryConnector"));
                    provider.getConnector("Distributed:Rio:Directory"));
        }

        public DirectoryInstance onHornetQProvider() {
            return null;
        }
    }
}
