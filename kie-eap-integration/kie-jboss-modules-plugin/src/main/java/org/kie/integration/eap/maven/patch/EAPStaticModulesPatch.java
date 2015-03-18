package org.kie.integration.eap.maven.patch;

import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public abstract class EAPStaticModulesPatch extends EAPAbstractPatch {

    protected EAPLayer staticLayer;

    public void setStaticLayer(EAPLayer staticLayer) {
        this.staticLayer = staticLayer;
    }

    public EAPLayer getStaticLayer() {
        return staticLayer;
    }

    public abstract void execute(EAPModule module, Properties patchProperties) throws EAPPatchException;

    @Override
    public void execute() throws EAPPatchException {
        // Extract the modules that contain this webfragment patch definition.
        if (staticLayer == null) throw new EAPPatchException("The static layer is not set.", getId());

        Collection<EAPModule> nodes = staticLayer.getModules();
        if (nodes != null && !nodes.isEmpty()) {
            for (EAPModule node : nodes) {
                // Check if the module contains a webfragment patch.
                Properties nodePatchProperties = getModulePatchProperties(node);
                if (nodePatchProperties != null && !nodePatchProperties.isEmpty()) {
                    execute(node, nodePatchProperties);
                }
            }
        }
    }

    /**
     * Returns the properties related to thsi path for a given module.
     * @param module The module .
     * @return The properties related to this patch.
     */
    public Properties getModulePatchProperties(EAPModule module) {
        Properties p = module.getProperties();
        return getPatchProperties(p);
    }

    protected Artifact getArtifact(EAPModule module, String coordinates) {
        if (coordinates == null || coordinates.trim().length() == 0) return null;
        Artifact source = EAPArtifactUtils.createArtifact(coordinates);

        Collection<EAPModuleResource> resources = module.getResources();
        if (resources != null && !resources.isEmpty()) {
            for (EAPModuleResource resource : resources) {

                try {
                    Artifact resourceArtifact = (Artifact) resource.getResource();
                    
                    // Full coordinates matching.
                    if (EAPArtifactUtils.equals(source, resourceArtifact)) return resourceArtifact;
                    
                    // No version coordinates matching.
                    if (EAPArtifactUtils.equalsNoVersion(source, resourceArtifact)) return resourceArtifact;
                    
                } catch (ClassCastException e) {
                    // Resource is not an artifact.
                    continue;
                }


            }
        }
        
        return null;
    }

    /**
     *
     Lifecycle methods for static module generation.
     // TODO: Implement pending methods.
     **/

}
