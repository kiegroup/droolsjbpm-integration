package org.jbpm.simulation.converter;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.PathFormatConverter;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONPathFormatConverter implements PathFormatConverter<JSONObject> {

    public JSONObject convert(List<PathContext> completePaths) {
        JSONObject parent = new JSONObject();
        JSONObject paths = new JSONObject();
        try {
            if(completePaths != null && completePaths.size() > 0) {
                for(PathContext pc : completePaths) {
                    paths.put(pc.getId(), getPathFlowElementsAsString(pc.getPathElements()));
                }
            }
            parent.put("paths", paths);
        } catch (JSONException e) {
            // TODO need logging
            e.printStackTrace();
        }
        return parent;
    }
    
    private String getPathFlowElementsAsString(List<FlowElement> flowElements) {
        String ret = "";
        if(flowElements != null && flowElements.size() > 0) {
            for(FlowElement fe : flowElements) {
                ret += fe.getId();
                ret += "|";
            }
        }
        return ret.endsWith("|") ? ret.substring(0, ret.length() - 1) : ret;
    }

}
