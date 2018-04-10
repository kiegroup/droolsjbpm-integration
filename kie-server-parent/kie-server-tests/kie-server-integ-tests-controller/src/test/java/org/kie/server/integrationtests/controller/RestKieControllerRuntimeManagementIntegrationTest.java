/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.integrationtests.controller;

import org.assertj.core.api.SoftAssertions;
import org.junit.Ignore;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;

@Ignore("Runtime management REST API is not aviable yet")
public class RestKieControllerRuntimeManagementIntegrationTest extends KieControllerRuntimeManagementIntegrationTest<KieServerControllerHTTPClientException> {

    @Override
    protected void assertNotFoundException(KieServerControllerHTTPClientException e) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(e.getResponseCode()).isEqualTo(404);
            softly.assertThat(e.getMessage()).isNotNull();
        });
    }

    @Override
    protected void assertBadRequestException(KieServerControllerHTTPClientException e) {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(e.getResponseCode()).isEqualTo(400);
            softly.assertThat(e.getMessage()).isNotNull();
        });
    }
}
