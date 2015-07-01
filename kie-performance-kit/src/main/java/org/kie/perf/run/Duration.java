package org.kie.perf.run;

import org.kie.perf.TestConfig;

public class Duration implements IRunType {

    private long endTime;
    private int limit;
    
    private int i;
    
    @Override
    public void start(int limit) {
        this.limit = limit;
        endTime = System.currentTimeMillis() + TestConfig.getInstance().getDuration()*1000;
        i = 0;
    }

    @Override
    public boolean isEnd() {
        i++;
        return i > limit || System.currentTimeMillis() > endTime;
    }

}
