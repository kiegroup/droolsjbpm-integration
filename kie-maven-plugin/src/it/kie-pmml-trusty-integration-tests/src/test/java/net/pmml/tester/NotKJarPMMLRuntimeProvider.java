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

import org.kie.pmml.api.runtime.PMMLRuntime;

import static org.apache.commons.io.FileUtils.getFile;

public class NotKJarPMMLRuntimeProvider extends AbstractPMMLRuntimeProvider {

    @Override
    public PMMLRuntime getPMMLRuntime(String modelName, String fileName) {
        File pmmlFile = getFile(fileName);
        return PMML_RUNTIME_FACTORY.getPMMLRuntimeFromFile(pmmlFile);
    }
}
