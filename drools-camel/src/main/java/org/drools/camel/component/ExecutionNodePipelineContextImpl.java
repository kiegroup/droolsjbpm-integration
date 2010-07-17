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
