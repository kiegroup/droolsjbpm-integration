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

import java.util.Map;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * A DMN transformer processor for kie-local:// local/embedded execution
 * expecting as the Camel Exchange body a Map<String, Object>
 * which will be used for DMN context while evaluating the designated DMN model.
 */
public class ToDMNEvaluateAllCommandProcessor implements Processor {

    private final String modelNamespace;
    private final String modelName;
    private final String outIdentifier;

    public ToDMNEvaluateAllCommandProcessor(String modelNamespace,
                                            String modelName,
                                            String outIdentifier) {
        Objects.requireNonNull(modelNamespace);
        Objects.requireNonNull(modelName);
        Objects.requireNonNull(outIdentifier);
        this.modelNamespace = modelNamespace;
        this.modelName = modelName;
        this.outIdentifier = outIdentifier;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setBody(new DMNEvaluateAllLocalBatchCommand(modelNamespace,
                                                                     modelName,
                                                                     exchange.getIn().getBody(Map.class),
                                                                     outIdentifier));
    }

}
