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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.SortTool;
import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplate;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplateFile;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component( role = EAPTemplateBuilder.class, hint = "velocity" )
public class EAPVelocityTemplateBuilder implements EAPTemplateBuilder {

    private static final String TEMPLATES_STATIC_PATH = "/org/kie/integration/eap/maven/assembly/templates/static/";
    private static final String TEMPLATES_DYNAMIC_PATH = "/org/kie/integration/eap/maven/assembly/templates/dynamic/";
    private static final String TEMPLATES_BASE_PATH = "/org/kie/integration/eap/base/";
    private static final String TEMPLATE_BASIC_POM = "pom.vm";
    private static final String TEMPLATE_MODULE_DESCRIPTOR = "module.vm";
    private static final String TEMPLATE_LAYERS_DESCRIPTOR = "layers.vm";
    private static final String TEMPLATE_ASSEMBLY_COMPONENT = "assembly-component.vm";
    private static final String TEMPLATE_ASSEMBLY = "assembly.vm";
    private static final String TEMPLATE_JBOSS_DEP_STRUCTURE = "jboss-deployment-structure.vm";
    private static final String TEMPLATE_JBOSS_ALL = "jboss-all.vm";
    private static final String VM_CONTEXT_NODE = "node";
    private static final String VM_CONTEXT_MODULE_PATH = "moduleDescriptorPath";
    private static final String VM_CONTEXT_OUTPUT_PATH = "outputDir";
    private static final String VM_CONTEXT_LAYER_NAME = "layerName";
    private static final String VM_DEPENDENCIES = "dependencies";
    private static final String VM_CONTEXT_LAYER_ID = "layerId";
    private static final String VM_CONTEXT_LAYER_FORMATS = "formats";
    private static final String VM_CONTEXT_LAYER_DESC_FILE = "layersDescriptorFile";
    private static final String VM_CONTEXT_LAYER_COMPONENTS = "components";
    private static final String VM_CONTEXT_INCLUDE = "include";
    private static final String VM_CONTEXT_EXCLUSIONS = "exclusions";
    private static final String VM_CONTEXT_FILES = "files";
    private static final String VM_CONTEXT_ARTIFACT_ID = "artifactId";
    private static final String VM_CONTEXT_GROUP_ID = "groupId";
    private static final String VM_CONTEXT_VERSION = "version";
    private static final String VM_CONTEXT_PACKAGING = "packaging";
    private static final String VM_CONTEXT_NAME = "name";
    private static final String VM_CONTEXT_MODULES = "modules";
    private static final String VM_CONTEXT_PARENT = "parent";
    private static final String VM_CONTEXT_PROPERTIES = "properties";

    private static VelocityEngine velocityEngine = new VelocityEngine();;

    private static Template moduleDescriptorTemplate;
    private static Template layersDescriptorTemplate;
    private static Template assemblyComponentTemplate;
    private static Template assemblyTemplate;
    private static Template jBossDepStructureTemplate;
    private static Template assemblyDynamicTemplate;
    private static Template jbossAllTemplate;

    static {
        init(velocityEngine);
        initTemplates();
    }

    public EAPVelocityTemplateBuilder() {

    }

