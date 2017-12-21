/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.springboot;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JBPMApplication {

    @Autowired
    private RuntimeManagerComponent runtimeManagerComponent;

    @Bean
    CommandLineRunner startProcess() {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                runtimeManagerComponent.createRuntimeManager("simpleprocess.bpmn2");
                RuntimeEngine engine = runtimeManagerComponent.getRuntimeEngine(EmptyContext.get());

                KieSession ksession = engine.getKieSession();

                for (int i = 0; i < 20; i++) {
                    ksession.startProcess("simpleprocess");
                }
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(JBPMApplication.class, args);
    }
}
