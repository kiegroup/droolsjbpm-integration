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

import java.io.File;

import org.junit.Test;
import org.kie.pmml.api.PMMLRuntimeFactory;
import org.kie.pmml.api.runtime.PMMLRuntime;
import org.kie.pmml.evaluator.assembler.factories.PMMLRuntimeFactoryImpl;

import static org.kie.test.util.filesystem.FileUtils.getFile;

public abstract class AbstractPMMLTest {

    private static final PMMLRuntimeFactory PMML_RUNTIME_FACTORY = new PMMLRuntimeFactoryImpl();
    protected AbstractPMMLExecutor abstractPMMLExecutor;
    protected PMMLRuntime pmmlRuntime;

    public AbstractPMMLTest(PMMLRuntime pmmlRuntime) {
        this.pmmlRuntime = pmmlRuntime;
    }

    protected static PMMLRuntime getPMMLRuntime(String modelName, String fileName) {
        File pmmlFile  = getFile(fileName);
        return PMML_RUNTIME_FACTORY.getPMMLRuntimeFromFile(pmmlFile);
    }

    @Test
    public void test() throws Exception {
        abstractPMMLExecutor.test(pmmlRuntime);
    }
}
