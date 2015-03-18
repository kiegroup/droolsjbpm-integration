package org.kie.integration.eap.maven.patch;

import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;

import java.util.Collection;

/**
 * JBoss EAP/AS have some incompatibilities or issues.
 * A EAPPatch is a Java class used to fix these issues when building the modules distribution.
 */
public interface EAPPatch {

    /**
     * The unique identifier for the patch.
     * @return The unique identifier for the patch.
     */
    String getId();

    /**
     * The temporal output path for patch artifacts.
     * @param path The output path.
     */
    void setOutputPath(String path);
    
    /**
     * The artifacts holder instance.
     * @param artifactsHolder The artifacts holder instance.
     */
    void setArtifactsHolder(EAPArtifactsHolder artifactsHolder);
    
    /**
     * Used to check if this patch apply for a given container.
     * 
     * @param eap The JBoss EAP/AS container instance.
     * @return This patch apply for this container.
     */
    boolean doApply(EAPContainer eap);

    /**
     * After previous setters has been called, the execute method run the patches for the moodules.
     * @throws EAPPatchException
     */
    void execute() throws EAPPatchException;
    
}
