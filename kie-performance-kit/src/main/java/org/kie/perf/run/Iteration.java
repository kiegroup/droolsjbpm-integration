package org.kie.perf.run;

import org.kie.perf.TestConfig;

public class Iteration implements IRunType {

    private int i;
    private int limit;

    @Override
    public void start(int limit) {
        i = 0;
        this.limit = limit;
    }

    @Override
    public boolean isEnd() {
        i++;
        return i > limit || i > TestConfig.getInstance().getIterations();
    }

}
