/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.eap.config;

import static org.wildfly.extras.config.LayerConfig.Type.INSTALLING;
import static org.wildfly.extras.config.LayerConfig.Type.REQUIRED;
import java.util.Arrays;
import java.util.List;

import org.wildfly.extras.config.ConfigContext;
import org.wildfly.extras.config.ConfigPlugin;
import org.wildfly.extras.config.LayerConfig;

public class KIEConfigPlugin implements ConfigPlugin {

    @Override
    public String getConfigName() {
        return "bpms";
    }

    @Override
    public List<LayerConfig> getLayerConfigs() {
        return Arrays.asList(new LayerConfig("fuse", REQUIRED, -10), new LayerConfig("bpms", INSTALLING, -9));
    }

    @Override
    public void applyDomainConfigChange(ConfigContext context, boolean enable) {
        // intentionally left empty, no config changes needed
    }

    @Override
    public void applyStandaloneConfigChange(ConfigContext context, boolean enable) {
        // intentionally left empty, no config changes needed
    }

}
