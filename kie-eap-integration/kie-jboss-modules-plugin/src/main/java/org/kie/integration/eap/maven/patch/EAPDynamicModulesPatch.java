package org.kie.integration.eap.maven.patch;

import org.apache.maven.plugin.MojoFailureException;
import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeResource;
import org.kie.integration.eap.maven.model.graph.EAPModulesGraph;
import org.kie.integration.eap.maven.model.module.EAPDynamicModule;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.template.EAPTemplateBuilder;
import org.kie.integration.eap.maven.template.assembly.EAPAssemblyTemplate;
import org.kie.integration.eap.maven.util.EAPArtifactUtils;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public abstract class EAPDynamicModulesPatch extends EAPAbstractPatch {

    protected EAPStaticLayerDistribution staticLayerDistribution;
    
    public void setStaticLayerDistribution(EAPStaticLayerDistribution staticLayerDistribution) {
        this.staticLayerDistribution = staticLayerDistribution;
    }

    public EAPStaticLayerDistribution getStaticLayerDistribution() {
        return staticLayerDistribution;
    }
    
    public abstract void execute(EAPModuleGraphNode node, Properties patchProperties) throws EAPPatchException;

    @Override
    public void execute() throws EAPPatchException {
        // Extract the modules that contain this webfragment patch definition.
        if (staticLayerDistribution == null) throw new EAPPatchException("The static layer is not set.", getId());
        if (staticLayerDistribution.getGraph() == null) throw new EAPPatchException("The static layer graph is not set.", getId());

        List<EAPModuleGraphNode> nodes = staticLayerDistribution.getGraph().getNodes();
        if (nodes != null && !nodes.isEmpty()) {
            for (EAPModuleGraphNode node : nodes) {
                // Check if the module contains a webfragment patch.
                Properties nodePatchProperties = getNodePatchProperties(node);
                if (nodePatchProperties != null && !nodePatchProperties.isEmpty()) {
                    execute(node, nodePatchProperties);
                }
            }
        }
    }

    /**
     * Returns the properties related to thsi path for a given module node.
     * @param node  The module node.
     * @return The properties related to this patch.
     */
    public Properties getNodePatchProperties(EAPModuleGraphNode node) {
        Properties p = node.getProperties();
        return getPatchProperties(p);
    }

    protected Artifact getArtifact(EAPModuleGraphNode module, String coordinates) {
        if (coordinates == null || coordinates.trim().length() == 0) return null;
        Artifact source = EAPArtifactUtils.createArtifact(coordinates);

        Collection<EAPModuleGraphNodeResource> resources = module.getResources();
        if (resources != null && !resources.isEmpty()) {
            for (EAPModuleGraphNodeResource resource : resources) {

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
        Lifecycle methods for dynamic module generation.
        // TODO: Implement pending methods.
     **/

    /**
     * Apply the patch for the module assembly descriptor.
     *
     * @param assemblyTemplate The assembly teamplte model.
     */
    public void patchAssembly(EAPDynamicModule module, EAPAssemblyTemplate assemblyTemplate) throws EAPPatchException {
        // To be overwritten.
    }
    
}
