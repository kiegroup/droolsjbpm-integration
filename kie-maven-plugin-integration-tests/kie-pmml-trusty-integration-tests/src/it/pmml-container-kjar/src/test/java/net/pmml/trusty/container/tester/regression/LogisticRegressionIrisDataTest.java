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

package net.pmml.trusty.container.tester.regression;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.pmml.api.runtime.PMMLRuntime;
import net.pmml.trusty.container.tester.AbstractPMMLTest;

@RunWith(Parameterized.class)
public class LogisticRegressionIrisDataTest extends AbstractPMMLTest {

    public static final String MODEL_NAME = "LogisticRegressionIrisData";
    public static final String FILE_NAME = "logisticregressionirisdata/logisticRegressionIrisData.pmml";

    public static final Collection<Object[]> DATA = Arrays.asList(new Object[][]{
            {6.9, 3.1, 5.1, 2.3, "virginica"},
            {5.8, 2.6, 4.0, 1.2, "versicolor"},
            {5.7, 3.0, 4.2, 1.2, "versicolor"},
            {5.0, 3.3, 1.4, 0.2, "setosa"},
            {5.4, 3.9, 1.3, 0.4, "setosa"}});
    private static PMMLRuntime localPMMLRuntime;

    static {
        localPMMLRuntime = getPMMLRuntime(MODEL_NAME, FILE_NAME);
    }

    public LogisticRegressionIrisDataTest(double sepalLength, double sepalWidth, double petalLength,
                                          double petalWidth, String expectedResult) {
        super(localPMMLRuntime);
        abstractPMMLExecutor = new LogisticRegressionIrisDataExecutor(sepalLength, sepalWidth, petalLength,
                                                                      petalWidth, expectedResult);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return DATA;
    }
}
