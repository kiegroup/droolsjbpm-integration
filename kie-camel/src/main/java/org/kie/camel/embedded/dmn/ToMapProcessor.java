/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.embedded.dmn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * A generic Apache Camel transformer processor,
 * transforming the Exchange body into a Map with a single entry for the specified key and entry value the current Exchange body.
 */
public class ToMapProcessor implements Processor {

    private final String entryKey;

    public ToMapProcessor(String entryKey) {
        Objects.requireNonNull(entryKey);
        this.entryKey = entryKey;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> newBody = new HashMap<>();
        newBody.put(entryKey, exchange.getIn().getBody());
        exchange.getIn().setBody(newBody);
    }

}
