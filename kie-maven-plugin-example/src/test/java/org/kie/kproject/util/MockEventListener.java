package org.kie.kproject.util;

import java.util.List;

import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;

public class MockEventListener implements DMNRuntimeEventListener {

    private List<Integer> selected;
    private List<Integer> matches;

    @Override
    public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {

    }

    @Override
    public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {

    }

    @Override
    public void beforeEvaluateBKM(BeforeEvaluateBKMEvent event) {

    }

    @Override
    public void afterEvaluateBKM(AfterEvaluateBKMEvent event) {

    }

    @Override
    public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {

    }

    @Override
    public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {

    }

    @Override
    public void beforeEvaluateDecisionTable(BeforeEvaluateDecisionTableEvent event) {

    }

    @Override
    public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
        matches = event.getMatches();
        selected = event.getSelected();
    }

    @Override
    public void beforeEvaluateDecisionService(BeforeEvaluateDecisionServiceEvent event) {

    }

    @Override
    public void afterEvaluateDecisionService(AfterEvaluateDecisionServiceEvent event) {

    }

    public List<Integer> getSelected() {
        return selected;
    }

    public List<Integer> getMatches() {
        return matches;
    }
}