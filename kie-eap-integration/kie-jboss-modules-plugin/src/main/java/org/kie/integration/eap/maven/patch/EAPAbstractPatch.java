package org.kie.integration.eap.maven.patch;

import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.kie.integration.eap.maven.util.EAPConstants;
import org.eclipse.aether.artifact.Artifact;

import java.util.*;

public abstract class EAPAbstractPatch implements EAPPatch {
    
    private String outputPath;
    private EAPArtifactsHolder artifactsHolder;


    /**
     * Returns the properties related to this patch.
     * @param p  The properties.
     * @return The properties related to this patch.
     */
    public Properties getPatchProperties(Properties p) {
        Properties result = null;
        
        if (p != null && !p.isEmpty()) {
            Enumeration<String> pNames = (Enumeration<String>) p.propertyNames();
            if (pNames != null) {
                String currentPatchPropertyName = getCurrentPatchModulePropertyName();
                result = new Properties();
                while (pNames.hasMoreElements()) {
                    String pName = pNames.nextElement();
                    if (pName != null && pName.startsWith(currentPatchPropertyName)) {
                        String pValue = (String) p.get(pName);
                        result.put(pName, pValue);
                    }
                }
            }
        }
        
        return result;
    }
    
    protected String getCurrentPatchModulePropertyName() {
        return new StringBuilder(EAPConstants.MODULE_PATCH_PREFFIX).append(getId()).toString();
    }
    
    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public EAPArtifactsHolder getArtifactsHolder() {
        return artifactsHolder;
    }

    public void setArtifactsHolder(EAPArtifactsHolder artifactsHolder) {
        this.artifactsHolder = artifactsHolder;
    }
}
