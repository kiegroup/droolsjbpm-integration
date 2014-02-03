package org.jbpm.simulation.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.math3.stat.StatUtils;
import org.jbpm.simulation.NodeStatistic;
import org.jbpm.simulation.SimulationDataProvider;
import org.kie.api.definition.process.Node;

public class JBPMBAMSimulationDataProvider implements SimulationDataProvider {

    private DataSource bamDataSource;
    private boolean processLoaded = false;
    private Map<String, Map<String, Object>> processStatistics = new HashMap<String, Map<String, Object>>();
    
    private String processId;
    
    private static final String GET_PROCESS_INFO_QUERY = 
            "select nodeid, count(processinstanceid), min(log_date), max(log_date) from nodeinstancelog where processid = ? and type = ? group by nodeid;";
    
    private static final String PROCESS_INSTANCE_COUNT_QUERY = 
            "select count(processinstanceid) from processinstancelog where processid = ?;";
    
    private static final String PROCESS_INSTANCE_COUNT_FOR_PATH_QUERY = 
            "select count(processinstanceid) from nodeinstancelog where processid = ? and nodeid in (@1) group by processinstanceid;";
    
    public JBPMBAMSimulationDataProvider(String bamDataSource, String processId) {
        try {
            InitialContext ctx = new InitialContext();
            this.bamDataSource = (DataSource) ctx.lookup(bamDataSource);
        
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get data source: " + bamDataSource, e);
        }
        this.processId = processId;
    }

    public JBPMBAMSimulationDataProvider(DataSource bamDataSource, String processId) {
        this.bamDataSource = bamDataSource;
        this.processId = processId;
    }


    public Map<String, Object> getSimulationDataForNode(Node node) {
        
        if (!processLoaded) {
            loadProcessInfo(processId);
        }
        
        
        return processStatistics.get(Long.toString(node.getId()));
    } 
    

    protected void loadProcessInfo(String processId) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        Map<String, NodeStatistic> enterStats = new HashMap<String, NodeStatistic>();
        Map<String, NodeStatistic> exitStats = new HashMap<String, NodeStatistic>();
        
        try {
            connection = bamDataSource.getConnection();
            
            // collect information about node enter events
            pstmt = connection.prepareStatement(GET_PROCESS_INFO_QUERY);
            pstmt.setString(1, processId);
            pstmt.setInt(2, 0);
            
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String nodeIdbam = rs.getString(1);
                Long numberOfProcessInstances = rs.getLong(2);
                Timestamp minTs = rs.getTimestamp(3);
                Timestamp maxTs = rs.getTimestamp(4);
                

                enterStats.put(nodeIdbam, new NodeStatistic(nodeIdbam, minTs.getTime(), maxTs.getTime(), numberOfProcessInstances));
            }
            
            
            // collect information about node exit events
            pstmt = connection.prepareStatement(GET_PROCESS_INFO_QUERY);
            pstmt.setString(1, processId);
            pstmt.setInt(2, 1);
            
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String nodeIdbam = rs.getString(1);
                Long numberOfProcessInstances = rs.getLong(2);
                Timestamp minTs = rs.getTimestamp(3);
                Timestamp maxTs = rs.getTimestamp(4);
                
                exitStats.put(nodeIdbam, new NodeStatistic(nodeIdbam, minTs.getTime(), maxTs.getTime(), numberOfProcessInstances));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {

                }
            }
            
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {

                }
            }
            
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
        
        // process db information and construct single map with data
        
        Iterator<String> nodes = enterStats.keySet().iterator();
        while (nodes.hasNext()) {
            Map<String, Object> nodeProperties = new HashMap<String, Object>();
            String node = (String) nodes.next();
            NodeStatistic enterStat = enterStats.get(node);
            NodeStatistic exitStat = exitStats.remove(node);
            if (exitStat != null) {
                long minExec = exitStat.getMinTimeStamp() - enterStat.getMinTimeStamp();
                long maxExec = exitStat.getMaxTimeStamp() - enterStat.getMaxTimeStamp();
            
                nodeProperties.put("duration", new Double(StatUtils.mean(new double[]{minExec, maxExec})).longValue());
                nodeProperties.put("max-exec", maxExec);
                nodeProperties.put("min-exec", minExec);
                nodeProperties.put("range", (maxExec - minExec)/2);
                nodeProperties.put("numberOfInstance", enterStat.getInstances());
            } else {
                nodeProperties.put("duration", 0);
                nodeProperties.put("max-exec", 0);
                nodeProperties.put("min-exec", 0);
                nodeProperties.put("range", 0);
                nodeProperties.put("numberOfInstance", enterStat.getInstances());
            }
            this.processStatistics.put(node, nodeProperties);
        }
        
        if (!exitStats.isEmpty()) {
            nodes = exitStats.keySet().iterator();
            while (nodes.hasNext()) {
                Map<String, Object> nodeProperties = new HashMap<String, Object>();
                String node = (String) nodes.next();
                NodeStatistic exitStat = exitStats.get(node);

                nodeProperties.put("duration", 0);
                nodeProperties.put("max-exec", 0);
                nodeProperties.put("min-exec", 0);
                nodeProperties.put("range", 0);
                nodeProperties.put("numberOfInstance", exitStat.getInstances());
                
                this.processStatistics.put(node, nodeProperties);
            }
            
        }
        this.processLoaded = true;
    }
    
    /*
     * Experimental  - not really working yet....
     */
    public double calculatePathProbability(SimulationPath path) {
        
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            
            connection = bamDataSource.getConnection();
            
            // collect information about node enter events
            pstmt = connection.prepareStatement(PROCESS_INSTANCE_COUNT_QUERY);
            pstmt.setString(1, processId);
            rs = pstmt.executeQuery();
            Integer instanceCount = 1;
            if (rs.next()) {
                instanceCount = rs.getInt(1);
            }
            
            String parameters = buildParameterPlaceHolder(path.getActivityIds().size());
            String query = PROCESS_INSTANCE_COUNT_FOR_PATH_QUERY.replaceFirst("@1", parameters);
            pstmt = connection.prepareStatement(query);
            pstmt.setString(1, processId);
            int parameterIndex = 2;
            
            for (String node : path.getActivityIds()) {
                pstmt.setString(parameterIndex, node.replaceFirst("_", ""));
                parameterIndex++;
            }
            
            rs = pstmt.executeQuery();
            Integer pathcount = 0;
            if (rs.next()) {
                pathcount = rs.getInt(1)/2;
            }
            
            double result = (100 * pathcount) / instanceCount;
            
            path.setProbability(result);
            
            return result;
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {

                }
            }
            
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {

                }
            }
            
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
        
        return -1;
        
    }

    public Map<String, Object> getProcessDataForNode(Node node) {
        return null;
    }

    private String buildParameterPlaceHolder(int size) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < size; i++) {
            result.append("?,");
        }
        // remove last comma
        result.deleteCharAt(result.length()-1);
        return result.toString();
    }
}
