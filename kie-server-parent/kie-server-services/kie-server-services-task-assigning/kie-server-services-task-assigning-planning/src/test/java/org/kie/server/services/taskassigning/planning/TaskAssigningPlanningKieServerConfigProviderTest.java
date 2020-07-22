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

package org.kie.server.services.taskassigning.planning;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kie.server.api.model.KieServerConfigProviderLoader;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskAssigningPlanningKieServerConfigProviderTest {

    @Test
    public void expectedValues() {
        assertProviderValues(new TaskAssigningPlanningKieServerConfigProvider());
    }

    @Test
    public void isLoaded() {
        List<TaskAssigningPlanningKieServerConfigProvider> providers = KieServerConfigProviderLoader.getConfigProviders().stream()
                .filter(provider -> provider instanceof TaskAssigningPlanningKieServerConfigProvider)
                .map(provider -> (TaskAssigningPlanningKieServerConfigProvider) provider)
                .collect(Collectors.toList());
        assertThat(providers.size()).isEqualTo(1);
        assertProviderValues(providers.get(0));
    }

    private void assertProviderValues(TaskAssigningPlanningKieServerConfigProvider configProvider) {
        assertThat(configProvider.getItems().size()).isEqualTo(1);
        assertThat(configProvider.getItems().get(0).getName()).isEqualTo(TaskAssigningPlanningKieServerConfigProvider.VALUE_NAME);
        assertThat(configProvider.getItems().get(0).getValue()).isEqualTo(TaskAssigningSolution.class.getPackage().getName());
        assertThat(configProvider.getItems().get(0).getType()).isEqualTo(String.class.getName());
    }
}
