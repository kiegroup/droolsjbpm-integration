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

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.KieBase;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.command.RegistryContext;

public class DMNEvaluateAllLocalBatchCommand implements ExecutableCommand<DMNResult> {

    private final String modelNamespace;
    private final String modelName;
    private final Map<String, Object> inputCtx;
    private final String outIdentifier;

    public DMNEvaluateAllLocalBatchCommand(String modelNamespace,
                                           String modelName,
                                           Map<String, Object> inputCtx,
                                           String outIdentifier) {
        this.modelNamespace = modelNamespace;
        this.modelName = modelName;
        this.inputCtx = new HashMap<>(inputCtx);
        this.outIdentifier = outIdentifier;
    }

    @Override
    public DMNResult execute(Context context) {
        RegistryContext registryContext = (RegistryContext) context;
        KieBase kBase = registryContext.lookup(KieBase.class);
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(kBase).get(DMNRuntime.class);
        DMNModel model = dmnRuntime.getModel(modelNamespace, modelName);
        DMNContext dmnContext = dmnRuntime.newContext();
        for (Entry<String, Object> kv : inputCtx.entrySet()) {
            dmnContext.set(kv.getKey(), kv.getValue());
        }
        DMNResult dmnResult = dmnRuntime.evaluateAll(model, dmnContext);
        registryContext.register(DMNResult.class, dmnResult);

        if (this.outIdentifier != null) {
            ((RegistryContext) context).lookup(ExecutionResultImpl.class).setResult(this.outIdentifier, dmnResult);
        }

        return dmnResult;
    }
}
