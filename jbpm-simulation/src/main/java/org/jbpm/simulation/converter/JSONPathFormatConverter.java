/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation.converter;

import java.util.List;
import java.util.Set;

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
            if(completePaths != null && !completePaths.isEmpty()) {
                for(PathContext pc : completePaths) {
                    paths.put(pc.getPathId(), getPathFlowElementsAsString(pc.getPathElements()));
                }
            }
            parent.put("paths", paths);
        } catch (JSONException e) {
            // TODO need logging
            e.printStackTrace();
        }
        return parent;
    }
    
    private String getPathFlowElementsAsString(Set<FlowElement> flowElements) {
        String ret = "";
        if(flowElements != null && !flowElements.isEmpty()) {
            for(FlowElement fe : flowElements) {
                ret += fe.getId();
                ret += "|";
            }
        }
        return ret.endsWith("|") ? ret.substring(0, ret.length() - 1) : ret;
    }

}
