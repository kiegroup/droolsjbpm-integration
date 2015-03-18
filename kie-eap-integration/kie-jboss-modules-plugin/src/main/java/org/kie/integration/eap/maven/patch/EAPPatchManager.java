package org.kie.integration.eap.maven.patch;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;

import java.util.Collection;
import java.util.LinkedList;

@Component( role = EAPPatchManager.class )
public class EAPPatchManager {
    
    private EAPPatch patches[];
    private Collection<EAPStaticModulesPatch> staticPatches;
    private Collection<EAPDynamicModulesPatch> dynamicPatches;
    private EAPContainer container;
    
    public EAPPatchManager() {
        staticPatches = new LinkedList<EAPStaticModulesPatch>();
        dynamicPatches = new LinkedList<EAPDynamicModulesPatch>();
        init_patches();
        init();
    }
    
    // TODO: Replace the injection using plexus container.
    private void init_patches() {
        patches = new EAPPatch[] {new EAPWebfragmentPatch()};
    }

    /**
     * Internal initialize.
     */
    private void init() {
        
        // Clear the patch list.
        staticPatches.clear();
        dynamicPatches.clear();
        
        // Populate the patch list.
        for (EAPPatch patch : patches) {
            try {
                EAPStaticModulesPatch _patch = (EAPStaticModulesPatch) patch;
                staticPatches.add(_patch);
            } catch (ClassCastException e) {
                // It's not a static one.
            }

            try {
                EAPDynamicModulesPatch _patch = (EAPDynamicModulesPatch) patch;
                dynamicPatches.add(_patch);
            } catch (ClassCastException e) {
                // It's not a dynamic one.
            }
        }
    }

    public void initDynamic(EAPContainer  container, String outputPath, EAPArtifactsHolder artifactsHolder, EAPStaticLayerDistribution staticLayerDistribution) {
        this.container = container;
        for (EAPDynamicModulesPatch patch : dynamicPatches) {
            if (patch.doApply(container)) {
                patch.setOutputPath(outputPath);
                patch.setArtifactsHolder(artifactsHolder);
                patch.setStaticLayerDistribution(staticLayerDistribution);
                
            }
        }
    }

    public void initStatic(EAPContainer  container, String outputPath, EAPArtifactsHolder artifactsHolder, EAPLayer layer) {
        this.container = container;
        for (EAPStaticModulesPatch patch : staticPatches) {
            if (patch.doApply(container)) {
                patch.setOutputPath(outputPath);
                patch.setArtifactsHolder(artifactsHolder);
                patch.setStaticLayer(layer);

            }
        }
    }

    public void executeAll() throws EAPPatchException {
        for (EAPPatch patch : patches) {
            execute(patch);
        }
    }

    public void executeDynamic() throws EAPPatchException {
        for (EAPPatch patch : dynamicPatches) {
            execute(patch);
        }
    }
    
    public void iterateDynamic(EAPPatchRunnable runnable) throws EAPPatchException {
        for (EAPPatch patch : dynamicPatches) {
            runnable.execute(patch);
        }
    }

    public void iterateStatic(EAPPatchRunnable runnable) throws EAPPatchException {
        for (EAPPatch patch : staticPatches) {
            runnable.execute(patch);
        }
    }

    public void executeStatic() throws EAPPatchException {
        for (EAPPatch patch : staticPatches) {
            execute(patch);
        }
    }
    
    protected void execute(EAPPatch patch) throws EAPPatchException {
        if (patch.doApply(container)) patch.execute();
    }

    public EAPPatch[] getPatches() {
        return patches;
    }

    public Collection<EAPStaticModulesPatch> getStaticModulePatches() {
        return staticPatches;
    }
    
    public Collection<EAPDynamicModulesPatch> getDynamicModulePatches() {
        return dynamicPatches;
    }

    public void setPatches(EAPPatch[] patches) {
        this.patches = patches;
        init();
    }
    
    public static interface EAPPatchRunnable {
        void execute(EAPPatch patch) throws EAPPatchException;
    }
}
