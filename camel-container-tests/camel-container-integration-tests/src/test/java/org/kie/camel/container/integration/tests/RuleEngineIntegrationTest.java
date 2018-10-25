/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.container.integration.tests;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.model.Person;

public class RuleEngineIntegrationTest extends AbstractKieCamelIntegrationTest {

    @Test
    public void testRuleEvaluation() {
        Person person = new Person();
        person.setName("George");
        person.setAge(15);
        Person response = kieCamelTestService.verifyAge(person);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.isCanDrink()).isFalse();

        person = new Person();
        person.setName("John");
        person.setAge(25);
        response = kieCamelTestService.verifyAge(person);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.isCanDrink()).isTrue();
    }
}
