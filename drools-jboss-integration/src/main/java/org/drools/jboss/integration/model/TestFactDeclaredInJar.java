/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.drools.jboss.integration.model;

/**
 * Dummy fact used for testing usage of facts declared as a java class and
 * compiled into KIE jar.
 */
public class TestFactDeclaredInJar {

    private final String value;

    public TestFactDeclaredInJar() {
        this.value = null;
    }
    
    public TestFactDeclaredInJar(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
