package org.jbpm.simulation.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jbpm.simulation.ActivitySimulator;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationEvent;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.instance.impl.NodeInstanceFactory;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;

public class SimulationCompositeNodeInstance extends org.jbpm.workflow.instance.node.CompositeNodeInstance {

    private static final long serialVersionUID = -1965605499505300424L;

    @Override
    public void internalTrigger(NodeInstance from, String type) {

        CompositeNode compositeNode = (CompositeNode) getNode();

        for (Node node: compositeNode.getNodes()) {
            if (node instanceof StartNode) {
                StartNode startNode = (StartNode) node;
                if (startNode.getTriggers() == null || startNode.getTriggers().isEmpty()) {
                    NodeInstance nodeInstance = getNodeInstance(startNode);
                    ((org.jbpm.workflow.instance.NodeInstance) nodeInstance).trigger(null, null);

                }
            }
        }
    }

    public org.jbpm.workflow.instance.NodeInstance getNodeInstance(final Node node) {

        NodeInstanceFactory conf = SimulationNodeInstanceFactoryRegistry.getInstance(getProcessInstance().getKnowledgeRuntime().getEnvironment()).getProcessNodeInstanceFactory(node);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal node type: " + node.getClass());
        }
        NodeInstanceImpl nodeInstance = (NodeInstanceImpl) conf.getNodeInstance(node, getProcessInstance(), this);
        if (nodeInstance == null) {
            throw new IllegalArgumentException("Illegal node type: " + node.getClass());
        }
        return nodeInstance;
    }

}
