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

package org.kie.karaf.itest.camel.kiecamel.tools;

import org.kie.karaf.itest.camel.kiecamel.model.Person;

public class PersonFactory {

    private PersonFactory() {
    }

    public static Person createOldPerson() {
        Person person = new Person();
        person.setName("Old Person");
        person.setAge(21);
        return person;
    }

    public static Person createYoungPerson() {
        Person person = new Person();
        person.setName("Young Person");
        person.setAge(18);
        return person;
    }

}
