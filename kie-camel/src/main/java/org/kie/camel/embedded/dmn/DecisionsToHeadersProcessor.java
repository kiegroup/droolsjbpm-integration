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
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.dmn.api.core.DMNResult;

/**
 * A DMN transformer processor for kie-local:// local/embedded result processing
 * of DMN Decision(s) as Camel Exchange header(s)
 */
public class DecisionsToHeadersProcessor implements Processor {

    private final String outIdentifier;
    private final Map<String, String> toHeadersMap;

    public DecisionsToHeadersProcessor(String outIdentifier, Map<String, String> toHeadersMap) {
        Objects.requireNonNull(outIdentifier);
        Objects.requireNonNull(toHeadersMap);
        this.outIdentifier = outIdentifier;
        this.toHeadersMap = new HashMap<>(toHeadersMap);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        ExecutionResults body = exchange.getIn().getBody(ExecutionResults.class);
        DMNResult value = (DMNResult) body.getValue(outIdentifier);
        for (Entry<String, String> kv : toHeadersMap.entrySet()) {
            exchange.getIn().setHeader(kv.getKey(), value.getDecisionResultByName(kv.getValue()).getResult());
        }
    }

    public static Builder builder(String outIdentifier,
                                  String headerKey,
                                  String decisionName) {
        return new Builder(outIdentifier, headerKey, decisionName);
    }

    public static class Builder {

        private final String outIdentifier;
        private final Map<String, String> toHeadersMap = new HashMap<>();

        public Builder(String outIdentifier,
                       String headerKey,
                       String decisionName) {
            Objects.requireNonNull(outIdentifier);
            Objects.requireNonNull(headerKey);
            Objects.requireNonNull(decisionName);
            this.outIdentifier = outIdentifier;
            toHeadersMap.put(headerKey, decisionName);
        }

        public Builder with(String headerKey, String decisionName) {
            Objects.requireNonNull(headerKey);
            Objects.requireNonNull(decisionName);
            toHeadersMap.put(headerKey, decisionName);
            return this;
        }

        public DecisionsToHeadersProcessor build() {
            return new DecisionsToHeadersProcessor(this.outIdentifier, this.toHeadersMap);
        }
    }

}
