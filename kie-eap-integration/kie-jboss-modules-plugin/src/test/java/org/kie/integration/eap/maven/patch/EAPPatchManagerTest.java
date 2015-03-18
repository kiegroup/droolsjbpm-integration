package org.kie.integration.eap.maven.patch;

import junit.framework.Assert;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.integration.eap.maven.distribution.EAPStaticLayerDistribution;
import org.kie.integration.eap.maven.eap.EAPContainer;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNode;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.util.EAPArtifactsHolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EAPPatchManagerTest {

    private EAPPatchManager tested;
    private EAPStaticModulesPatch staticModulesPatch;
    private EAPDynamicModulesPatch dynamicModulesPatch;
    @Mock
    private EAPContainer container;
    @Mock
    private EAPArtifactsHolder artifactsHolder;
    @Mock
    private Collection<EAPModule> modules;
    @Mock
    private EAPStaticLayerDistribution staticLayerDistribution;
    @Mock
    private EAPLayer staticLayer;

    @Before
    public void setUp() throws Exception {
        // Init the annotated mocks.
        MockitoAnnotations.initMocks(this);
        
        // Create static and dynamic patch instances.
        staticModulesPatch = new EAPStaticModulesPatch() {
            @Override
            public String getId() {
                return "static";
            }

            @Override
            public boolean doApply(EAPContainer eap) {
                return true;
            }

            @Override
            public void execute(EAPModule module, Properties patchProperties) throws EAPPatchException {
                
            }
        };
        
        dynamicModulesPatch = new EAPDynamicModulesPatch() {
            @Override
            public String getId() {
                return "dynamic";
            }

            @Override
            public boolean doApply(EAPContainer eap) {
                return true;
            }

            @Override
            public void execute(EAPModuleGraphNode node, Properties patchProperties) throws EAPPatchException {
                
            }
            
        };
        
        Collection<EAPPatch> patches = new LinkedList<EAPPatch>();
        patches.add(staticModulesPatch);
        patches.add(dynamicModulesPatch);
        
        // Create the mocked container.
        when(container.getContainerId()).thenReturn(EAPContainer.EAPContainerId.EAP);
        when(container.getVersion()).thenReturn(new ComparableVersion("6.1.1"));
        
        // Create the tested instance.
        tested = new EAPPatchManager();
        tested.setPatches(patches.toArray(new EAPPatch[patches.size()]));
    }

    @Test
    public void testInit1() throws Exception {
        assertNotNull(tested.getStaticModulePatches());
        assertTrue(tested.getStaticModulePatches().size() == 1);
        assertNotNull(tested.getDynamicModulePatches());
        assertTrue(tested.getDynamicModulePatches().size() == 1);
    }
    
    @Test
    public void testInit2() throws Exception {
        String outputPath = "output-path-1";
        
        tested.initStatic(container, outputPath, artifactsHolder, staticLayer);
        tested.initDynamic(container, outputPath, artifactsHolder, staticLayerDistribution);
        
        EAPStaticModulesPatch resultStatic = tested.getStaticModulePatches().iterator().next();
        assertNotNull(resultStatic);
        assertEquals(resultStatic.getOutputPath(), outputPath);
        assertTrue(resultStatic.getArtifactsHolder() == artifactsHolder);
        assertTrue(resultStatic.getStaticLayer() == staticLayer);

        EAPDynamicModulesPatch resultDynamic = tested.getDynamicModulePatches().iterator().next();
        assertNotNull(resultDynamic);
        assertEquals(resultDynamic.getOutputPath(), outputPath);
        assertTrue(resultDynamic.getArtifactsHolder() == artifactsHolder);
        assertTrue(resultDynamic.getStaticLayerDistribution() == staticLayerDistribution);
    }

    // TODO: @Test
    public void testExecute() throws Exception {
        
    }

    @After
    public void tearDown() throws Exception {

    }
    
}
