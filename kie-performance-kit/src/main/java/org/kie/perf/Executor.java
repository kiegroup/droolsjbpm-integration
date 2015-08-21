package org.kie.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.kie.perf.TestConfig.Measure;
import org.kie.perf.TestConfig.ReporterType;
import org.kie.perf.annotation.KPKConstraint;
import org.kie.perf.metrics.CPUUsageHistogramSet;
import org.kie.perf.metrics.CsvSingleReporter;
import org.kie.perf.metrics.MemoryUsageGaugeSet;
import org.kie.perf.metrics.PerfRepoReporter;
import org.kie.perf.metrics.ThreadStatesGaugeSet;
import org.kie.perf.scenario.IPerfTest;
import org.kie.perf.suite.ITestSuite;
import org.perfrepo.client.PerfRepoClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;

public class Executor {

    protected static final Logger log = LoggerFactory.getLogger(Executor.class);

    private ScheduledReporter reporter;

    public Executor() {

    }

    public ITestSuite findTestSuite() throws Exception {
        Class<?> csuite = Class.forName("org.kie.perf.suite." + TestConfig.getInstance().getSuite());
        return (ITestSuite) csuite.newInstance();
    }

    public void initMetrics(IPerfTest scenario) {
        // including reporter
        MetricRegistry metrics = SharedMetricRegistry.getInstance();

        TestConfig tc = TestConfig.getInstance();
        ReporterType reporterType = tc.getReporterType();
        if (reporterType == ReporterType.CONSOLE) {
            reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        } else if (reporterType == ReporterType.CSV) {
            File reportDataLocation = new File(tc.getReportDataLocation());
            if (!reportDataLocation.exists()) {
                reportDataLocation.mkdirs();
            }
            reporter = CsvReporter.forRegistry(metrics).formatFor(Locale.US).convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build(reportDataLocation);
            reporter.start(tc.getPeriodicity(), TimeUnit.SECONDS);
        } else if (reporterType == ReporterType.CSVSINGLE) {
            File reportDataLocation = new File(tc.getReportDataLocation());
            if (!reportDataLocation.exists()) {
                reportDataLocation.mkdirs();
            }
            reporter = CsvSingleReporter.forRegistry(metrics).formatFor(Locale.US).convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build(reportDataLocation);
        } else if (reporterType == ReporterType.PERFREPO) {
            PerfRepoClient client = new PerfRepoClient(tc.getPerfRepoHost(), tc.getPerfRepoUrlPath(), tc.getPerfRepoUsername(), tc.getPerfRepoPassword());
            reporter = PerfRepoReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(client);
        }

        for (Measure m : tc.getMeasure()) {
            if (m == Measure.MEMORYUSAGE) {
                metrics.registerAll(new MemoryUsageGaugeSet(scenario.getClass()));
            } else if (m == Measure.FILEDESCRIPTORS) {
                metrics.register(MetricRegistry.name(scenario.getClass(), "file.descriptors.usage"), new FileDescriptorRatioGauge());
                metrics.register(MetricRegistry.name(scenario.getClass(), "file.descriptors.used"), new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        try {
                            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
                            Method method = os.getClass().getDeclaredMethod("getOpenFileDescriptorCount");
                            method.setAccessible(true);
                            return (Long) method.invoke(os);
                        } catch (Exception e) {

                        }
                        return -1L;
                    }
                });
            } else if (m == Measure.THREADSTATES) {
                metrics.registerAll(new ThreadStatesGaugeSet(scenario.getClass()));
            } else if (m == Measure.CPUUSAGE) {
                metrics.registerAll(CPUUsageHistogramSet.getInstance(scenario.getClass()));
            }
        }
    }

    public IPerfTest selectNextTestFromSuite(ITestSuite testSuite) throws Exception {
        TestConfig tc = TestConfig.getInstance();
        String testPackage = testSuite.getTestPackage();
        Class<? extends IPerfTest> selectedScenario = null;
        if (tc.getScenario() != null) {
            selectedScenario = (Class<? extends IPerfTest>) Class.forName(testPackage + "." + tc.getScenario());
        }

        Reflections reflections = new Reflections(testPackage);
        Set<Class<? extends IPerfTest>> scenarios = reflections.getSubTypesOf(IPerfTest.class);
        IPerfTest instance = null;

        if (selectedScenario == null) {
            // parent process going through all scenarios to start new child
            // processes for each scenario
            for (Class<? extends IPerfTest> c : scenarios) {
                if (selectedScenario == null) {
                    ProcessBuilder processBuilder = new ProcessBuilder(tc.getStartScriptLocation(), c.getSimpleName());
                    try {
                        Process process = processBuilder.start();
                        InputStreamReader isr = new InputStreamReader(process.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                        process.waitFor();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            try {
                instance = selectedScenario.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public KPKConstraint checkScenarioConstraints(IPerfTest scenario) {
        KPKConstraint constraint = scenario.getClass().getAnnotation(KPKConstraint.class);
        if (constraint == null) {
            return null;
        }

        for (String c : constraint.value()) {
            String[] entry = c.split("=");
            if (entry.length == 2) {
                String val = System.getProperty(entry[0]);
                if (val == null || !val.equals(entry[1])) {
                    return constraint;
                }
            } else {
                log.error("Scenario constraint entry '" + c + "' is in wrong format; should be 'propertyName=expectedValue'");
                return constraint;
            }
        }

        return null;
    }

    public void report() {
        reporter.report();
    }

    public static void main(String[] args) {
        Executor exec = new Executor();
        try {
            TestConfig tc = TestConfig.getInstance();
            ITestSuite testSuite = exec.findTestSuite();
            IPerfTest scenario = exec.selectNextTestFromSuite(testSuite);

            String msg = "======== SUITE: " + tc.getSuite();
            String scenarioName = tc.getScenario();
            if (scenarioName != null) {
                msg += " / " + "SCENARIO: " + scenarioName;
            }
            msg += " ========";
            log.info(msg);

            if (scenario != null) {
                // this is a child process for this scenario with own JVM
                KPKConstraint constraint = exec.checkScenarioConstraints(scenario);
                if (constraint != null) {
                    log.info("Scenario '" + scenarioName + "' skipped due to constraints " + Arrays.toString(constraint.value()));
                } else {
                    exec.initMetrics(scenario);
                    testSuite.initScenario(scenario);
                    if (tc.isWarmUp()) {
                        SharedMetricRegistry.setWarmUp(true);
                        scenario.initMetrics();
                        for (int i = 0; i < tc.getWarmUpCount(); ++i) {
                            scenario.execute();
                        }
                        SharedMetricRegistry.setWarmUp(false);
                    }
                    scenario.initMetrics();

                    CPUUsageHistogramSet cpuusage = null;
                    boolean cpuusageEnabled = tc.getMeasure().contains(Measure.CPUUSAGE);
                    if (cpuusageEnabled) {
                        cpuusage = CPUUsageHistogramSet.getInstance(scenario.getClass());
                        cpuusage.start();
                    }
                    testSuite.startScenario(scenario);
                    if (cpuusageEnabled) {
                        cpuusage.stop();
                    }

                    exec.report();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

}
