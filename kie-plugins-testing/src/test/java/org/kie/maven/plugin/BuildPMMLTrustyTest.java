/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuildPMMLTrustyTest extends KieMavenPluginBaseIntegrationTest {

    private static final String PROJECT_NAME = "kjar-12-with-pmml-trusty";

    private static final String GAV_ARTIFACT_ID = "kie-maven-plugin-test-kjar-12";
    private static final String GAV_VERSION = "1.0.0.Final";


    private static final List<String> PMML_FILE_NAMES = new ArrayList<>();
    private static final List<String> EXAMPLE_PMML_CLASSES = new ArrayList();

    public BuildPMMLTrustyTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @BeforeClass
    public static void init() {
        PMML_FILE_NAMES.add("categoricalvariablesregression/categoricalVariablesRegression.pmml");
        PMML_FILE_NAMES.add("compoundnestedpredicatescorecard/CompoundNestedPredicateScorecard.pmml");
        PMML_FILE_NAMES.add("logisticregressionirisdata/logisticRegressionIrisData.pmml");
        PMML_FILE_NAMES.add("simplescorecardcategorical/SimpleScorecardCategorical.pmml");
        EXAMPLE_PMML_CLASSES.add("categoricalvariablesregression/CategoricalVariablesModel.class");
        EXAMPLE_PMML_CLASSES.add("categoricalvariablesregression/CategoricalVariablesRegressionFactory.class");
        EXAMPLE_PMML_CLASSES.add("categoricalvariablesregression/KiePMMLRegressionTableRegression1.class");

        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/CompoundNestedPredicateScorecard.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/CompoundNestedPredicateScorecardFactory.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/INPUT1.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/INPUT2.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/SCORE.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/PMMLRuleMapperImpl.class");
        EXAMPLE_PMML_CLASSES.add("compoundnestedpredicatescorecard/PMMLRuleMappersImpl.class");

        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/KiePMMLRegressionTableClassification1.class");
        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/KiePMMLRegressionTableRegression2.class");
        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/KiePMMLRegressionTableRegression3.class");
        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/KiePMMLRegressionTableRegression4.class");
        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/LogisticRegressionIrisData.class");
        EXAMPLE_PMML_CLASSES.add("logisticregressionirisdata/LogisticRegressionIrisDataFactory.class");

        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/INPUT1.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/INPUT2.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/SCORE.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/SimpleScorecardCategorical.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/SimpleScorecardCategoricalFactory.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/PMMLRuleMapperImpl.class");
        EXAMPLE_PMML_CLASSES.add("simplescorecardcategorical/PMMLRuleMappersImpl.class");
    }

    @Test
    public void testContentKjarWithPMML() throws Exception {
        final MavenExecutionResult mavenExecutionResult = buildKJarProject(PROJECT_NAME, new String[]{"-Dorg.kie.version=" + TestUtil.getProjectVersion()}, "clean", "install");
        mavenExecutionResult.assertErrorFreeLog();
        final File basedir = mavenExecutionResult.getBasedir();
        final File kjarFile = new File(basedir, "target/" + GAV_ARTIFACT_ID + "-" + GAV_VERSION + ".jar");
        Assertions.assertThat(kjarFile).exists();

        final JarFile jarFile = new JarFile(kjarFile);
        final Set<String> jarContent = new HashSet<>();
        final Enumeration<JarEntry> kjarEntries = jarFile.entries();
        while (kjarEntries.hasMoreElements()) {
            final String entryName = kjarEntries.nextElement().getName();
            jarContent.add(entryName);
        }

        Assertions.assertThat(jarContent).isNotEmpty();
        for (String pmmlFileName : PMML_FILE_NAMES) {
            Assertions.assertThat(jarContent).contains(pmmlFileName);
        }
        for (String examplePmmlClass : EXAMPLE_PMML_CLASSES) {
            Assertions.assertThat(jarContent).contains(examplePmmlClass);
        }
    }
}