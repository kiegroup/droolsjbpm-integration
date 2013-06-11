package org.drools.jboss.integration;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.Path;
import org.kie.workbench.common.services.datamodel.events.InvalidateDMOProjectCacheEvent;
import org.kie.workbench.common.services.datamodel.oracle.ProjectDataModelOracle;
import org.kie.workbench.common.services.datamodel.service.DataModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


@Ignore @RunWith(Arquillian.class)
public class AnnotationsTest extends FullDistributionTest {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationsTest.class);

    @Inject
    IOService ioService;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private Paths paths;

    @Inject
    private Event<InvalidateDMOProjectCacheEvent> invalidateDMOProjectCache;

    @Test
    public void test() {

        try {

            assertNotNull(ioService);
            assertNotNull(dataModelService);
            assertNotNull(paths);

            URI projectUri = null;
            projectUri = new URI("default://master@uf-playground/GuvnorM2RepoDependencyExample1");

            Path projectPath = ioService.get(projectUri);
            assertNotNull(projectPath);

            invalidateDMOProjectCache.fire(new InvalidateDMOProjectCacheEvent(paths.convert(projectPath)));

            ProjectDataModelOracle projectDataModelOracle = dataModelService.getProjectDataModel(paths.convert(projectPath));

            String annotationTest = "org.kie.test.AnnotationsBean";
            String annotationTestSerializable = "org.kie.test.AnnotationsBeanSerializable";

            boolean isTestLoaded = false;
            boolean isTestSerializableLoaded = false;

            String types[] = projectDataModelOracle.getFactTypes();
            if (types != null) {
                for (int i = 0; i < types.length; i++) {
                    logger.info("**************************** -> Loading type: " + types[i]);
                    if (annotationTest.equals(types[i])) isTestLoaded = true;
                    if (annotationTestSerializable.equals(types[i])) isTestSerializableLoaded = true;

                }
            } else {
                logger.info("project has not types");
            }

            assertEquals("Annotations test class: " + annotationTest + " wasn't loaded", true, isTestLoaded);
            assertEquals("Annotations test class: " + annotationTestSerializable + " wasn't loaded", true, isTestSerializableLoaded);

        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }

    }
}
