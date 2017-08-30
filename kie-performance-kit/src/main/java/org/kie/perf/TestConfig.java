package org.kie.perf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kie.perf.run.Duration;
import org.kie.perf.run.IRunType;
import org.kie.perf.run.Iteration;
import org.kie.perf.suite.ConcurrentLoadSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static TestConfig tc;

    protected Properties properties;

    protected String projectName;
    protected String suite;
    protected String scenario;
    protected String startScriptLocation;

    protected String databaseName;

    protected RunType runType;
    protected int duration;
    protected int iterations;
    protected int expectedRate;

    protected ReporterType reporterType;
    protected int periodicity;
    protected String reportDataLocation;

    protected int threads;

    protected boolean warmUp;
    protected int warmUpCount;
    protected int warmUpTime;

    protected String perfRepoHost;
    protected String perfRepoUrlPath;
    protected String perfRepoUsername;
    protected String perfRepoPassword;
    
    protected String version = Executor.class.getPackage().getImplementationVersion();

    protected List<Measure> measure;
    protected List<String> tags = new ArrayList<String>();

    protected TestConfig() {

    }

    public Properties loadProperties() throws Exception {
        properties = new Properties();
        
        projectName = System.getProperty("projectName");
        if (projectName == null || projectName.isEmpty()) {
            projectName = "Project";
        }

        suite = System.getProperty("suite");
        properties.put("suite", suite);
        
        properties.put("suite.version", version);
        addTag(version);

        scenario = System.getProperty("scenario");
        if (scenario == null || scenario.isEmpty() || scenario.equals("${scenario}")) {
            scenario = null;
        } else {
            properties.put("scenario", scenario);
        }

        startScriptLocation = System.getProperty("startScriptLocation");
        if (startScriptLocation == null) {
            startScriptLocation = "./run.sh";
        }
        properties.put("startScriptLocation", startScriptLocation);

        runType = RunType.valueOf(System.getProperty("runType").toUpperCase());
        duration = Integer.valueOf(System.getProperty("duration"));
        iterations = Integer.valueOf(System.getProperty("iterations"));
        
        String expectedRateProp = System.getProperty("expectedRate");
        if (expectedRateProp == null) {
            expectedRate = 10;
        } else {
            expectedRate = Integer.valueOf(expectedRateProp);
        }

        properties.put("runType", runType);
        properties.put("duration", duration);
        properties.put("iterations", iterations);
        properties.put("expectedRate", expectedRate);

        reporterType = ReporterType.valueOf(System.getProperty("reporterType").toUpperCase());
        periodicity = Integer.valueOf(System.getProperty("periodicity"));
        reportDataLocation = System.getProperty("reportDataLocation");

        properties.put("reporterType", reporterType);
        properties.put("periodicity", periodicity);
        properties.put("reportDataLocation", reportDataLocation);

        threads = Integer.valueOf(System.getProperty("threads"));
        properties.put("threads", threads);
        if (suite.equals(ConcurrentLoadSuite.class.getSimpleName())) {
            addTag("thread-" + threads);
        }

        warmUp = Boolean.valueOf(System.getProperty("warmUp"));
        warmUpCount = Integer.valueOf(System.getProperty("warmUpCount"));
        warmUpTime = Integer.valueOf(System.getProperty("warmUpTime"));

        properties.put("warmUp", warmUp);
        properties.put("warmUpCount", warmUpCount);
        properties.put("warmUpTime", warmUpTime);

        measure = new ArrayList<TestConfig.Measure>();
        String mprop = System.getProperty("measure");
        String[] mlist = (mprop != null) ? mprop.toUpperCase().split(",") : new String[0];
        for (String m : mlist) {
            try {
                measure.add(Measure.valueOf(m));
            } catch (Exception ex) {

            }
        }
        properties.put("measure", measure);

        perfRepoHost = System.getProperty("perfRepo.host");
        if (perfRepoHost != null) {
            properties.put("perfRepo.host", perfRepoHost);
        }
        perfRepoUrlPath = System.getProperty("perfRepo.urlPath");
        if (perfRepoUrlPath != null) {
            properties.put("perfRepo.urlPath", perfRepoUrlPath);
        }
        perfRepoUsername = System.getProperty("perfRepo.username");
        if (perfRepoUsername != null) {
            properties.put("perfRepo.username", perfRepoUsername);
        }
        perfRepoPassword = System.getProperty("perfRepo.password");
        if (perfRepoPassword != null) {
            properties.put("perfRepo.password", perfRepoPassword);
        }

        return properties;
    }

    public static TestConfig getInstance() {
        if (tc == null) {
            tc = new TestConfig();
            try {
                tc.loadProperties();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return tc;
    }

    protected void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public Properties getProperties() {
        return properties;
    }
    
    public String getProjectName() {
        return projectName;
    }

    public String getSuite() {
        return suite;
    }
    
    public String getVersion() {
        return version;
    }

    public String getScenario() {
        return scenario;
    }

    public String getStartScriptLocation() {
        return startScriptLocation;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public RunType getRunType() {
        return runType;
    }

    public int getDuration() {
        return duration;
    }

    public int getIterations() {
        return iterations;
    }
    
    public int getExpectedRate() {
        return expectedRate;
    }

    public ReporterType getReporterType() {
        return reporterType;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public String getReportDataLocation() {
        return reportDataLocation;
    }

    public int getThreads() {
        return threads;
    }

    public boolean isWarmUp() {
        return warmUp;
    }

    public int getWarmUpCount() {
        return warmUpCount;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public List<Measure> getMeasure() {
        return measure;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getPerfRepoHost() {
        return perfRepoHost;
    }

    public String getPerfRepoUrlPath() {
        return perfRepoUrlPath;
    }

    public String getPerfRepoUsername() {
        return perfRepoUsername;
    }

    public String getPerfRepoPassword() {
        return perfRepoPassword;
    }

    public static enum ReporterType {
        CONSOLE, CSV, CSVSINGLE, PERFREPO
    }

    public static enum Measure {
        MEMORYUSAGE, FILEDESCRIPTORS, THREADSTATES, CPUUSAGE
    }

    public static enum RunType {
        DURATION(Duration.class), ITERATION(Iteration.class);

        private Class<? extends IRunType> klass;

        private RunType(Class<? extends IRunType> klass) {
            this.klass = klass;
        }

        public IRunType newInstance() {
            IRunType instance = null;
            try {
                instance = klass.newInstance();
            } catch (Exception e) {

            }
            return instance;
        }
    }

}
