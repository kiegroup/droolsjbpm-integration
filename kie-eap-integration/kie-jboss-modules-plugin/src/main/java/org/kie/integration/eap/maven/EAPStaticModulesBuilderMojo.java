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
package org.kie.integration.eap.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.template.EAPTemplateBuilder;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.kie.integration.eap.maven.util.EAPFileUtils;

import java.io.File;
import java.io.IOException;

/**
 * This plugin mojo generates a static layer definition and the assembly files to assemble it.
 *
 * @goal build-static-layer
 * @requiresProject true
 */
public class EAPStaticModulesBuilderMojo extends EAPBaseMojo {

    private static final String OUTPUT_STATIC_MODULES = "static-modules";
    private static final String MODULE_DESCRIPTOR_NAME = "module.xml";
    private static final String MODULE_ASSEMBLY_NAME = "component.xml";
    private static final String LAYERS_DESCRIPTOR_NAME = "layers.conf";
    private static final String GLOBAL_ASSEMBLY__DESCRIPTOR_NAME = "-assembly.xml";
    

    /** The path where modules will be deployed in EAP. Corresponds to modules/system/layers. **/
    private static final String ASSEMBLY_OUTPUT_PATH = new StringBuilder("modules").append(File.separator).
            append("system").append(File.separator).append("layers").toString();

    /**
     * The scanner for static modules.
     * @component role-hint='velocity'
     */
    private EAPTemplateBuilder templateBuilder;

    /**
     * The output path for the genrated module descriptor and assembly files.
     * The resulting assembly descriptor file will be created in this path.
     *
     * @parameter default-value=""
     */
    protected String outputPath;

    /**
     * The output formats for assembly descriptor. Use comma-separated values.
     *
     * @parameter default-value="dir,zip"
     */
    protected String assemblyFormats;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

        // Print the graph.
        String distroString = distribution.print();

        getLog().info(distroString);

        // Some checks.
        if (outputPath == null || outputPath.trim().length() == 0) throw new MojoFailureException("Output path value missing.");
        String _outputPath = outputPath.replaceAll("/", File.separator);
        String distroOutputPath = new StringBuilder(_outputPath).append(File.separator).
                append(OUTPUT_STATIC_MODULES).append(File.separator).append(distributionName).toString();

        // Create the output directories.
        File outputPathFile = new File(_outputPath);
        File distroOutputPathFile = new File(distroOutputPath);
        outputPathFile.mkdirs();
        distroOutputPathFile.mkdirs();

        // The output path for assembled elements. Corresponds to modules/system/layers/bpms (for bpms distro)
        StringBuilder outputAssemblyDistroPath = new StringBuilder(ASSEMBLY_OUTPUT_PATH).append(File.separator).
                append(distributionName);

        // Generate the layer configuration descriptor for EAP.
        String layersDescriptor = generateLayersDescriptor(distribution.getGraph());
        try {
            EAPFileUtils.writeFile(distroOutputPathFile, LAYERS_DESCRIPTOR_NAME, layersDescriptor);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write layer descriptor file.", e);
        }

        String[] componentDescriptors = new String[distribution.getGraph().getNodes().size()];
        int index = 0;
        // Generate the module descriptor and assembly component descriptor files for each module
        for (EAPModuleGraphNode node : distribution.getGraph().getNodes() ) {
            String _outputModulePath = new StringBuilder(distroOutputPath).append(File.separator).append(node.getName()).toString();
            File outputModulePath = new File(_outputModulePath);

            // Generate the descriptor files based on templates.
            String moduleDescriptor = generateModuleDescriptor(node);

            // The output path for assembled module. Corresponds to modules/system/layers/bpms/org/apache/maven/main (for bpms distro, org.apache.maven module and main slot).
            String moduleLocation = node.getLocation().replaceAll("/", File.separator);
            String moduleAssemblyOutputPath = new StringBuilder(outputAssemblyDistroPath).append(File.separator).
                    append(moduleLocation).append(File.separator).append(node.getSlot()).toString();
            String moduleAssembly = generateModuleAssemblyComponent(node, new StringBuilder(_outputModulePath).
                    append(File.separator).append(MODULE_DESCRIPTOR_NAME).toString(),moduleAssemblyOutputPath);

            componentDescriptors[index] = new StringBuilder(_outputModulePath).
                    append(File.separator).append(MODULE_ASSEMBLY_NAME).toString();

            // Write the descriptor files in the output path for this module.
            try {
                EAPFileUtils.writeFile(outputModulePath, MODULE_DESCRIPTOR_NAME, moduleDescriptor);
                EAPFileUtils.writeFile(outputModulePath, MODULE_ASSEMBLY_NAME, moduleAssembly);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot write descriptor files for module " + node.getName(), e);
            }

            index++;
        }

        // Generate the global assembly file for all module assembly component files generated.
        String layerId = new StringBuilder(distributionName).append("-layer").toString();
        String[] formats = getAssemblyFormats();
        String layerDescriptorFilePath = new StringBuilder(distroOutputPath).
                append(File.separator).append(LAYERS_DESCRIPTOR_NAME).toString();
        String globalAssemblyFileName = new StringBuilder(distributionName).
                append(GLOBAL_ASSEMBLY__DESCRIPTOR_NAME).toString();
        String globalAssembly = generateGlobalAssembly(layerId, formats, layerDescriptorFilePath, componentDescriptors);

        try {
            EAPFileUtils.writeFile(distroOutputPathFile, globalAssemblyFileName, globalAssembly);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write the global assembly descritpro file.", e);
        }

        // Write the resulting distribution properties.
        writeDistributionProperties();

    }

    protected void writeDistributionProperties() throws MojoExecutionException{
        if (distribution.getGraph() != null) {

            // Package root path where generated distribution properties will be placed (relative to build output dir).
            String[] _distroPackage = EAPConstants.DISTRIBUTION_PROPERTIES_PACKAGE.split("\\.");
            StringBuilder distroPath = new StringBuilder(project.getBuild().getOutputDirectory());
            for (String dp : _distroPackage) {
                distroPath.append(File.separator).append(dp);
            }

            try {
                // Generate the distribution definition content.
                String distroDefinition = (String) distributionManager.write(distribution);

                // Save the content into a file.
                File distroPathFile = new File(distroPath.toString());
                distroPathFile.mkdirs();
                File file = new File(distroPathFile, EAPConstants.DISTRO_PACKAGE_FILE_NAME);

                EAPFileUtils.writeToFile(distroDefinition, file);

            } catch (Exception e) {
                throw new MojoExecutionException("Cannot generate the distribution package file.", e);
            }
        }
    }



    private String[] getAssemblyFormats() {
        return assemblyFormats.split(",");
    }

    protected String generateGlobalAssembly(String layerId, String[] formats, String layerDescriptorFilePath, String[] componentDescriptorsFilePaths) {
        return templateBuilder.buildGlobalAssembly(layerId, formats, layerDescriptorFilePath, componentDescriptorsFilePaths);
    }

    protected String generateModuleDescriptor(EAPModuleGraphNode node) {
        return templateBuilder.buildModuleDescriptor(node);
    }

    protected String generateModuleAssemblyComponent(EAPModuleGraphNode node, String moduleDescriptorPath, String outputPath) {
        return templateBuilder.buildModuleAssemblyComponent(node, moduleDescriptorPath, outputPath);
    }

    protected String generateLayersDescriptor(EAPModulesGraph graph) {
        return templateBuilder.buildLayersConfiguration(graph);
    }

}
