/**
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.remote.rest.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.KieBase;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.process.WorkflowProcess;
import org.kie.services.remote.rest.graph.jaxb.ActiveNodeInfo;
import org.kie.services.remote.rest.graph.jaxb.DiagramInfo;
import org.kie.services.remote.rest.graph.jaxb.DiagramNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code needs to be finished -- it was pulled from the old jbpm-gwt module and the bpm-console projec. 
 * 
 * The idea is to use this (and the underlying classes in the .jaxb package) to duplicate the image and image info functions
 * that the old gwt-console had. 
 * 
 * /gwt-console-server/rs/process/definition/{id}/image
 * /gwt-console-server/rs/process/instance/{id}/activeNodeInfo
 *
 */
public class GraphViewerPluginImpl {
    
	private static final Logger logger = LoggerFactory.getLogger(GraphViewerPluginImpl.class);
	private KieBase kbase;
	

	public List<ActiveNodeInfo> getActiveNodeInfo(String instanceId) {
		ProcessInstanceLog processInstance = JPAProcessInstanceDbLog.findProcessInstance(new Long(instanceId));
		if (processInstance == null) {
			throw new IllegalArgumentException("Could not find process instance " + instanceId);
		} 
		Map<String, NodeInstanceLog> nodeInstances = new HashMap<String, NodeInstanceLog>();
		for (NodeInstanceLog nodeInstance: JPAProcessInstanceDbLog.findNodeInstances(new Long(instanceId))) {
			if (nodeInstance.getType() == NodeInstanceLog.TYPE_ENTER) {
				nodeInstances.put(nodeInstance.getNodeInstanceId(), nodeInstance);
			} else {
				nodeInstances.remove(nodeInstance.getNodeInstanceId());
			}
		}
		if (!nodeInstances.isEmpty()) {
			List<ActiveNodeInfo> result = new ArrayList<ActiveNodeInfo>();
			for (NodeInstanceLog nodeInstance: nodeInstances.values()) {
				boolean found = false;
				DiagramInfo diagramInfo = getDiagramInfo(processInstance.getProcessId());
				if (diagramInfo != null) {
    				for (DiagramNodeInfo nodeInfo: diagramInfo.getNodeList()) {
    					if (nodeInfo.getName().equals("id=" + nodeInstance.getNodeId())) {
    						result.add(new ActiveNodeInfo(diagramInfo.getWidth(), diagramInfo.getHeight(), nodeInfo));
    						found = true;
    						break;
    					}
    				}
				} else {
				    throw new IllegalArgumentException("Could not find info for diagram for process " + processInstance.getProcessId());
				}
				if (!found) {
					throw new IllegalArgumentException("Could not find info for node "
						+ nodeInstance.getNodeId() + " of process " + processInstance.getProcessId());
				}
			}
			return result;
		}
		return null;
	}

	public DiagramInfo getDiagramInfo(String processId) {
		Process process = kbase.getProcess(processId);
		if (process == null) {
		    
		    return null;
		    
		}

		DiagramInfo result = new DiagramInfo();
		// TODO: diagram width and height?
		result.setWidth(932);
		result.setHeight(541);
		List<DiagramNodeInfo> nodeList = new ArrayList<DiagramNodeInfo>();
		if (process instanceof WorkflowProcess) {
			addNodesInfo(nodeList, ((WorkflowProcess) process).getNodes(), "id=");
		}
		result.setNodeList(nodeList);
		return result;
	}
	
	private void addNodesInfo(List<DiagramNodeInfo> nodeInfos, Node[] nodes, String prefix) {
		for (Node node: nodes) {
			nodeInfos.add(new DiagramNodeInfo(
				prefix + node.getId(),
				(Integer) node.getMetaData().get("x"),
				(Integer) node.getMetaData().get("y"),
				(Integer) node.getMetaData().get("width"),
				(Integer) node.getMetaData().get("height")));
			if (node instanceof NodeContainer) {
				addNodesInfo(nodeInfos, ((NodeContainer) node).getNodes(), prefix + node.getId() + ":");
			}
		}
	}

	public byte[] getProcessImage(String processId) {
	    // TODO? 
	    return null;
	}
	
	private static final int BUFFER_SIZE = 512;

	public static int transfer(InputStream in, OutputStream out) throws IOException {
		int total = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = in.read(buffer);
		while (bytesRead != -1) {
			out.write(buffer, 0, bytesRead);
			total += bytesRead;
			bytesRead = in.read(buffer);
		}
		return total;
	}

	public URL getDiagramURL(String id) {
	    // TODO
	    
		URL result = GraphViewerPluginImpl.class.getResource("/" + id + ".png");
		if (result != null) {
			return result;
		}
		
		return null;
	}

	public List<ActiveNodeInfo> getNodeInfoForActivities(
			String processDefinitionId, List<String> activities) {
		// TODO Auto-generated method stub
		return new ArrayList<ActiveNodeInfo>();
	}

}
