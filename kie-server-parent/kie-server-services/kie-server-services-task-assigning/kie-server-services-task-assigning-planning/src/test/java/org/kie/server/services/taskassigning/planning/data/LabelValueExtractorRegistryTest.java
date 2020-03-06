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

package org.kie.server.services.taskassigning.planning.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.data.LabelValueExtractor;
import org.kie.server.services.taskassigning.core.model.DefaultLabels;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LabelValueExtractorRegistryTest {

    private static final String SOME_VALUE = "SOME_VALUE";
    private static final String SKILLS_ATTRIBUTE = "skills";
    private static final String AFFINITIES_ATTRIBUTE = "affinities";

    @Test
    public void getInstance() {
        assertThat(LabelValueExtractorRegistry.getInstance()).isNotNull();
    }

    @Test
    public void defaultExtractorsAreLoaded() {
        LabelValueExtractorRegistry registry = LabelValueExtractorRegistry.getInstance();
        assertLabelExtractorsLoaded(TaskData.class, registry, DefaultLabels.AFFINITIES.name(), DefaultTaskDataAffinitiesValueExtractor.class);
        assertLabelExtractorsLoaded(TaskData.class, registry, DefaultLabels.SKILLS.name(), DefaultTaskDataSkillsValueExtractor.class);
        assertLabelExtractorsLoaded(TaskData.class, registry, TestTaskDataLabelValueExtractor1.TEST_LABEL, TestTaskDataLabelValueExtractor2.class);
        assertLabelExtractorsLoaded(User.class, registry, DefaultLabels.AFFINITIES.name(), DefaultUserAffinitiesValueExtractor.class);
        assertLabelExtractorsLoaded(User.class, registry, DefaultLabels.SKILLS.name(), DefaultUserSkillsValueExtractor.class);
    }

    @Test
    public void getLabelValueExtractorsForUser() {
        getLabelValueExtractors(User.class, 2);
    }

    @Test
    public void getLabelValueExtractorsForTaskData() {
        getLabelValueExtractors(TaskData.class, 3);
    }

    private <T> void getLabelValueExtractors(Class<T> sourceType, int expectedCount) {
        Optional<Set<LabelValueExtractor<T>>> valueExtractors = LabelValueExtractorRegistry.getInstance().getLabelValueExtractors(sourceType);
        assertThat(valueExtractors.isPresent()).isTrue();
        valueExtractors.ifPresent(v -> assertThat(v.size()).isEqualTo(expectedCount));
    }

    @Test
    public void applyUserLabelValueExtractors() {
        User user = mock(User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SKILLS_ATTRIBUTE, SOME_VALUE);
        attributes.put(AFFINITIES_ATTRIBUTE, null);
        when(user.getAttributes()).thenReturn(attributes);
        Map<String, Set<Object>> result = new HashMap<>();
        BiConsumer<String, Set<Object>> consumer = result::put;
        LabelValueExtractorRegistry.getInstance().applyLabelValueExtractors(User.class, user, consumer);
        assertThat(result.get(DefaultLabels.SKILLS.name())).isEqualTo(new HashSet<>(Collections.singleton(SOME_VALUE)));
        assertThat(result.get(DefaultLabels.AFFINITIES.name())).isEqualTo(Collections.EMPTY_SET);
    }

    @Test
    public void applyTaskDataLabelValueExtractors() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(SKILLS_ATTRIBUTE, null);
        inputData.put(AFFINITIES_ATTRIBUTE, SOME_VALUE);
        inputData.put(TestTaskDataLabelValueExtractor1.TEST_LABEL, SOME_VALUE);
        TaskData taskData = TaskData.builder().inputData(inputData).build();
        Map<String, Set<Object>> result = new HashMap<>();
        BiConsumer<String, Set<Object>> consumer = result::put;
        LabelValueExtractorRegistry.getInstance().applyLabelValueExtractors(TaskData.class, taskData, consumer);
        assertThat(result.get(DefaultLabels.SKILLS.name())).isEqualTo(Collections.EMPTY_SET);
        assertThat(result.get(DefaultLabels.AFFINITIES.name())).isEqualTo(new HashSet<>(Collections.singleton(SOME_VALUE)));
        assertThat(result.get(TestTaskDataLabelValueExtractor1.TEST_LABEL)).isEqualTo(new HashSet<>(Collections.singleton(TestTaskDataLabelValueExtractor2.EXAMPLE_LABEL_VALUE2)));
    }

    private <T> void assertLabelExtractorsLoaded(Class<T> sourceType,
                                                 LabelValueExtractorRegistry registry,
                                                 String labelName,
                                                 Class<?> extractorType) {
        Optional<Set<LabelValueExtractor<T>>> extractors = registry.getLabelValueExtractors(sourceType);
        assertThat(extractors.isPresent()).isTrue();
        if (extractors.isPresent()) {
            assertThat(extractors.get().size())
                    .isGreaterThan(0);
            assertThat(extractors.get().stream().anyMatch(extractor -> labelName.equals(extractor.getLabelName()) &&
                    extractorType.equals(extractor.getClass())))
                    .isTrue();
        }
    }
}
