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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public abstract class AbstractLabelValueExtractorTest<T, E extends AbstractLabelValueExtractor<T>> {

    public static final String CUSTOM_NAME = "CUSTOM_NAME";
    public static final String VALUE1 = "VALUE1";
    public static final String VALUE2 = "VALUE2";

    protected E valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = createExtractor();
    }

    protected abstract E createExtractor();

    protected abstract Class<T> getExpectedType();

    protected abstract int getExpectedPriority();

    protected abstract String getExpectedLabelName();

    @Parameterized.Parameter()
    public T source;

    @Parameterized.Parameter(1)
    public Set<Object> expectedValue;

    @Parameterized.Parameters(name = "{index}: source={0}, expectedValue={1}")
    public static Collection<Object[]> data() {
        return new ArrayList<>();
    }

    @Test
    public void getType() {
        assertThat(valueExtractor.getType()).isEqualTo(getExpectedType());
    }

    @Test
    public void getLabelName() {
        assertThat(valueExtractor.getLabelName()).isEqualTo(getExpectedLabelName());
    }

    @Test
    public void getPriority() {
        assertThat(valueExtractor.getPriority()).isEqualTo(getExpectedPriority());
    }

    @Test
    public void extract() {
        Set<Object> result = valueExtractor.extract(source);
        assertThat(result).isEqualTo(expectedValue);
    }
}
