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

package org.jbpm.simulation.impl;

import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.ThrowLinkNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.factory.CreateNewNodeFactory;
import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;

public class SimulationNodeInstanceFactoryRegistry extends NodeInstanceFactoryRegistry {

    private static SimulationNodeInstanceFactoryRegistry instance;
    
    public static SimulationNodeInstanceFactoryRegistry getInstance() {
        if (instance == null) {
            instance = new SimulationNodeInstanceFactoryRegistry();
        }
        
        return instance;
    }
    
    private SimulationNodeInstanceFactoryRegistry() {
        
      register( RuleSetNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( Split.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( Join.class,
                new ReuseNodeFactory( SimulationJoinNodeInstance.class ) );
      register( StartNode.class,
                new CreateNewNodeFactory( SimulationStartNodeInstance.class ) );
      register( EndNode.class,
                new CreateNewNodeFactory( SimulationEndNodeInstance.class ) );
      register( MilestoneNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( SubProcessNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( ActionNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( WorkItemNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( TimerNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( FaultNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( CompositeNode.class,
                new CreateNewNodeFactory( SimulationCompositeNodeInstance.class ) );
      register( CompositeContextNode.class,
                new CreateNewNodeFactory( SimulationCompositeNodeInstance.class ) );
      register( HumanTaskNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( ForEachNode.class,
                new CreateNewNodeFactory( SimulationCompositeNodeInstance.class ) );
      register( EventNode.class,
                new CreateNewNodeFactory( SimulationEventNodeInstance.class ) );
      register( StateNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      register( DynamicNode.class,
                new CreateNewNodeFactory( SimulationNodeInstance.class ) );
      
      register(CatchLinkNode.class, new CreateNewNodeFactory(
              SimulationNodeInstance.class));
      register(ThrowLinkNode.class, new CreateNewNodeFactory(
              SimulationNodeInstance.class));
    }
}
