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

package net.pmml.tester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.kie.pmml.api.PMMLRuntimeFactory;
import org.kie.pmml.api.runtime.PMMLRuntime;
import org.kie.pmml.evaluator.assembler.factories.PMMLRuntimeFactoryImpl;

public abstract class AbstractPMMLTest {

    protected static final PMMLRuntimeFactory PMML_RUNTIME_FACTORY = new PMMLRuntimeFactoryImpl();

    protected static final List<AbstractPMMLRuntimeProvider> ABSTRACT_PMML_RUNTIME_PROVIDERS = Arrays.asList(new NotKJarPMMLRuntimeProvider(),
                                                                                                             new MavenPMMLRuntimeProvider(),
                                                                                                             new KieContainerPMMLRuntimeProvider());

    protected AbstractPMMLExecutor abstractPMMLExecutor;
    protected PMMLRuntime pmmlRuntime;

    public AbstractPMMLTest(PMMLRuntime pmmlRuntime) {
        this.pmmlRuntime = pmmlRuntime;
    }

    protected static Collection<Object[]> getDATA(final Collection<Object[]> INPUT_DATA, final String fileName, final String fullFileName) {
        Collection<Object[]> toReturn = new ArrayList<>();
        for (AbstractPMMLRuntimeProvider abstractPMMLRuntimeProvider : ABSTRACT_PMML_RUNTIME_PROVIDERS) {
            for (Object[] object : INPUT_DATA) {
                Object[] toAdd =  new Object[object.length +2];
                System.arraycopy(object, 0, toAdd, 0, object.length);
                toAdd[toAdd.length-2] = abstractPMMLRuntimeProvider;
                if (abstractPMMLRuntimeProvider instanceof KieContainerPMMLRuntimeProvider) {
                    toAdd[toAdd.length-1] = fullFileName;
                } else {
                    toAdd[toAdd.length-1] = fileName;
                }
                toReturn.add(toAdd);
            }
        }
        return toReturn;
    }

    protected static PMMLRuntime getPMMLRuntime(String modelName, String pmmlFile) {
        return PMML_RUNTIME_FACTORY.getPMMLRuntimeFromClasspath(pmmlFile);
    }

    @Test
    public void test() throws Exception {
        abstractPMMLExecutor.test(pmmlRuntime);
    }
}
