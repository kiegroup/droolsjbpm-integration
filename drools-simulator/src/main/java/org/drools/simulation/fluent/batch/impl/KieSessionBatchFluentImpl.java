package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.OutCommand;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.fluent.Scope;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

import java.util.Map;

public class KieSessionBatchFluentImpl extends BaseBatchFluent<KieSessionBatchFluent> implements KieSessionBatchFluent {


    public KieSessionBatchFluentImpl(FluentContext fluentCtx) {
        super(fluentCtx);
    }

    @Override
    public BatchBuilderFluent end() {
        return null;
    }

    public BatchBuilderFluent end(String name) {
        return null;
    }

    public BatchBuilderFluent end(String context, String name) {
        return null;
    }

    @Override
    public KieSessionBatchFluent startProcess(String processId) {
        return this;
    }

    @Override
    public KieSessionBatchFluent startProcess(String processId, Map<String, Object> parameters) {
        return this;
    }

    @Override
    public KieSessionBatchFluent createProcessInstance(String processId, Map<String, Object> parameters) {
        return this;
    }

    @Override
    public KieSessionBatchFluent startProcessInstance(long processInstanceId) {
        return this;
    }

    @Override
    public KieSessionBatchFluent signalEvent(String type, Object event) {
        return this;
    }

    @Override
    public KieSessionBatchFluent signalEvent(String type, Object event, long processInstanceId) {
        return this;
    }

    @Override
    public KieSessionBatchFluent abortProcessInstance(long processInstanceId) {
        return this;
    }

    @Override
    public WorkItemManagerFluent<WorkItemManagerFluent, KieSessionBatchFluent, BatchBuilderFluent> getWorkItemManager() {
        return null;
    }

    @Override
    public KieSessionBatchFluent fireAllRules() {
        fluentCtx.addCommand( new FireAllRulesCommand());
        return this;
    }

    @Override
    public KieSessionBatchFluent setGlobal(String identifier, Object object) {
        return this;
    }

    @Override
    public KieSessionBatchFluent getGlobal(String identifier) {
        return this;
    }

    @Override
    public KieSessionBatchFluent insert(Object object) {
        fluentCtx.addCommand( new InsertObjectCommand(object));
        return this;
    }

    @Override
    public KieSessionBatchFluent update(FactHandle handle, Object object) {
        return this;
    }

    @Override
    public KieSessionBatchFluent delete(FactHandle handle) {
        return this;
    }

    @Override
    public BatchBuilderFluent dispose() {
        fluentCtx.addCommand( new DisposeCommand());
        return fluentCtx.getBatchBuilderFluent();
    }

}
