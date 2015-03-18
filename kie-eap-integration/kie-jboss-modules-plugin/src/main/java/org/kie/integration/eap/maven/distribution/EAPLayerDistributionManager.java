/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.distribution;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = EAPLayerDistributionManager.class )
public interface EAPLayerDistributionManager {

    /**
     * Reads a static layer model from a given input.
     * @param input The source object (file,etc)
     * @return The layer distribution model.
     * @throws Exception Exception parsing the input.
     */
    EAPStaticLayerDistribution read(Object input) throws Exception;

    /**
     * Generates a persistence object from a static layer model.
     * @param distro The distribution to persist.
     * @return The persistence oject (can differ from each implementation).
     * @throws Exception Exception writing the output.
     */

    Object write(EAPStaticLayerDistribution distro) throws Exception;
}
