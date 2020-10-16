/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.pmml.tester.regression;

import java.util.Arrays;
import java.util.Collection;

import net.pmml.tester.AbstractPMMLRuntimeProvider;
import net.pmml.tester.AbstractPMMLTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CategoricalVariablesRegressionTest extends AbstractPMMLTest {

    public static final String MODEL_NAME = "categoricalVariables_Model";
    private static final String FILE_NAME = "categoricalVariablesRegression.pmml";
    private static final String FULL_FILE_PATH = "categoricalvariablesregression/" + FILE_NAME;


    private static final Collection<Object[]> INPUT_DATA = Arrays.asList(new Object[][]{
            {"red", "classA"}, {"green", "classA"}, {"blue", "classA"}, {"orange", "classA"}, {"yellow", "classA"},
            {"red", "classB"}, {"green", "classB"}, {"blue", "classB"}, {"orange", "classB"}, {"yellow", "classB"},
            {"red", "classC"}, {"green", "classC"}, {"blue", "classC"}, {"orange", "classC"}, {"yellow", "classC"}
    });

    public static final Collection<Object[]> DATA = getDATA(INPUT_DATA, FILE_NAME, FULL_FILE_PATH);

    public CategoricalVariablesRegressionTest(String x, String y,
                                              AbstractPMMLRuntimeProvider abstractPMMLRuntimeProvider,
                                              String fileName) {
        super(abstractPMMLRuntimeProvider.getPMMLRuntime(MODEL_NAME, fileName));
        abstractPMMLExecutor = new CategoricalVariablesRegressionExecutor(x, y);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DATA;
    }
}