    @Override
    public String buildLayersConfiguration(EAPModulesGraph graph) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_LAYER_NAME, graph.getDistributionName());
        layersDescriptorTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildGlobalAssembly(String layerId, String[] formats, String layerDescriptorFilePath, String[] componentDescriptorsFilePaths) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_LAYER_ID, layerId);
        context.put(VM_CONTEXT_LAYER_FORMATS, formats);
        context.put(VM_CONTEXT_LAYER_DESC_FILE, layerDescriptorFilePath);
        context.put(VM_CONTEXT_LAYER_COMPONENTS, componentDescriptorsFilePaths);
        assemblyTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildModuleAssemblyComponent(EAPModuleGraphNode node, String moduleDescriptorPath, String outputPath) {
        VelocityContext context = createContext(node);
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_OUTPUT_PATH, outputPath);
        context.put(VM_CONTEXT_MODULE_PATH, moduleDescriptorPath);

        assemblyComponentTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildModuleDescriptor(EAPModuleGraphNode node) {
        VelocityContext context = createContext(node);
        StringWriter writer = new StringWriter();

        moduleDescriptorTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildJbossDeploymentStructure(Collection<? extends EAPModuleGraphNodeDependency> dependencies) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_DEPENDENCIES, dependencies);
        jBossDepStructureTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildDynamicModuleAssembly(EAPAssemblyTemplate assemblyTemplate) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_LAYER_ID, assemblyTemplate.getId());
        context.put(VM_CONTEXT_LAYER_FORMATS, assemblyTemplate.getFormats());
        // TODO: Iterate over all includes in velocity template.
        context.put(VM_CONTEXT_INCLUDE, assemblyTemplate.getInclusions().iterator().next());
        context.put(VM_CONTEXT_EXCLUSIONS, assemblyTemplate.getExclusions());
        context.put(VM_CONTEXT_FILES, assemblyTemplate.getFiles());
        assemblyDynamicTemplate.merge(context, writer);

        return writer.toString();
    }

    @Override
    public String buildDynamicModuleDependency(String name) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_NAME, name);
        jbossAllTemplate.merge(context, writer);

        return writer.toString();
    }

    public static String buildBasicPom(String groupId, String artifactId, String version, String packaging, String name, Collection<EAPBaseDependency> dependencies,
                                       Collection<String> modules, EAPParent parent, Map<String, String> properties) {
        VelocityContext context = createContext();
        StringWriter writer = new StringWriter();

        context.put(VM_CONTEXT_ARTIFACT_ID, artifactId);
        context.put(VM_CONTEXT_VERSION, version);
        context.put(VM_CONTEXT_GROUP_ID, groupId);
        context.put(VM_CONTEXT_PACKAGING, packaging);
        context.put(VM_CONTEXT_NAME, name);
        context.put(VM_CONTEXT_PARENT, parent);
        if (dependencies != null) context.put(VM_DEPENDENCIES, dependencies);
        else context.put(VM_DEPENDENCIES, Collections.emptyList());
        if (modules != null) context.put(VM_CONTEXT_MODULES, modules);
        else context.put(VM_CONTEXT_MODULES, Collections.emptyList());
        if (properties != null) context.put(VM_CONTEXT_PROPERTIES, properties);
        else context.put(VM_CONTEXT_PROPERTIES, Collections.emptyMap());

        Template tempalte =  velocityEngine.getTemplate(new StringBuilder(TEMPLATES_BASE_PATH).append(TEMPLATE_BASIC_POM).toString());
        tempalte.merge(context, writer);

        return writer.toString();
    }

    private static void init(VelocityEngine velocityEngine) {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    private static void initTemplates() {
        if (moduleDescriptorTemplate == null) moduleDescriptorTemplate = getTemplate(TEMPLATE_MODULE_DESCRIPTOR, TEMPLATES_STATIC_PATH);
        if (layersDescriptorTemplate == null) layersDescriptorTemplate = getTemplate(TEMPLATE_LAYERS_DESCRIPTOR, TEMPLATES_STATIC_PATH);
        if (assemblyComponentTemplate == null) assemblyComponentTemplate = getTemplate(TEMPLATE_ASSEMBLY_COMPONENT, TEMPLATES_STATIC_PATH);
        if (assemblyTemplate == null) assemblyTemplate = getTemplate(TEMPLATE_ASSEMBLY, TEMPLATES_STATIC_PATH);
        if (jBossDepStructureTemplate == null) jBossDepStructureTemplate = getTemplate(TEMPLATE_JBOSS_DEP_STRUCTURE, TEMPLATES_DYNAMIC_PATH);
        if (assemblyDynamicTemplate == null) assemblyDynamicTemplate = getTemplate(TEMPLATE_ASSEMBLY, TEMPLATES_DYNAMIC_PATH);
        if (jbossAllTemplate == null) jbossAllTemplate = getTemplate(TEMPLATE_JBOSS_ALL, TEMPLATES_DYNAMIC_PATH);
    }

    protected static Template getTemplate(String name, String path) {
        return velocityEngine.getTemplate(new StringBuilder(path).append(name).toString());
    }

    protected VelocityContext createContext(EAPModuleGraphNode node) {
        VelocityContext context = createContext();
        context.put(VM_CONTEXT_NODE, node);
        return context;
    }

    protected static VelocityContext createContext() {
        VelocityContext context = new VelocityContext();
        context.put("sorter", new SortTool());
        return context;
    }

    public static class EAPBaseDependency {
        private String artifactId;
        private String groupId;
        private String version;
        private String type;
        private String scope;
        private String systemPath;

        public EAPBaseDependency(String artifactId, String groupId, String version, String type, String scope, String systemPath) {
            this.artifactId = artifactId;
            this.groupId = groupId;
            this.version = version;
            this.type = type;
            this.scope = scope;
            this.systemPath = systemPath;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getVersion() {
            return version;
        }

        public String getType() {
            return type;
        }

        public String getScope() {
            return scope;
        }

        public String getSystemPath() {
            return systemPath;
        }
    }

    public static class EAPParent{
        private String artifactId;
        private String groupId;
        private String version;

        public EAPParent(String artifactId, String groupId, String version) {
            this.artifactId = artifactId;
            this.groupId = groupId;
            this.version = version;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

}
