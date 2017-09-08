package org.kie.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
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

    private static final Logger log = LoggerFactory.getLogger(Executor.class);
    
    private static Executor instance;

    private ScheduledReporter reporter;
    private ITestSuite testSuite;
    
    public static Executor getInstance() throws Exception {
        if (instance == null) {
            instance = new Executor();
            instance.initReporter();
            instance.initTestSuite();
        }
        return instance;
    }
    
    private void initReporter() {
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
            PerfRepoClient client = new PerfRepoClient(tc.getPerfRepoHost(), tc.getPerfRepoUrlPath(), tc.getPerfRepoUsername(),
                    tc.getPerfRepoPassword());
            reporter = PerfRepoReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(client);
        }
    }
    
    public ScheduledReporter getReporter() {
        return reporter;
    }

    public void initTestSuite() throws Exception {
        Class<?> csuite = Class.forName("org.kie.perf.suite." + TestConfig.getInstance().getSuite());
        testSuite = (ITestSuite) csuite.newInstance();
    }
    
    public ITestSuite getTestSuite() {
        return testSuite;
    }

    public void initMetrics(IPerfTest scenario) {
        MetricRegistry metrics = SharedMetricRegistry.getInstance();
        TestConfig tc = TestConfig.getInstance();
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
    
    public Set<Class<? extends IPerfTest>> getScenarios(String testPackage) throws Exception {
        TestConfig tc = TestConfig.getInstance();
        Class<? extends IPerfTest> selectedScenario = null;
        Set<Class<? extends IPerfTest>> scenarios = new HashSet<>();
        if (tc.getScenario() != null) {
            selectedScenario = (Class<? extends IPerfTest>) Class.forName(testPackage + "." + tc.getScenario());
            scenarios.add(selectedScenario);
            return scenarios;
        }

        Reflections reflections = new Reflections(testPackage);
        Set<Class<? extends IPerfTest>> allScenarios = reflections.getSubTypesOf(IPerfTest.class);
        for (Class<? extends IPerfTest> c : allScenarios) {
            if (Modifier.isAbstract(c.getModifiers())) {
                continue;
            }
            scenarios.add(c);
            
        }
        return scenarios;
    }
    
    public void forkScenario(String scenarioName) {
        ProcessBuilder processBuilder = new ProcessBuilder(TestConfig.getInstance().getStartScriptLocation(), scenarioName);
        try {
            Process process = processBuilder.start();
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public KPKConstraint checkScenarioConstraints(Class<? extends IPerfTest> scenario) {
        KPKConstraint constraint = scenario.getAnnotation(KPKConstraint.class);
        if (constraint == null) {
            return null;
        }

        for (String c : constraint.value()) {
            String[] entry = null;
            boolean noteq = c.contains("!=");
            if (noteq) {
                entry = c.split("!=");
            } else {
                entry = c.split("=");
            }
            if (entry.length == 2) {
                String val = System.getProperty(entry[0]);
                if (val == null || ( !noteq && !val.equals(entry[1])) || (noteq && val.equals(entry[1]))) {
                    return constraint;
                }
            } else {
                log.error("Scenario constraint entry '" + c + "' is in wrong format; should be 'propertyName=expectedValue'");
                return constraint;
            }
        }

        return null;
    }

    public void performWarmUp(IPerfTest scenario) {
        TestConfig tc = TestConfig.getInstance();
        SharedMetricRegistry.setWarmUp(true);
        scenario.initMetrics();
        long endWarmUpTime = System.currentTimeMillis() + tc.getWarmUpTime() * 1000; // warmUpTime is in seconds
        log.info("Starting JVM WarmUp for {} iterations or {} seconds, whatever comes first", tc.getWarmUpCount(), tc.getWarmUpTime());
        for (int i = 0; i < tc.getWarmUpCount() && endWarmUpTime > System.currentTimeMillis(); ++i) {
            scenario.execute();
        }
        log.info("JVM WarmUp has ended");
        SharedMetricRegistry.setWarmUp(false);
    }

    public static void main(String[] args) {
        try {
            Executor exec = getInstance();
            TestConfig tc = TestConfig.getInstance();

            String msg = "======== SUITE: " + tc.getSuite();
            String scenarioName = tc.getScenario();
            if (scenarioName != null) {
                msg += " / " + "SCENARIO: " + scenarioName;
            }
            msg += " ========";
            log.info(msg);

            exec.getTestSuite().start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
