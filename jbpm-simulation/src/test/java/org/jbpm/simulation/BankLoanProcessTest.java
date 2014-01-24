package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.junit.Test;

public class BankLoanProcessTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testBankLoanProcess() throws Exception {
        // enable logging
        System.setProperty("jbpm.simulation.log.enabled", "true");
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-BankLoanProcess.bpmn2"));

        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine())
            out += line;

        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.banl-loan", out, 10, 100, true, "onevent.simulation.rules.drl");
        assertNotNull(repo);

        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.getSession().execute(new InsertElementsCommand((Collection)wmRepo.getAggregatedEvents()));
        wmRepo.fireAllRules();

        List<AggregatedSimulationEvent> summary = (List<AggregatedSimulationEvent>) wmRepo.getGlobal("summary");
        assertNotNull(summary);
        assertEquals(11, summary.size());
        assertEquals(110, wmRepo.getEvents().size());

        wmRepo.close();

    }
}
