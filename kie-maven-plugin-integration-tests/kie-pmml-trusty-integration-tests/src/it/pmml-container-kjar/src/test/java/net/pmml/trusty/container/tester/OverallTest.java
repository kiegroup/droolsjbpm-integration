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
package net.pmml.trusty.container.tester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import net.pmml.trusty.container.tester.regression.CategoricalVariablesRegressionExecutor;
import net.pmml.trusty.container.tester.regression.CategoricalVariablesRegressionTest;
import net.pmml.trusty.container.tester.regression.LogisticRegressionIrisDataExecutor;
import net.pmml.trusty.container.tester.regression.LogisticRegressionIrisDataTest;
import net.pmml.trusty.container.tester.scorecard.CompoundNestedPredicateScorecardExecutor;
import net.pmml.trusty.container.tester.scorecard.CompoundNestedPredicateScorecardTest;
import net.pmml.trusty.container.tester.scorecard.SimpleScorecardCategoricalExecutor;
import net.pmml.trusty.container.tester.scorecard.SimpleScorecardCategoricalTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.pmml.api.runtime.PMMLRuntime;

@RunWith(Parameterized.class)
public class OverallTest extends AbstractPMMLTest {

    private static Collection<ExecutorRuntimeTupla> DATA;

    static {
        DATA = new ArrayList<>();
        final PMMLRuntime categoricalVariablesRegressionRuntime = getPMMLRuntime(CategoricalVariablesRegressionTest.MODEL_NAME, CategoricalVariablesRegressionTest.FILE_NAME);
        CategoricalVariablesRegressionTest.DATA.forEach(new Consumer<Object[]>() {
            @Override
            public void accept(Object[] objects) {
                AbstractPMMLExecutor executor = new CategoricalVariablesRegressionExecutor((String) objects[0], (String) objects[1]);
                DATA.add(new ExecutorRuntimeTupla(executor, categoricalVariablesRegressionRuntime));
            }
        });
        final PMMLRuntime logisticRegressionIrisDataRuntime = getPMMLRuntime(LogisticRegressionIrisDataTest.MODEL_NAME, LogisticRegressionIrisDataTest.FILE_NAME);
        LogisticRegressionIrisDataTest.DATA.forEach(new Consumer<Object[]>() {
            @Override
            public void accept(Object[] objects) {
                AbstractPMMLExecutor executor = new LogisticRegressionIrisDataExecutor((Double) objects[0],
                                                                                       (Double) objects[1],
                                                                                       (Double) objects[2],
                                                                                       (Double) objects[3],
                                                                                       (String) objects[4]);
                DATA.add(new ExecutorRuntimeTupla(executor, logisticRegressionIrisDataRuntime));
            }
        });
        final PMMLRuntime compoundNestedPredicateScorecardRuntime = getPMMLRuntime(CompoundNestedPredicateScorecardTest.MODEL_NAME, CompoundNestedPredicateScorecardTest.FILE_NAME);
        CompoundNestedPredicateScorecardTest.DATA.forEach(new Consumer<Object[]>() {
            @Override
            public void accept(Object[] objects) {
                AbstractPMMLExecutor executor = new CompoundNestedPredicateScorecardExecutor((Double) objects[0],
                                                                                             (String) objects[1],
                                                                                             (Double) objects[2],
                                                                                             (String) objects[3],
                                                                                             (String) objects[4]);
                DATA.add(new ExecutorRuntimeTupla(executor, compoundNestedPredicateScorecardRuntime));
            }
        });
        final PMMLRuntime simpleScorecardCategoricalRuntime = getPMMLRuntime(SimpleScorecardCategoricalTest.MODEL_NAME, SimpleScorecardCategoricalTest.FILE_NAME);
        SimpleScorecardCategoricalTest.DATA.forEach(new Consumer<Object[]>() {
            @Override
            public void accept(Object[] objects) {
                AbstractPMMLExecutor executor = new SimpleScorecardCategoricalExecutor((String) objects[0],
                                                                                       (String) objects[1],
                                                                                       (Double) objects[2],
                                                                                       (String) objects[3],
                                                                                       (String) objects[4]);
                DATA.add(new ExecutorRuntimeTupla(executor, simpleScorecardCategoricalRuntime));
            }
        });
    }

    public OverallTest(ExecutorRuntimeTupla executorTupla) {
        super(executorTupla.pmmlRuntime);
        abstractPMMLExecutor = executorTupla.executor;
    }

    @Parameterized.Parameters
    public static Collection<ExecutorRuntimeTupla> data() {
        return DATA;
    }

    private static class ExecutorRuntimeTupla {

        private AbstractPMMLExecutor executor;
        private PMMLRuntime pmmlRuntime;

        public ExecutorRuntimeTupla(AbstractPMMLExecutor executor, PMMLRuntime pmmlRuntime) {
            this.executor = executor;
            this.pmmlRuntime = pmmlRuntime;
        }
    }
}
