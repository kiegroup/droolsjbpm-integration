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
package org.kie.integration.eap.maven.eap;

import org.apache.maven.plugin.logging.Log;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.template.EAPVelocityTemplateBuilder;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.kie.integration.eap.maven.util.EAPFileUtils;
import org.kie.integration.eap.maven.util.EAPXMLUtils;
import org.eclipse.aether.artifact.Artifact;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to scan an JBoss EAP/AS module filesystem structure and generate the module definitions for the base modules.
 *
 * Behavior:
 * - Scan <EAP_ROOT>/modules directory
 * - For each module.xml:
 *   - Scan the module properties (name, slot, etc)
 *   - Scan the module resources
 *   - For each resource:
 *      - If resource is type jar, look for a pom.xml maven descriptor inside the jar file to generate the module resource definition.
 *      - If resource is type jar but no pom.xml found inside it, extract an artifactId and version properties by parsing the jar filename.
 */
public class EAPFileSystemBaseModulesScanner {

    private static final String TMP_PATH = System.getProperty("java.io.tmpdir");
    private static final String RESOURCE_GROUP_ID = "org.jboss.eap-module-base";
    private static final String MODULE_GROUP_ID = "org.kie";
    private static final String MODULE_ARTIFACT_ID_PREFFIX = "eap-module-";
    private static final String PACKAGING = "jar";
    private static final String POM_XML_NAME = "pom.xml";
    private static final String MODULE_XML_NAME = "module.xml";
    private static final String POM = "pom";
    private static final String SCOPE_SYSTEM = "system";
    private static final String MODULES_PATH = new StringBuilder("modules").append(File.separator).append("system").
            append(File.separator).append("layers").append(File.separator).
            append("base").toString();
    private static final String RESOURCES_PATH = new StringBuilder("src").append(File.separator).append("main").
            append(File.separator).append("resources").toString();
    /**
     * The pattern that allows obtaining the artifactId and the version.
     */
    private static final Pattern JAR_NAME_PARSER_PATTERN = Pattern.compile("(.*)-(\\d+[\\.-].*).jar");

    private static final String XPATH_POM_ARTIFACT_ID = "/project/artifactId";
    private static final String XPATH_POM_GROUP_ID = "/project/groupId";
    private static final String XPATH_POM_VERSION = "/project/version";
    private static final String XPATH_POM_PACKAGING = "/project/packaging";
    private static final String XPATH_POM_PARENT_GROUP_ID = "/project/parent/groupId";
    private static final String XPATH_POM_PARENT_VERSION = "/project/parent/version";

    private String eapRoot;
    private String eapName;
    private String outputPath;
    private String mavenModulesVersion;
    private String tempPath;
    private Log logger;

    public EAPFileSystemBaseModulesScanner(String eapRoot, String eapName, String mavenModulesVersion, String outputPath) {
        this.eapRoot = eapRoot;
        this.eapName = eapName;
        this.mavenModulesVersion = mavenModulesVersion;
        this.outputPath = outputPath;
    }

