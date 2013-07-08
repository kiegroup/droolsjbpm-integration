package org.drools.jboss.integration;

import org.guvnor.common.services.project.builder.events.InvalidateDMOProjectCacheEvent;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.Path;
import org.kie.workbench.common.screens.datamodeller.model.*;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.kie.workbench.common.services.datamodel.oracle.ProjectDataModelOracle;
import org.kie.workbench.common.services.datamodel.service.DataModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


@Ignore
@RunWith(Arquillian.class)
public class AnnotationsTest extends FullDistributionTest {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationsTest.class);

    @Inject
    IOService ioService;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private DataModelerService modelerService;

    @Inject
    private ProjectService projectService;

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
                logger.info("project has no types");
            }

            assertEquals("Annotations test class: " + annotationTest + " wasn't loaded", true, isTestLoaded);
            assertEquals("Annotations test class: " + annotationTestSerializable + " wasn't loaded", true, isTestSerializableLoaded);

        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void test2() {
        try {
            assertNotNull(ioService);
            assertNotNull(dataModelService);
            assertNotNull(paths);
            assertNotNull(modelerService);
            assertNotNull(projectService);

            URI projectUri = new URI("default://master@uf-playground/GuvnorM2RepoDependencyExample2");

            Path projectPath = ioService.get(projectUri);
            assertNotNull(projectPath);

            org.uberfire.backend.vfs.Path path = paths.convert(projectPath);
            invalidateDMOProjectCache.fire(new InvalidateDMOProjectCacheEvent(path));

            Project project = projectService.resolveProject(path);

            Map<String, AnnotationDefinitionTO> annotationDefs = modelerService.getAnnotationDefinitions();

            DataModelTO dataModel = modelerService.loadModel(project);

            DataObjectTO dataObject = new DataObjectTO("GeneratedBean", "a.b.c", null);
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, "Generated Bean");
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, "This is a programmatically added bean");
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.ROLE_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, "EVENT");
            dataModel.getDataObjects().add(dataObject);

            ObjectPropertyTO baseTypeProp = new ObjectPropertyTO("simpleProperty", "java.lang.String", false, true);
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, "Simple Property");
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, "This is a programmatically added String property");
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.POSITION_ANNOTATON), AnnotationDefinitionTO.VALUE_PARAM, 1);
            dataObject.setProperties(Arrays.asList(baseTypeProp));

            GenerationResult result = modelerService.saveModel(dataModel, project);
            assertNotNull(result);
            logger.info("**************************** -> Model saved in " + result.getGenerationTimeSeconds() + " seconds");

            invalidateDMOProjectCache.fire(new InvalidateDMOProjectCacheEvent(path));

            DataModelTO reloadedModel = modelerService.loadModel(project);
            assertNotNull(reloadedModel);

        } catch (Throwable e) {
            fail("Test failed: " + e.getMessage());
        }
    }
}
