/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.karaf.itest.util;

import java.lang.reflect.Field;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.junit.PaxExam;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;

/**
 * Special extension to PaxExam JUNit runner to allow use of static server to return fixed responses to easily test
 * kie server client operations instead of relying on fully configured workbench instance to be available for the tests.
 *
 * Since actual test runs within Karaf, bootstrapping server as part of beforeClass won't work as that is already in Karaf and thus will require
 * lots of dependencies and bundles.
 */
public class PaxExamWithWireRestClientMock extends PaxExam {

    private WireMockServer wireMockServer;
    private Class<?> clazz;
    private Integer port;
    private String host;

    public PaxExamWithWireRestClientMock(Class<?> klass) throws InitializationError {
        super(klass);
        this.clazz = klass;
        port = (Integer) getFieldValue("PORT");
        host = (String) getFieldValue("HOST");
    }

    @Override
    public void run(RunNotifier notifier) {

        this.wireMockServer = new WireMockServer(wireMockConfig().bindAddress(host).port(port));
        this.wireMockServer.start();

        setupMockServer();

        System.out.println("WireMock server started and bound to localhost:" + port);
        super.run(notifier);

        this.wireMockServer.stop();

    }

    protected Object getFieldValue(String name) {
        try {
            Field f = clazz.getField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void setupMockServer() {
        configureFor("localhost", port);
        stubFor(post(urlEqualTo("/jbpm-console/rest/execute"))
            .withHeader("Accept", equalTo("application/xml"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/xml")
                .withBody("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                          "<command-response>\n" +
                          "  <deployment-id>org.jbpm:Evaluation:1.0</deployment-id>\n" +
                          "  <ver>6.3.0.1</ver>\n" +
                          "  <process-instance index=\"0\">\n" +
                          "    <process-id>evaluation</process-id>\n" +
                          "    <id>40</id>\n" +
                          "    <state>1</state>\n" +
                          "    <parentProcessInstanceId>0</parentProcessInstanceId>\n" +
                          "    <command-name>StartProcessCommand</command-name>\n" +
                          "  </process-instance>\n" +
                          "</command-response>")));
    }

}