    public void generate() throws Exception {
        if (logger != null) logger.info("Starting the JBoss EAP/AS base modules generation process...");

        File eapRoot = new File(getEapRoot());

        if (!eapRoot.exists()) throw new IllegalArgumentException("Root JBoss EAP/AS path " + eapRoot + " does not exists.");
        if (eapName == null || eapName.trim().length() == 0) throw new IllegalArgumentException("The name for the EAP distribution (eapName property) cannot be null or empty.");
        if (mavenModulesVersion == null || mavenModulesVersion.trim().length() == 0) throw new IllegalArgumentException("The version for the EAP distribution (mavenModulesVersion property) cannot be null or empty.");

        File eapModulesRoot = new File(eapRoot, MODULES_PATH);
        if (!eapModulesRoot.exists()) throw new RuntimeException("Jboss EAP base modules root path " + eapModulesRoot + " not found.");

        File outputRoot = new File(outputPath);
        if (!outputRoot.exists()) throw new IllegalArgumentException("Output root path " + outputRoot + " does not exists.");
        outputRoot = new File(outputPath, eapName);

        if (tempPath != null && tempPath.trim().length() > 0) {
            File tempRoot = new File(tempPath);
            if (!tempRoot.exists()) throw new IllegalArgumentException("Temp root path " + tempRoot + " does not exists.");
        }

        // Iterate over all directories and subdirectories.
        Collection<EAPBaseModule> modules = new ArrayList<EAPBaseModule>();
        scan(eapModulesRoot, modules);

        // Resolve module aliases.
        // TODO: If duplicating the module resources for the alias, when loading module definitions, it produces a resource duplication exception.
        resolveAliases(modules);

        if (modules != null) {
            Collection<String> moduleNames = new ArrayList<String>();

            if (logger != null) logger.info("Total base modules scanned: " + modules.size());
            for (EAPBaseModule module : modules) {
                createModuleDescriptor(module, outputRoot);
                String moduleRootName = module.getMavenModuleName();
                moduleNames.add(moduleRootName);
            }

            // Create this eapName pom file..
            String modulesPomFileContent = EAPVelocityTemplateBuilder.buildBasicPom(MODULE_GROUP_ID, MODULE_ARTIFACT_ID_PREFFIX + eapName,
                    mavenModulesVersion,POM,"KIE EAP - " + eapName + " base modules",null,moduleNames, null, null);
            File modulesPomFile = new File(outputRoot, POM_XML_NAME);
            EAPFileUtils.writeToFile(modulesPomFileContent, modulesPomFile);
        }

        if (logger != null) logger.info("Generation process finished successfully.");
    }

    private void createModuleDescriptor(EAPBaseModule module, File outputRoot) throws IOException {
        if (logger != null) logger.info("Creating maven module for module " + module);

        String moduleRootName = module.getMavenModuleName();

        // Create the maven module root directory.
        File moduleRoot = new File(outputRoot, moduleRootName);
        moduleRoot.mkdirs();

        // Create pom artifact dependencies.
        HashMap<Artifact, File> resources = module.getResources();
        Collection<EAPVelocityTemplateBuilder.EAPBaseDependency> dependencies = null;
        if (resources != null && !resources.isEmpty()) {
            dependencies = new ArrayList<EAPVelocityTemplateBuilder.EAPBaseDependency>();

            File resourcesPomRoot = new File(moduleRoot, RESOURCES_PATH);
            resourcesPomRoot.mkdirs();

            // Copy pom in generated ouput maven module.
            for (Map.Entry<Artifact, File> entry : resources.entrySet()) {
                Artifact artifact = entry.getKey();
                File resourcePomTempFile = entry.getValue();

                String pomFileName = resourcePomTempFile.getName();
                File resourcesPom = new File(resourcesPomRoot, pomFileName);

                // Write the pom file.
                EAPFileUtils.writeToFile(new BufferedInputStream(new FileInputStream(resourcePomTempFile)), resourcesPom);

                EAPVelocityTemplateBuilder.EAPBaseDependency dependency = new EAPVelocityTemplateBuilder.EAPBaseDependency(artifact.getArtifactId(), artifact.getGroupId(),
                        artifact.getVersion(), POM, SCOPE_SYSTEM, pomFileName);
                dependencies.add(dependency);
            }
        }

        // Create the maven pom.xml file for this module.
        File modulePom = new File(moduleRoot, POM_XML_NAME);
        Map<String, String> moduleProperties = new HashMap<String, String>(4);
        moduleProperties.put("module.name", module.getName());
        moduleProperties.put("module.slot", module.getSlot());
        moduleProperties.put("module.type", "base");
        String modulePomContent = EAPVelocityTemplateBuilder.buildBasicPom(MODULE_GROUP_ID, MODULE_ARTIFACT_ID_PREFFIX + moduleRootName,
                mavenModulesVersion, POM, "EAP Base Module " + module, dependencies, null, new EAPVelocityTemplateBuilder.EAPParent(MODULE_ARTIFACT_ID_PREFFIX + eapName, MODULE_GROUP_ID, mavenModulesVersion), moduleProperties);

        EAPFileUtils.writeToFile(modulePomContent, modulePom);

        if (logger != null) logger.info("Maven module for module " + module + " created successfully.");
    }

