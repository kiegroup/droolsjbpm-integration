package org.drools.grid.services.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionEnvironmentInfo extends GridResourceInfo {

    private Map<String, Map<String, String>> executionNodesInfo = new HashMap<String, Map<String, String>>();

    public ExecutionEnvironmentInfo() {
    }

    public ExecutionEnvironmentInfo(Status status) {
        super( status );
    }

    public void addExecutionNodeInfo(String id,
                                     Map<String, String> directoryMap) {
        this.executionNodesInfo.put( id,
                                     Collections.unmodifiableMap( directoryMap ) );
    }

    public void setExecutionNodesInfo(Map<String, Map<String, String>> executionNodesInfo) {
        this.executionNodesInfo = executionNodesInfo;
    }

    public Map<String, Map<String, String>> getExecutionNodesInfo() {
        return Collections.unmodifiableMap( this.executionNodesInfo );
    }

}
