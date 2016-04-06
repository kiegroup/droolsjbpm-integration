/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

public class KModuleBeanFactoryPostProcessorTest {

    @Test
    public void testEapContextDetectionEnumWithExpectedUrl() throws Exception {
        KModuleBeanFactoryPostProcessor postProcessor = new KModuleBeanFactoryPostProcessor();
        boolean result = postProcessor.isEapSpecificUrl(new URL("file://with-eap-specific-suffix/service-loader-resources/"));
        Assertions.assertThat(result).as("EAP-specific URL should have been found!").isTrue();
    }

    @Test
    public void testEapContextDetectionEnumWithoutExpectedUrl() throws Exception {
        KModuleBeanFactoryPostProcessor postProcessor = new KModuleBeanFactoryPostProcessor();
        boolean result = postProcessor.isEapSpecificUrl(new URL("file://my-test-url/no-eap-specific-context"));
        Assertions.assertThat(result).as("EAP-specific URL should _not_ have been found!").isFalse();
    }

    @Test
    public void testTryGetRootUrlForEapContext() throws Exception {
        // URL of WEB-INF/classes/ dir inside the WAR file
        final URL webInfClassesUrl = new URL("file://war-urls/WEB-INF/classes/");
        Enumeration<URL> urls = new Enumeration<URL>() {
            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index <= 1;
            }

            @Override
            public URL nextElement() {
                try {
                    if (index == 0) {
                        index++;
                        return webInfClassesUrl;
                    } else if (index == 1) {
                        // URL with EAP-specific suffix
                        index++;
                        return new URL("file://some-path-with-eap-specific-suffix/service-loader-resources/");
                    } else {
                        throw new IllegalStateException("");
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Buggy test!", e);
                }
            }
        };

        KModuleBeanFactoryPostProcessor postProcessor = new KModuleBeanFactoryPostProcessor();
        URL actualUrl = postProcessor.tryGetRootUrlForEapContext(urls);
        Assertions.assertThat(actualUrl).as("KModule root URL should have been found inside enumeration " + urls + "!").isEqualTo(webInfClassesUrl);
    }

}