    private void resolveAliases(Collection<EAPBaseModule> modules) {

        if (modules != null) {
            for (EAPBaseModule module : modules) {
                String moduleNameAlias = module.getModuleNameAlias();
                String moduleSlotAlias = module.getModuleSlotAlias();

                if (moduleNameAlias != null && moduleNameAlias.trim().length() > 0) {
                    if (logger != null) logger.info("Aliasing module '" + module.getName() + "' with '" + moduleNameAlias + ":" + moduleSlotAlias + "'.");
                    EAPBaseModule m = getModule(modules, moduleNameAlias, moduleSlotAlias);
                    if (m == null) throw new RuntimeException("Module alias not found.");
                    module.update(m);
                }

            }
        }

    }

    private EAPBaseModule getModule(Collection<EAPBaseModule> modules, String name, String slot) {
        if (modules != null) {
            for (EAPBaseModule module : modules) {
                if (name.equalsIgnoreCase(module.getName()) && slot.equalsIgnoreCase(module.getSlot())) return module;
            }
        }
        return null;
    }

    private void scan(File directory, Collection<EAPBaseModule>  result) throws Exception {

        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                scan(child, result);
            }
            else if (child.isFile() && child.getName().equalsIgnoreCase(MODULE_XML_NAME)) {
                EAPBaseModule m = scanModule(directory, child);
                result.add(m);
            }
        }

    }

    private EAPBaseModule scanModule(File directory, File moduleXML) throws Exception {
        if (logger != null) logger.info("Scanning module descrtpor '" + moduleXML + "'...");
        EAPBaseModule result = null;

        EAPXMLUtils xmlUtils = new EAPXMLUtils(new FileInputStream(moduleXML));
        Document root = xmlUtils.getDocument();
        if (root!= null) {
            result = new EAPBaseModule();
            String moduleName = null;
            String moduleSlot = null;
            NodeList moduleNodes = root.getElementsByTagName("module");
            if (moduleNodes != null && moduleNodes.getLength() > 0) {
                // First module element is the JBoss EAP module declaration. Read the module name.
                Node moduleNode = moduleNodes.item(0);
                moduleName = EAPXMLUtils.getAttributeValue(moduleNode, "name");
                moduleSlot = EAPXMLUtils.getAttributeValue(moduleNode, "slot");
                result.setName(moduleName);
                result.setSlot(moduleSlot);
                if (logger != null) logger.debug("Module name: '" + moduleName + "'");
                if (logger != null) logger.debug("Module slot: '" + moduleSlot + "'");
            } else {
                // Look for aliases.
                NodeList moduleAliasNodes = root.getElementsByTagName("module-alias");
                if (moduleAliasNodes != null && moduleAliasNodes.getLength() > 0) {
                    // First module element is the JBoss EAP module declaration. Read the module name.
                    Node moduleNode = moduleAliasNodes.item(0);
                    moduleName = EAPXMLUtils.getAttributeValue(moduleNode, "name");
                    moduleSlot= EAPXMLUtils.getAttributeValue(moduleNode, "slot");
                    String moduleAliasModule = EAPXMLUtils.getAttributeValue(moduleNode, "target-name");
                    String moduleAliasSlot = EAPXMLUtils.getAttributeValue(moduleNode, "target-slot");
                    result.setName(moduleName);
                    result.setSlot(moduleSlot);
                    result.setModuleNameAlias(moduleAliasModule);
                    result.setModuleSlotAlias(moduleAliasSlot);
                } else {
                    throw new RuntimeException("Module name/slot or aliases nor found for file " + moduleXML.getAbsolutePath());
                }
            }



            NodeList resourcesNode = root.getElementsByTagName("resource-root");
            if (resourcesNode != null && resourcesNode.getLength() > 0) {
                for (int i = 0; i < resourcesNode.getLength(); i++) {
                    Node node = resourcesNode.item(i);
                    String resourceName = EAPXMLUtils.getAttributeValue(node, "path");
                    if (logger != null) logger.debug("Found resource: '" + resourceName + "'");
                    File file = new File(directory, resourceName);
                    if (file.isFile() && resourceName.endsWith(PACKAGING)) scanModuleResource(result, directory, resourceName);
                }
            }
        }

        if (logger != null) logger.info("Scanning module descrtpor '" + moduleXML + "' complete." );
        return result;
    }

    private EAPBaseModule scanModuleResource(EAPBaseModule module, File directory, String name) throws Exception {
        if (logger != null) logger.info("Scanning resource: '" + name + "'...");

        Artifact artifact = null;
        File pomFile = null;
        String tmpPomFileName = getTempPath() + File.separator + name + "-pom.xml";

        File file = new File(directory, name);
        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> e = jarFile.entries();
        boolean pomFound = false;
        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            if (entry.getName().endsWith(POM_XML_NAME)) {
                InputStream is = jarFile.getInputStream(entry);
                pomFound = true;

                // Write pom to tmp file.
                EAPFileUtils.writeToFile(is, tmpPomFileName);
                pomFile = new File(tmpPomFileName);

                // XML Header is duplicated.
                EAPFileUtils.removeFirstLineIfDuplicated(pomFile);

                // Parse artifact coordinates.
                try {
                    artifact = scanPomCoordinates(pomFile);
                } catch (Exception e1) {
                    if (logger != null) logger.warn("The resource pom.xml file for resource '" + name + "' and module '" + module + "' cannot be parsed. The artifact coordinates for this resource will be extracted from jar name. Please review the generated pom.xml.");
                    pomFound = false;
                    pomFile.delete();
                }
            }
        }

        // pom file not present in Jar file. Generate a pom on the fly.
        if (!pomFound) {
            // Generate artifact coordinates from file name, as pom file have not been found.
            artifact = scanJarNameCoordinates(RESOURCE_GROUP_ID, PACKAGING, name);
            // Generate pom.
            String pomFileString = EAPVelocityTemplateBuilder.buildBasicPom(artifact.getGroupId(), artifact.getArtifactId(),
                    artifact.getVersion(), artifact.getExtension(), "EAP Base Module " + module, null, null, null, null);
            // String pomFileString = EAPArtifactUtils.getArtifactCoordinates(artifact);
            // Write pom to tmp file.
            EAPFileUtils.writeToFile(pomFileString, tmpPomFileName);
            pomFile = new File(tmpPomFileName);
        }

        // Add the result.
        module.getResources().put(artifact, pomFile);

        if (logger != null) logger.info("Scanning resource: '" + name + "' completed.");
        return module;
    }



    private Artifact scanPomCoordinates(File pomFile) throws Exception {
        EAPXMLUtils eapxmlUtils = new EAPXMLUtils(new FileInputStream(pomFile));

        Node groupIdNode = eapxmlUtils.getXPathNode(XPATH_POM_GROUP_ID);
        Node artifactIdNode = eapxmlUtils.getXPathNode(XPATH_POM_ARTIFACT_ID);
        Node versionNode = eapxmlUtils.getXPathNode(XPATH_POM_VERSION);
        Node packagingNode = eapxmlUtils.getXPathNode(XPATH_POM_PACKAGING);
        Node parentGroupIdNode = eapxmlUtils.getXPathNode(XPATH_POM_PARENT_GROUP_ID);
        Node parentVersionNode = eapxmlUtils.getXPathNode(XPATH_POM_PARENT_VERSION);

        String groupId = groupIdNode != null && groupIdNode.getFirstChild() != null ? groupIdNode.getFirstChild().getNodeValue() : null;
        if (groupId == null) {
            // Obtain from parent tag.
            groupId = parentGroupIdNode != null && parentGroupIdNode.getFirstChild() != null ? parentGroupIdNode.getFirstChild().getNodeValue() : "";
        }
        String artifactId = artifactIdNode != null && artifactIdNode.getFirstChild() != null ? artifactIdNode.getFirstChild().getNodeValue() : "";
        String version = versionNode != null && versionNode.getFirstChild() != null ? versionNode.getFirstChild().getNodeValue() : null;
        if (version == null) {
            // Obtain from parent tag.
            version = parentVersionNode != null && parentVersionNode.getFirstChild() != null ? parentVersionNode.getFirstChild().getNodeValue() : "";
        }
        String packaging = packagingNode != null && packagingNode.getFirstChild() != null ? packagingNode.getFirstChild().getNodeValue() : "";

        return EAPArtifactUtils.createArtifact(groupId, artifactId, version, packaging);
    }

    /**
     * Parses a jar resource name.
     * Extract artifactId and version coordinates.
     */
    private Artifact scanJarNameCoordinates(String groupId, String packaging, String jarName) {
        String[] result = new String[2];

        Matcher m1 = JAR_NAME_PARSER_PATTERN.matcher(jarName);
        boolean matches = m1.matches();

        if (!matches) {
            // throw new RuntimeException("Jar file '" + name + "' does not match a valid pattern parser.");
            if (logger != null) logger.warn("Cannot parse jar with name: " + jarName + ". Using whole name as artifactId.");
            result[0] = jarName.substring(0,jarName.length() - 4);
            result[1] = "";
        } else {
            result[0] = m1.group(1);
            result[1] = m1.group(2);
        }

        return EAPArtifactUtils.createArtifact(groupId, result[0], result[1], packaging);
    }

    public String getTempPath() {
        if (tempPath == null || tempPath.trim().length() == 0) return TMP_PATH;
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public String getEapRoot() {
        return eapRoot;
    }

    public Log getLogger() {
        return logger;
    }

    public static class EAPBaseModule {
        String name;
        String slot;
        HashMap<Artifact, File> resources;
        String moduleNameAlias;
        String moduleSlotAlias;

        private EAPBaseModule() {
            resources = new HashMap<Artifact, File>();
        }

        private String getSlot() {
            if (slot == null || slot.trim().length() == 0) return EAPConstants.SLOT_MAIN;
            return slot;
        }

        private void setSlot(String slot) {
            this.slot = slot;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private HashMap<Artifact, File> getResources() {
            return resources;
        }

        private void setResources(HashMap<Artifact, File> resources) {
            this.resources = resources;
        }

        private String getModuleNameAlias() {
            return moduleNameAlias;
        }

        private void setModuleNameAlias(String moduleNameAlias) {
            this.moduleNameAlias = moduleNameAlias;
        }

        private String getModuleSlotAlias() {
            if (moduleSlotAlias == null || moduleSlotAlias.trim().length() == 0) return "main";
            return moduleSlotAlias;
        }

        private void setModuleSlotAlias(String moduleSlotAlias) {
            this.moduleSlotAlias = moduleSlotAlias;
        }

        public void update(EAPBaseModule other) {
            this.resources = other.resources;
        }

        public String getMavenModuleName() {
            return new StringBuilder(getName().replaceAll("\\.","-")).
                    append("-").append(getSlot()).toString();
        }

        @Override
        public String toString() {
            if (name == null || name.trim().length() == 0) return super.toString();
            return new StringBuilder(name).append(":").append(getSlot()).toString();
        }
    }



}