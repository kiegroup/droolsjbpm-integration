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
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.camel.container.api.model.Person;

public class RuleEngineIntegrationTest extends AbstractKieCamelIntegrationTest {

    private static final String GLOBAL_NAME = "testGlobal";
    private static final String GLOBAL_VALUE = "global-value";

    @Test
    public void testRuleEvaluationService() {
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

    @Test
    public void testSetGetGlobal() {
        final SetGlobalCommand setGlobalCommand = new SetGlobalCommand(GLOBAL_NAME, GLOBAL_VALUE);
        runCommand(setGlobalCommand);

        final GetGlobalCommand getGlobalCommand = new GetGlobalCommand(GLOBAL_NAME);
        getGlobalCommand.setOutIdentifier(GLOBAL_NAME);
        final ExecutionResults executionResults = runCommand(getGlobalCommand);
        Assertions.assertThat(executionResults).isNotNull();
        Assertions.assertThat(executionResults.getValue(GLOBAL_NAME)).isEqualTo(GLOBAL_VALUE);
    }

    @Test
    public void testInsertGetDeleteFact() {
        final Person person = new Person("John", 15);
        final FactHandle factHandle = insertObject(person);

        final Person returnedPerson = (Person) getObject(factHandle);
        Assertions.assertThat(returnedPerson).isEqualToComparingFieldByField(person);

        final DeleteCommand deleteCommand = new DeleteCommand(factHandle);
        runCommand(deleteCommand);

        Assertions.assertThat(getObject(factHandle)).isNull();
    }

    @Test
    public void testFireAllRules() {
        final Person oldPerson = new Person("Tom", 30);
        final Person youngPerson = new Person("John", 15);

        final FactHandle oldPersonFactHandle = insertObject(oldPerson);
        final FactHandle youngPersonFactHandle = insertObject(youngPerson);

        final FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
        fireAllRulesCommand.setOutIdentifier(DEFAULT_OUT_ID);
        ExecutionResults executionResults = runCommand(fireAllRulesCommand);
        Assertions.assertThat(executionResults).isNotNull();
        Assertions.assertThat(executionResults.getValue(DEFAULT_OUT_ID)).isEqualTo(2);

        final Person oldPerson2 = (Person) getObject(oldPersonFactHandle);
        Assertions.assertThat(oldPerson2.isCanDrink()).isTrue();
        final Person youngPerson2 = (Person) getObject(youngPersonFactHandle);
        Assertions.assertThat(youngPerson2.isCanDrink()).isFalse();
    }

    private FactHandle insertObject(Object object) {
        final InsertObjectCommand insertObjectCommand = new InsertObjectCommand(object);
        insertObjectCommand.setOutIdentifier(DEFAULT_OUT_ID);
        final ExecutionResults executionResults = runCommand(insertObjectCommand);
        Assertions.assertThat(executionResults).isNotNull();
        Assertions.assertThat(executionResults.getFactHandle(DEFAULT_OUT_ID)).isNotNull();
        final FactHandle factHandle = (FactHandle) executionResults.getFactHandle(DEFAULT_OUT_ID);

        return factHandle;
    }

    private Object getObject(FactHandle factHandle) {
        final GetObjectCommand getObjectCommand = new GetObjectCommand(factHandle);
        getObjectCommand.setOutIdentifier(DEFAULT_OUT_ID);
        ExecutionResults executionResults = runCommand(getObjectCommand);
        Assertions.assertThat(executionResults).isNotNull();
        final Object object = executionResults.getValue(DEFAULT_OUT_ID);

        return object;
    }
}
