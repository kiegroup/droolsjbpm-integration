package org.kie.perf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kie.perf.run.Duration;
import org.kie.perf.run.IRunType;
import org.kie.perf.run.Iteration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static TestConfig tc;

    protected String suite;
    protected String scenario;
    protected String startScriptLocation;

    protected RunType runType;
    protected int duration;
    protected int iterations;

    protected ReporterType reporterType;
    protected int periodicity;
    protected String reportDataLocation;

    protected int threads;
    
    protected boolean warmUp;
    protected int warmUpCount;
    
    protected List<Measure> measure;

    protected TestConfig() {

    }

    public Properties loadProperties() throws Exception {
        Properties props = new Properties();
        props.load(TestConfig.class.getClassLoader().getResourceAsStream("performance.properties"));

        suite = props.getProperty("suite");
        if (suite == null) {
            suite = System.getProperty("suite");
        }
        scenario = props.getProperty("scenario");
        if (scenario == null) {
            scenario = System.getProperty("scenario");
            if (scenario.isEmpty() || scenario.equals("${scenario}")) {
                scenario = null;
            }
        }

        startScriptLocation = props.getProperty("startScriptLocation");
        if (startScriptLocation == null) {
            startScriptLocation = "./run.sh";
        }

        runType = RunType.valueOf(props.getProperty("runType").toUpperCase());
        duration = Integer.valueOf(props.getProperty("duration"));
        iterations = Integer.valueOf(props.getProperty("iterations"));

        reporterType = ReporterType.valueOf(props.getProperty("reporterType").toUpperCase());
        periodicity = Integer.valueOf(props.getProperty("periodicity"));
        reportDataLocation = props.getProperty("reportDataLocation");

        threads = Integer.valueOf(props.getProperty("threads"));
        
        warmUp = Boolean.valueOf(props.getProperty("warmUp"));
        warmUpCount = Integer.valueOf(props.getProperty("warmUpCount"));
        
        measure = new ArrayList<TestConfig.Measure>();
        String mprop = props.getProperty("measure");
        String[] mlist = (mprop != null)?mprop.toUpperCase().split(","):new String[0];
        for (String m : mlist) {
            try {
                measure.add(Measure.valueOf(m));
            } catch (Exception ex) {
                
            }
        }

        return props;
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

    public String getSuite() {
        return suite;
    }

    public String getScenario() {
        return scenario;
    }

    public String getStartScriptLocation() {
        return startScriptLocation;
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
    
    public List<Measure> getMeasure() {
        return measure;
    }

    public static enum ReporterType {
        CONSOLE, CSV, CSVSINGLE
    }
    
    public static enum Measure {
        MEMORYUSAGE, FILEDESCRIPTORS, THREADSTATES
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
