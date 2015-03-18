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
package org.kie.integration.eap.maven.builder;

import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.layer.EAPLayer;

@Component( role = EAPModulesGraphBuilder.class )
public interface EAPModulesGraphBuilder {

    /**
     * Builds a modules graph model from a given layer.
     *
     * @param distributionName The distribution name.
     * @param layer The static modules layer source for building the resulting modules graph.
     * @return The modules graph model.
     */
    EAPModulesGraph build(String distributionName, EAPLayer layer);

}
