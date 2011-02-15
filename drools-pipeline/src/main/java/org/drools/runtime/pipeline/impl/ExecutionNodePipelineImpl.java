/*
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import org.drools.grid.GridNode;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.ResultHandler;

public class ExecutionNodePipelineImpl extends BaseEmitter implements Pipeline {
    private GridNode node;

    public ExecutionNodePipelineImpl(GridNode node) {
        this.node = node;
    }

    public synchronized void insert(Object object, ResultHandler resultHandler) {
        emit(object, new ExecutionNodePipelineContextImpl(this.node, null, resultHandler));
    }

}
