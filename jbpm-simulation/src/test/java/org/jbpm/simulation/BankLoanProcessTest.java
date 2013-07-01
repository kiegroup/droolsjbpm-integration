package org.jbpm.simulation;

import org.jbpm.simulation.impl.WorkingMemorySimulationRepository;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BankLoanProcessTest {
    @Test
    public void testBankLoanProcess() throws Exception {
        InputStreamReader in = new InputStreamReader(this.getClass().getResourceAsStream("/BPMN2-BankLoanProcess.bpmn2"));

        String out = new String();
        BufferedReader br = new BufferedReader(in);
        for(String line = br.readLine(); line != null; line = br.readLine())
            out += line;

        SimulationRepository repo = SimulationRunner.runSimulation("defaultPackage.banl-loan", out, 10, 100, "onevent.simulation.rules.drl");
        assertNotNull(repo);

        WorkingMemorySimulationRepository wmRepo = (WorkingMemorySimulationRepository) repo;
        wmRepo.fireAllRules();

        assertEquals(10, wmRepo.getAggregatedEvents().size());
        assertEquals(110, wmRepo.getEvents().size());

        wmRepo.close();

    }
}
