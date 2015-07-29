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
        //props.load(TestConfig.class.getClassLoader().getResourceAsStream("performance.properties"));

        suite = System.getProperty("suite");
        props.put("suite", suite);

        scenario = System.getProperty("scenario");
        if (scenario.isEmpty() || scenario.equals("${scenario}")) {
            scenario = null;
        }
        props.put("scenario", scenario);

        startScriptLocation = System.getProperty("startScriptLocation");
        if (startScriptLocation == null) {
            startScriptLocation = "./run.sh";
        }
        props.put("startScriptLocation", startScriptLocation);

        runType = RunType.valueOf(System.getProperty("runType").toUpperCase());
        duration = Integer.valueOf(System.getProperty("duration"));
        iterations = Integer.valueOf(System.getProperty("iterations"));
        
        props.put("runType", runType);
        props.put("duration", duration);
        props.put("iterations", iterations);

        reporterType = ReporterType.valueOf(System.getProperty("reporterType").toUpperCase());
        periodicity = Integer.valueOf(System.getProperty("periodicity"));
        reportDataLocation = System.getProperty("reportDataLocation");
        
        props.put("reporterType", reporterType);
        props.put("periodicity", periodicity);
        props.put("reportDataLocation", reportDataLocation);

        threads = Integer.valueOf(System.getProperty("threads"));
        props.put("threads", threads);
        
        warmUp = Boolean.valueOf(System.getProperty("warmUp"));
        warmUpCount = Integer.valueOf(System.getProperty("warmUpCount"));
        
        props.put("warmUp", warmUp);
        props.put("warmUpCount", warmUpCount);
        
        measure = new ArrayList<TestConfig.Measure>();
        String mprop = System.getProperty("measure");
        String[] mlist = (mprop != null)?mprop.toUpperCase().split(","):new String[0];
        for (String m : mlist) {
            try {
                measure.add(Measure.valueOf(m));
            } catch (Exception ex) {
                
            }
        }
        props.put("measure", measure);

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
