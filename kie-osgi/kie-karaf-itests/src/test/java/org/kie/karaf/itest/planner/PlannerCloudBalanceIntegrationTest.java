/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.karaf.itest.planner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.karaf.itest.AbstractKarafIntegrationTest;
import org.kie.karaf.itest.planner.domain.CloudBalance;
import org.kie.karaf.itest.planner.domain.CloudComputer;
import org.kie.karaf.itest.planner.domain.CloudProcess;
import org.kie.karaf.itest.planner.domain.score.CloudBalancingIncrementalScoreCalculator;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.SolverConfigContext;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import org.optaplanner.core.config.constructionheuristic.ConstructionHeuristicType;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PlannerCloudBalanceIntegrationTest extends AbstractKarafIntegrationTest {

    private static final String CLOUD_BALANCE_INCREMENTAL_CONFIG_FILE = "cloudBalanceIncrementalConfig.xml";
    private static final String CLOUD_BALANCE_DROOLS_CONFIG_FILE = "cloudBalanceDroolsConfig.xml";

    @Test(expected=IllegalStateException.class)
    public void invalidSolutionPlannerTest() {
        solveSolution(cloudBalanceGeneratorForFuse(0, 0, 0));
    }

    @Test
    public void plannerCloudBalanceTest() {
        solveSolution(cloudBalanceGeneratorForFuse(5, 10, 0));
    }

    @Test
    @Ignore("RHPAM-1554")
    public void plannerSolverFactoryTest() throws IOException {
        solveUsingSolverFactory(CLOUD_BALANCE_INCREMENTAL_CONFIG_FILE);
    }

    @Test
    @Ignore("RHPAM-1554")
    public void plannerSolverFactoryDroolsTest() throws IOException {
        solveUsingSolverFactory(CLOUD_BALANCE_DROOLS_CONFIG_FILE);
    }

    private CloudBalance cloudBalanceGeneratorForFuse(int computerNum, int processNum, int seed) {
        Random random = new Random(seed);

        int costRange = 10000;
        int costBaseline = 1000;

        int cpuPowerRange = 100;
        int cpuPowerBaseLine = 10;

        int memoryRange = 100;
        int memoryBaseLine = 10;

        int networkRange = 100;
        int networkBaseLine = 10;

        CloudBalance cloudBalance = new CloudBalance();
        String string = String.valueOf(Integer.MIN_VALUE) + "hard";
        string += "/";
        string += String.valueOf(Integer.MIN_VALUE) + "soft";
        cloudBalance.setScore(HardSoftScore.parseScore(string));

        List<CloudComputer> computerList = new ArrayList<CloudComputer>();
        for (int i = 0; i < computerNum; i++) {
            CloudComputer computer = new CloudComputer();
            computer.setCost(random.nextInt(costRange) + costBaseline);
            computer.setCpuPower(random.nextInt(cpuPowerRange) + cpuPowerBaseLine);
            computer.setMemory(random.nextInt(memoryRange) + memoryBaseLine);
            computer.setNetworkBandwidth(random.nextInt(networkRange) + networkBaseLine);
            computerList.add(computer);
        }

        int processCpuPowerRange = 50;
        int processCpuPowerBaseLine = 1;

        int processMemoryRange = 50;
        int processMemoryBaseLine = 1;

        int processNetworkRange = 50;
        int processNetworkBaseLine = 1;

        List<CloudProcess> processList = new ArrayList<CloudProcess>();
        for (int i = 0; i < processNum; i++) {
            CloudProcess process = new CloudProcess();
            process.setRequiredCpuPower(random.nextInt(processCpuPowerRange) + processCpuPowerBaseLine);
            process.setRequiredMemory(random.nextInt(processMemoryRange) + processMemoryBaseLine);
            process.setRequiredNetworkBandwidth(random.nextInt(processNetworkRange) + processNetworkBaseLine);
            processList.add(process);
        }
        cloudBalance.setComputerList(computerList);
        cloudBalance.setProcessList(processList);
        return cloudBalance;
    }

    private void solveSolution(CloudBalance cloudBalance) {
        SolverConfig config = new SolverConfig();
        config.setEntityClassList(Arrays.<Class<?>>asList(CloudProcess.class));
        config.setSolutionClass(CloudBalance.class);
        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig());
        config.getScoreDirectorFactoryConfig().setIncrementalScoreCalculatorClass(CloudBalancingIncrementalScoreCalculator.class);
        config.getScoreDirectorFactoryConfig().setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT);

        ConstructionHeuristicPhaseConfig constructionHeuristicPhaseConfig = new ConstructionHeuristicPhaseConfig();
        constructionHeuristicPhaseConfig.setConstructionHeuristicType(ConstructionHeuristicType.FIRST_FIT_DECREASING);

        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        localSearchPhaseConfig.setTerminationConfig(new TerminationConfig());
        localSearchPhaseConfig.getTerminationConfig().setStepCountLimit(20);

        List<PhaseConfig> phases = new ArrayList<PhaseConfig>();
        phases.add(constructionHeuristicPhaseConfig);
        phases.add(localSearchPhaseConfig);
        config.setPhaseConfigList(phases);

        Assert.assertEquals(cloudBalance.getScore().getHardScore(), Integer.MIN_VALUE);
        Assert.assertEquals(cloudBalance.getScore().getSoftScore(), Integer.MIN_VALUE);

        Solver solver = config.buildSolver(new SolverConfigContext());
        solver.solve(cloudBalance);

        CloudBalance solution = (CloudBalance) solver.getBestSolution();

        Assert.assertNotEquals(solution.getScore().getHardScore(), Integer.MIN_VALUE);
        Assert.assertNotEquals(solution.getScore().getSoftScore(), Integer.MIN_VALUE);
    }

    private void solveUsingSolverFactory(String configFile) {
        SolverFactory solverFactory = SolverFactory
                .createFromXmlResource(configFile, PlannerCloudBalanceIntegrationTest.class.getClassLoader());

        CloudBalance cloudBalance = cloudBalanceGeneratorForFuse(10, 20, 0);
        Assert.assertEquals(cloudBalance.getScore().getHardScore(), Integer.MIN_VALUE);
        Assert.assertEquals(cloudBalance.getScore().getSoftScore(), Integer.MIN_VALUE);

        Solver solver = solverFactory.getSolverConfig().buildSolver(new SolverConfigContext());
        solver.solve(cloudBalance);

        CloudBalance solution = (CloudBalance) solver.getBestSolution();

        Assert.assertNotEquals(solution.getScore().getHardScore(), Integer.MIN_VALUE);
        Assert.assertNotEquals(solution.getScore().getSoftScore(), Integer.MIN_VALUE);
    }

    @Configuration
    public static Option[] configure() {
        return new Option[]{
                // Install Karaf Container
                getKarafDistributionOption(),

                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.WARN),

                // Option to be used to do remote debugging
                // debugConfiguration("5005", true),

                loadKieFeatures("optaplanner-engine"),
        };
    }
}
