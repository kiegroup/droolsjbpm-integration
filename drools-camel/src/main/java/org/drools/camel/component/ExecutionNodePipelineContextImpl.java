/**
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

package org.drools.camel.component;

import org.drools.grid.ExecutionNode;
import org.drools.runtime.CommandExecutor;

public class ExecutionNodePipelineContextImpl {
    private ExecutionNode node;
    CommandExecutor       exec;
    private ClassLoader   localClassLoadel;

    public ExecutionNodePipelineContextImpl(ExecutionNode node,
                                            ClassLoader localClassLoader) {
        this.node = node;
        this.localClassLoadel = localClassLoader;
    }

    public CommandExecutor getCommandExecutor() {
        return this.exec;
    }

    public void setCommandExecutor(CommandExecutor exec) {
        this.exec = exec;
    }

    public void setExecutionNode(ExecutionNode node) {
        this.node = node;
    }

    public ExecutionNode getExecutionNode() {
        return this.node;
    }

    public ClassLoader getLocalClassLoadel() {
        return localClassLoadel;
    }

    public void setLocalClassLoadel(ClassLoader localClassLoadel) {
        this.localClassLoadel = localClassLoadel;
    }

}
