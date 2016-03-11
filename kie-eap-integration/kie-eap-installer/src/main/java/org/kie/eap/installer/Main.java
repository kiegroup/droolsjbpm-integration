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

package org.kie.eap.installer;

import java.util.Arrays;
import java.util.LinkedList;

import org.wildfly.extras.patch.installer.AbstractInstaller;

/**
 * Main class that installs kie-eap-integration
 */
public final class Main {

    public static void main(String[] originalArgs) throws Exception {
        AbstractInstaller installer = new AbstractInstaller() {
            @Override
            public String getJarName() {
                return "kie-eap-installer.jar";
            }
        };
        installer.main(new LinkedList<String>(Arrays.asList(originalArgs)));
    }
}
