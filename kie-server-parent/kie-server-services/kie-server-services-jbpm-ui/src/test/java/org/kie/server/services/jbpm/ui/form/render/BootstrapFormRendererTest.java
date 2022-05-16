/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.ui.form.render;

import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.junit.Test;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;

import static org.assertj.core.api.Assertions.assertThat;

public class BootstrapFormRendererTest {

    @Test
    public void testProcessFormRendererWithDate() {
        FormReader reader = new FormReader();

        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/date-form.json"));
        ProcessAssetDesc processAssetDesc = new ProcessAssetDesc();
        processAssetDesc.setId("test-id");

        BootstrapFormRenderer bootstrapFormRenderer = new BootstrapFormRenderer();
        String outString = bootstrapFormRenderer.renderProcess("test-containerId", processAssetDesc, form);
        assertThat(outString).contains("'dateBirth' : String(getLocalDateWithoutTime('field_1703386699666296E12') )");
        assertThat(outString).contains("<input id=\"field_1703386699666296E12\" name=\"dateBirth\" type=\"date\" class=\"form-control\" value=");
    }

    @Test
    public void testProcessFormRendererWithDateTime() {
        FormReader reader = new FormReader();

        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/dateTime-local-form.json"));
        ProcessAssetDesc processAssetDesc = new ProcessAssetDesc();
        processAssetDesc.setId("test-id");

        BootstrapFormRenderer bootstrapFormRenderer = new BootstrapFormRenderer();
        String outString = bootstrapFormRenderer.renderProcess("test-containerId", processAssetDesc, form);

        assertThat(outString).contains("'dateBirth' : String(document.getElementById('field_1703386699666296E12').value)");
        assertThat(outString).contains("<input id=\"field_1703386699666296E12\" name=\"dateBirth\" type=\"datetime-local\" class=\"form-control\" value=");
    }
}
