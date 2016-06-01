package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.batch.KieSessionBatchFluent;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.fluent.Scope;
import org.kie.internal.fluent.runtime.WorkItemManagerFluent;

import java.util.Map;

public class KieSessionBatchFluentImpl extends BaseBatchFluent<KieSessionBatchFluentImpl> implements KieSessionBatchFluent {


    public KieSessionBatchFluentImpl(FluentContext fluentCtx) {
        super(fluentCtx);
    }

    @Override
    public KieSessionBatchFluent set(String name) {
        return this;
    }

    @Override
    public KieSessionBatchFluent out() {
        return this;
    }

    @Override
    public KieSessionBatchFluent out(String name) {
        return this;
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
    public WorkItemManagerFluent<WorkItemManagerFluent, KieSessionBatchFluent> getWorkItemManager() {
        return null;
    }

    @Override
    public KieSessionBatchFluent fireAllRules() {
        return this;
    }

    @Override
    public KieSessionBatchFluent setGlobal(String identifier, Object object) {
        return this;
    }

    @Override
    public KieSessionBatchFluent set(String name, Scope scope) {
        //addCommand(new SetVariableCommandFromLastReturn(null, name, scope));
        return this;
    }

    @Override
    public KieSessionBatchFluent getGlobal(String identifier) {
        return this;
    }

    @Override
    public KieSessionBatchFluent insert(Object object) {
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

}
