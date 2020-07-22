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

import java.util.Arrays;
import java.util.List;

public class KieServerConfigProvider1 implements KieServerConfigProvider {

    public static final String VALUE1_NAME = "VALUE1_NAME";
    public static final String VALUE1 = "VALUE1";
    public static final String VALUE1_TYPE = "VALUE1_TYPE";

    public static final String VALUE2_NAME = "VALUE2_NAME";
    public static final String VALUE2 = "VALUE2";
    public static final String VALUE2_TYPE = "VALUE2_TYPE";

    @Override
    public List<KieServerConfigItem> getItems() {
        return Arrays.asList(new KieServerConfigItem(VALUE1_NAME, VALUE1, VALUE1_TYPE),
                             new KieServerConfigItem(VALUE2_NAME, VALUE2, VALUE2_TYPE),
                             null);
    }
}
