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
package org.kie.integration.eap.maven.template;

import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplate;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplateFile;

import java.util.Collection;

@Component( role = EAPTemplateBuilder.class )
public interface EAPTemplateBuilder {

    /**
     * Builds the layers.conf file from a template.
     * @param graph The modules graph.
     * @return The layers.conf file.
     */
    String buildLayersConfiguration(EAPModulesGraph graph);

    /**
     * Builds the assembly descriptor file from a template.
     * @param layerId The layer if.
     * @param formats The assembly formats to generate.
     * @param layerDescriptorFilePath The path for the generated layer descriptor file.
     * @param componentDescriptorsFilePaths The path for the generated assembly components descriptors (one for each module to assemble).
     * @return The assembly descriptor file from a template.
     */
    String buildGlobalAssembly(String layerId, String[] formats, String layerDescriptorFilePath, String[] componentDescriptorsFilePaths);

    /**
     * Build the assembly component descriptor file for a given static module.
     * @param node The module graph node.
     * @param moduleDescriptorPath The path for the generated assembly component descriptor file.
     * @param outputPath The output path to generate the files.
     * @return The assembly component descriptor file.
     */
    String buildModuleAssemblyComponent(EAPModuleGraphNode node, String moduleDescriptorPath, String outputPath);

    /**
     * Build the module descriptor file for a given static module (module.xml)
     * @param graph The module graph node.
     * @return The module descriptor file for a given static module (module.xml)
     */
    String buildModuleDescriptor(EAPModuleGraphNode graph);

    /**
     * Build the jboss deployment structure descriptor file for a given dynamic module (jboss-deployment-structure.xml)
     * @param dependencies The dynamic module dependencies to add in the generated jboss deployment structure.
     * @return The jboss deployment structure descriptor file for a given dynamic module (jboss-deployment-structure.xml)
     */
    String buildJbossDeploymentStructure(Collection<? extends EAPModuleGraphNodeDependency> dependencies);

    /**
     * Build the assembly descriptor file for a given dynamic module.
     * @param assemblyTemplate The assembly model.
     * @return The assembly descriptor file for a given dynamic module.
     */
    String buildDynamicModuleAssembly(EAPAssemblyTemplate assemblyTemplate);

    /**
     * Build the jboss-all.xml descriptor to add a dependency from a dynamic module to another one.
     * @param name The dependant module name.
     * @return the jboss-all.xml descriptor content.
     */
    String buildDynamicModuleDependency(String name);

}
