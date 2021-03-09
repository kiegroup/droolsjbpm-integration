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

package org.kie.server.api.model;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KieServerConfigProviderLoaderTest {

    @Test
    public void getConfigProviders() {
        List<KieServerConfigProvider> providers = KieServerConfigProviderLoader.getConfigProviders();
        assertThat(providers.size()).isEqualTo(2);
        assertThat(providers.stream()).filteredOn(provider -> provider instanceof KieServerConfigProvider1).hasSize(1);
        assertThat(providers.stream()).filteredOn(provider -> provider instanceof KieServerConfigProvider2).hasSize(1);
    }

    @Test
    public void getConfigItems() {
        List<KieServerConfigItem> items = KieServerConfigProviderLoader.getConfigItems();
        assertThat(items)
                .hasSize(3)
                .contains(new KieServerConfigItem(KieServerConfigProvider1.VALUE1_NAME,
                                                  KieServerConfigProvider1.VALUE1,
                                                  KieServerConfigProvider1.VALUE1_TYPE))
                .contains(new KieServerConfigItem(KieServerConfigProvider1.VALUE2_NAME,
                                                  KieServerConfigProvider1.VALUE2,
                                                  KieServerConfigProvider1.VALUE2_TYPE))
                .contains(new KieServerConfigItem(KieServerConfigProvider2.VALUE1_NAME,
                                                  KieServerConfigProvider2.VALUE1,
                                                  KieServerConfigProvider2.VALUE1_TYPE));
    }
}


