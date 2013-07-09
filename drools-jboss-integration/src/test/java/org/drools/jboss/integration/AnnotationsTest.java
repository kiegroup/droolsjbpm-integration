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
import org.kie.workbench.common.services.datamodel.model.Annotation;
import org.kie.workbench.common.services.datamodel.model.ModelField;
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
import java.util.Set;

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

            String objectName = "GeneratedBean";
            String objectPackage = "a.b.c";
            String objectLabelValue = "Generated Bean";
            String objectDescriptionValue = "This is a programmatically added bean";
            String objectRoleValue = "EVENT";
            DataObjectTO dataObject = new DataObjectTO(objectName, objectPackage, null);
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, objectLabelValue);
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, objectDescriptionValue);
            dataObject.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.ROLE_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, objectRoleValue);
            dataModel.getDataObjects().add(dataObject);

            String fieldName = "simpleProperty";
            String fieldClass = "java.lang.String";
            String fieldLabelValue = "Simple Property";
            String fieldDescriptionValue = "This is a programmatically added String property";
            int fieldPositionValue = 1;
            ObjectPropertyTO baseTypeProp = new ObjectPropertyTO(fieldName, fieldClass, false, true);
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, fieldLabelValue);
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, fieldDescriptionValue);
            baseTypeProp.addAnnotation(annotationDefs.get(AnnotationDefinitionTO.POSITION_ANNOTATON), AnnotationDefinitionTO.VALUE_PARAM, fieldPositionValue);
            dataObject.setProperties(Arrays.asList(baseTypeProp));

            GenerationResult result = modelerService.saveModel(dataModel, project);
            assertNotNull(result);
            logger.info("**************************** -> Model saved in " + result.getGenerationTimeSeconds() + " seconds");

            invalidateDMOProjectCache.fire(new InvalidateDMOProjectCacheEvent(path));
            DataModelTO reloadedModel = modelerService.loadModel(project);
            assertNotNull(reloadedModel);

            ProjectDataModelOracle projectDataModelOracle = dataModelService.getProjectDataModel(path);
            String types[] = projectDataModelOracle.getFactTypes();
            if (types != null) {
                for (String type : types) {
                    if ((objectPackage + "." + objectName).equals(type)) {
                        // Check type annotations
                        Set<Annotation> annotations = projectDataModelOracle.getTypeAnnotations(type);
                        assertNotNull(annotations);
                        assertEquals("Type " + type + " should hold 3 annotations", 3, annotations.size());
                        checkAnnotation(annotations, AnnotationDefinitionTO.LABEL_ANNOTATION, objectLabelValue);
                        checkAnnotation(annotations, AnnotationDefinitionTO.DESCRIPTION_ANNOTATION, objectDescriptionValue);
                        checkAnnotation(annotations, AnnotationDefinitionTO.ROLE_ANNOTATION, objectRoleValue);

                        // Check type field annotations
                        ModelField[] fields = projectDataModelOracle.getModelFields().get(type);
                        assertNotNull(fields);
                        assertEquals("Type " + type + " should hold 1 field only", 2, fields.length);
                        Map<String, Set<Annotation>> mFieldAnnotations = projectDataModelOracle.getTypeFieldsAnnotations(type);
                        assertNotNull(mFieldAnnotations);
                        Set fieldAnnotations = mFieldAnnotations.get(fieldName);
                        assertNotNull(fieldAnnotations);
                        assertEquals("Field " + fieldName + "should have 3 annotations", 3, fieldAnnotations.size());
                        checkAnnotation(fieldAnnotations, AnnotationDefinitionTO.LABEL_ANNOTATION, fieldLabelValue);
                        checkAnnotation(fieldAnnotations, AnnotationDefinitionTO.DESCRIPTION_ANNOTATION, fieldDescriptionValue);
                        checkAnnotation(fieldAnnotations, AnnotationDefinitionTO.POSITION_ANNOTATON, Integer.toString(fieldPositionValue));
                    }
                }
            } else {
                fail("Test failed: error in fact types");
            }
        } catch (Throwable e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    private void checkAnnotation(Set<Annotation> annotations, String expectedType, String expectedValue) {
        if (annotations != null && expectedType != null && expectedValue != null) {
            boolean expectedTypeFound = false;
            for (Annotation a : annotations) {
                if (expectedType.equals(a.getQualifiedTypeName())) {
                    expectedTypeFound = true;
                    Map<String, String> attribs = a.getAttributes();
                    if (attribs != null && attribs.size() == 1) {
                        String value = attribs.get(AnnotationDefinitionTO.VALUE_PARAM);
                        if (value == null || !expectedValue.equals(value)) fail(expectedType + "annotation's value mismatch");
                    } else fail("Wrong attributes for Annotation " + expectedType);
                }
            }
            assertEquals("The Annotation of type" + expectedType + " was not present", true, expectedTypeFound);
        } else throw new IllegalArgumentException();
    }
}
