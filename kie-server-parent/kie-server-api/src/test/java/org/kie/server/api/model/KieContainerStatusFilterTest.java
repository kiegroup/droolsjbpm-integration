/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class KieContainerStatusFilterTest {

    @Test
    public void parseFromNullableStringWithNull() {
        KieContainerStatusFilter filter = KieContainerStatusFilter.parseFromNullableString(null);
        Assertions.assertThat(filter).isEqualTo(KieContainerStatusFilter.ACCEPT_ALL);
    }

    @Test
    public void parseFromNullableStringWithEmptyString() {
        KieContainerStatusFilter filter = KieContainerStatusFilter.parseFromNullableString("");
        Assertions.assertThat(filter).isEqualTo(KieContainerStatusFilter.ACCEPT_ALL);
    }

    @Test
    public void parseFromNullableStringWithSingleStatus() {
        KieContainerStatusFilter filter = KieContainerStatusFilter.parseFromNullableString("started");
        Assertions.assertThat(filter).isEqualTo(new KieContainerStatusFilter(KieContainerStatus.STARTED));
    }

    @Test
    public void parseFromNullableStringWithSingleStatusUppercase() {
        KieContainerStatusFilter filter = KieContainerStatusFilter.parseFromNullableString("STARTED");
        Assertions.assertThat(filter).isEqualTo(new KieContainerStatusFilter(KieContainerStatus.STARTED));
    }

    @Test
    public void parseFromNullableStringWithMultipleStatuses() {
        KieContainerStatusFilter filter = KieContainerStatusFilter.parseFromNullableString("creating,started,failed");
        Assertions.assertThat(filter).isEqualTo(
                new KieContainerStatusFilter(KieContainerStatus.CREATING, KieContainerStatus.STARTED, KieContainerStatus.FAILED));
    }
}
