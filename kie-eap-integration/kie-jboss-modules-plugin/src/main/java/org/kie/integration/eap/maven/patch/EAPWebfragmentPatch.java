package org.kie.integration.eap.maven.patch;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.module.EAPDynamicModule;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplate;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplateFile;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.kie.integration.eap.maven.util.EAPFileUtils;
import org.kie.integration.eap.maven.util.EAPXMLUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This patch impl is concerning webfragment descriptor files (servlet spec 3.0)
 * If a JAR resource contains a <code>webfragment.xml</code> file, and this JAR is placed as a static module resource, this webfragment is not loaded by the JBoss container.
 * So, this patch extracts the <code>webfragment.xml</code> file from JAR artifact and creates a new JAR "on the fly" adding this descriptor. Then, the new generated JAR file is added into the webapp that is used with the current modules distribution.  
 */
@Component( role = EAPPatch.class, hint = "webfragment" )
public class EAPWebfragmentPatch extends EAPDynamicModulesPatch {
    
    private static final String ID = "dynamic.webfragment";
    private static final String WF_ENTRY_NAME = "META-INF/web-fragment.xml";
    private static final ComparableVersion EAP_VERSION = new ComparableVersion("6.1.0");
    private static final ComparableVersion AS_VERSION = new ComparableVersion("7.0");
    
    private Collection<EAPWebfragment> webfragments;

    /**
     * Default constructor.
     */
    public EAPWebfragmentPatch() {
        webfragments = new LinkedList<EAPWebfragment>();
    }
    

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean doApply(EAPContainer eap) {
        ComparableVersion version = eap.getVersion();
        EAPContainer.EAPContainerId containerId = eap.getContainerId();
        
        // Apply for EAP >= 6.1.0
        if (containerId.equals(EAPContainer.EAPContainerId.EAP) && 
                (EAPArtifactUtils.isVersionEqualsThan(version, EAP_VERSION) || EAPArtifactUtils.isVersionGreaterThan(version, EAP_VERSION))) {
            return true;
        }

        // Apply for AS >= 7.0
        if (containerId.equals(EAPContainer.EAPContainerId.AS) &&
                (EAPArtifactUtils.isVersionEqualsThan(version, AS_VERSION) || EAPArtifactUtils.isVersionGreaterThan(version, AS_VERSION))) {
            return true;
        }
        
        return false;
    }

    @Override
    public void execute(EAPModuleGraphNode node, Properties patchProperties) throws EAPPatchException {
        // This patch only can contain a single property with a comma separated list of artifact coordinates.
        String artifactCoordinates = patchProperties.getProperty(getCurrentPatchModulePropertyName());
        if (artifactCoordinates == null || artifactCoordinates.trim().length() == 0) throw new EAPPatchException("The property value for this patch is null or empty", ID);

        String[] artifacts = artifactCoordinates.split(EAPConstants.COMMA);
        for (String artifact : artifacts) {
            EAPWebfragment wf = new EAPWebfragment(node, artifact);

            // Build the webfragment JAR file.
            wf.build();

            // Add to the current webfragments list.
            webfragments.add(wf);
        }
    }

    /**
     * Add the webfragment generated JAR as file inclusion in the module assembly descriptor.
     * 
     * @param assemblyTemplate The assembly teamplte model.
     */
    @Override
    public void patchAssembly(EAPDynamicModule module, EAPAssemblyTemplate assemblyTemplate) throws EAPPatchException {
        super.patchAssembly(module, assemblyTemplate);
        
        if (!webfragments.isEmpty()) {
            for (final EAPWebfragment wf : webfragments) {
                String nodeUID = wf.getNode().getUniqueId();

                // Check if this webapp depends on the webfragment artifacts.
                if (module.getDependency(nodeUID) != null) {

                    // Geneate the file inclusion for the webfragment.
                    // Add a fileset - file inclusion for the generated webfragment JAR file.
                    EAPAssemblyTemplateFile wfFile = new EAPAssemblyTemplateFile() {
                        @Override
                        public String getSource() {
                            return wf.getOutputPath();
                        }

                        @Override
                        public String getOutputDirectory() {
                            return EAPConstants.WEB_INF_LIB;
                        }

                        @Override
                        public String getFinalName() {
                            return wf.getFinalName();
                        }

                        @Override
                        public boolean isFiltered() {
                            return false;
                        }
                    };

                    // Add the file inclusion in the assembly descriptor.
                    assemblyTemplate.getFiles().add(wfFile);
                    
                }
            }
        }
    }

    private class EAPWebfragment {
        private static final String WEBFRAGMENT_JAR_SUFFIX = "-webfragment.jar";
        private EAPModuleGraphNode node;
        private String artifactCoordinates;
        private String outputPath;

        private EAPWebfragment(EAPModuleGraphNode node, String artifactCoordinates) {
            this.node = node;
            this.artifactCoordinates = artifactCoordinates;
        }

        /**
         * Geneates a new JAR on the fly with the webfragment file contained in the artifact.
         */
        public void build() throws EAPPatchException {
            // Resolve the artifact that contains the webfragment descriptor.
            Artifact artifact = getArtifact(node, artifactCoordinates);
            if (artifact == null) throw new EAPPatchException("Cannot resolve the artifact with coordinates '" + artifactCoordinates + "' in current module.", ID);
            
            // Resolve the artifact.
            Artifact jarArtifact = null;
            try {
                jarArtifact = getArtifactsHolder().resolveArtifact(artifact);
            } catch (ArtifactResolutionException e) {
                throw new EAPPatchException("Cannot resolve the artifact with coordinates '" + artifactCoordinates + "'.", e, ID);
            }

            File outPath = new File(EAPWebfragmentPatch.this.getOutputPath());
            outPath.mkdirs();
            
            // Extract the webfragment file.
            boolean found = false;
            ZipFile warFile = null;
            File outXmlFile = null;
            try {
                warFile = new ZipFile(jarArtifact.getFile(), ZipFile.OPEN_READ);
                for (Enumeration e = warFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
    
                    if (entry.getName().equalsIgnoreCase(WF_ENTRY_NAME)) {
                        found = true;
                        outXmlFile = new File(outPath, getFinalName() + ".xml");
                        InputStream in = warFile.getInputStream(entry);
                        EAPFileUtils.writeToFile(in, outXmlFile);
                    }
    
                }
            } catch (IOException e) {
                throw new EAPPatchException("Error extracting webfragment descriptor for  '" + warFile.getName() + "'.", e, ID);
            }

            if (!found) throw new EAPPatchException("Webfragment descriptor not found in '" + warFile.getName() + "'.", ID);
            
            // Generate a new JAR file and add the webfragment.
            try {
                if (outXmlFile != null) {
                    File outJarFile = EAPFileUtils.createJarFile(EAPWebfragmentPatch.this.getOutputPath(), getFinalName(), outXmlFile, WF_ENTRY_NAME);
                    outputPath = outJarFile.getAbsolutePath();    
                }
            } catch (IOException e) {
                throw new EAPPatchException("Error creating the resulting JAR file", e, ID);
            }
        }
        
        public String getFinalName() {
            return new StringBuilder(artifactCoordinates.replaceAll(":","-")).
                    append(WEBFRAGMENT_JAR_SUFFIX).toString();
        }
        
        public EAPModuleGraphNode getNode() {
            return node;
        }

        public String getArtifactCoordinates() {
            return artifactCoordinates;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }
    }
}
