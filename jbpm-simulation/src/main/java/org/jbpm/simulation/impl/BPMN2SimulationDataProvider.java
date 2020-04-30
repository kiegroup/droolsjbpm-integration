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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bpsim.*;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.BusinessRuleTask;
import org.eclipse.bpmn2.CancelEventDefinition;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ConditionalEventDefinition;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.Relationship;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.util.BPMN2Utils;
import org.jbpm.simulation.util.SimulationConstants;
import org.kie.api.definition.process.Node;

public class BPMN2SimulationDataProvider implements SimulationDataProvider {
    private Definitions def;

    public BPMN2SimulationDataProvider(Definitions def) {
        this.def = def;
    }
    
    public BPMN2SimulationDataProvider(String bpmn2xml) {
    	this.def = BPMN2Utils.getDefinitions(new ByteArrayInputStream(getBytes(bpmn2xml)));
    }
    
	public BPMN2SimulationDataProvider(InputStream bpmn2Stream) {
		this.def = BPMN2Utils.getDefinitions(bpmn2Stream);
	}
    
    public Map<String, Object> getSimulationDataForNode(Node node) {
        String nodeId = (String) node.getMetaData().get("UniqueId");
        return getSimulationDataForNode(nodeId);
    }
    public Map<String, Object> getSimulationDataForNode(
            String nodeId) {
        boolean reverse = false;
        if (nodeId.startsWith("$reverseprops$")) {
            reverse = true;
            nodeId = nodeId.replaceFirst("\\$reverseprops\\$", "");
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        // default value for probability is 50
        double defaultValue = 50.0;
        if (reverse) {
            defaultValue = 100 - defaultValue;
        }
        properties.put("probability", defaultValue);
        Scenario scenario = getDefaultScenario(def);
        if(scenario != null) {
        	String baseTimeUnitValue = "";
        	String baseCurrencyUnitValue = "";
        	if(scenario.getScenarioParameters() != null) {
        		baseCurrencyUnitValue = scenario.getScenarioParameters().getBaseCurrencyUnit();
        		baseTimeUnitValue = scenario.getScenarioParameters().getBaseTimeUnit().getName();
            }
        	
        	if(scenario.getElementParameters() != null) {
        		for(ElementParameters eleType : scenario.getElementParameters()) {
        			if(eleType.getElementRef().equals(nodeId)) {
        				if(eleType.getControlParameters() != null && eleType.getControlParameters().getProbability() != null) {

                            FlowElement element = null;
                            for (RootElement root : def.getRootElements()) {
                                if (root instanceof Process) {
                                    element = findElementInContainer((FlowElementsContainer) root, nodeId);
                                    if (element != null && element instanceof SequenceFlow) {
                                        element = ((SequenceFlow)element).getSourceRef();
                                    }
                                    break;
                                }
                            }
                            if (element != null && element instanceof ParallelGateway) {
                                // probability should be ignored for parallel gateways so use default value
                                properties.put("probability", 100.0);
                            } else {
                                FloatingParameterType valType = (FloatingParameterType) eleType.getControlParameters().getProbability().getParameterValue().get(0);
                                double value = valType.getValue();
                                if (reverse) {
                                    value = 100 - value;
                                }
                                properties.put("probability", value);
                            }
        				}
        				if(eleType.getTimeParameters() != null) {
        					if(eleType.getTimeParameters().getProcessingTime() != null) {
        						Parameter processingTime = eleType.getTimeParameters().getProcessingTime();
                    			ParameterValue paramValue =  processingTime.getParameterValue().get(0);
                    			if(paramValue instanceof NormalDistributionType) {
                    				NormalDistributionType ndt = (NormalDistributionType) paramValue;
                    				properties.put("mean", ndt.getMean());
                    				properties.put("standarddeviation", ndt.getStandardDeviation());
                    				properties.put("distributiontype", "normal");
                    			} else if(paramValue instanceof UniformDistributionType) {
                    				UniformDistributionType udt = (UniformDistributionType) paramValue;
                    				properties.put("max", udt.getMax());
                    				properties.put("min", udt.getMin());
                    				properties.put("distributiontype", "uniform");
                                // random distribution not supported in bpsim 1.0
//                    			} else if(paramValue instanceof RandomDistributionType) {
//                    				RandomDistributionType rdt = (RandomDistributionType) paramValue;
//                    				properties.put("max", rdt.getMax());
//                    				properties.put("min", rdt.getMin());
//                    				properties.put("distributiontype", "random");
                    			} else if(paramValue instanceof PoissonDistributionType) {
                    				PoissonDistributionType pdt = (PoissonDistributionType) paramValue;
                    				properties.put("mean", pdt.getMean());
                    				properties.put("distributiontype", "poisson");
                    			}
                                properties.put("timeunit", baseTimeUnitValue);

                                if(eleType.getTimeParameters().getWaitTime() != null) {
                                    FloatingParameterType waittimeType = (FloatingParameterType) eleType.getTimeParameters().getWaitTime().getParameterValue().get(0);
                                    properties.put("waittime", waittimeType.getValue());
                                }
        					}
        				}
        				if(eleType.getCostParameters() != null) {
        					CostParameters costParams = eleType.getCostParameters();
        					if(costParams.getUnitCost() != null) {
                                FloatingParameterType unitCostVal = (FloatingParameterType) costParams.getUnitCost().getParameterValue().get(0);
        						properties.put("unitcost", unitCostVal.getValue());
        					}
        					properties.put("currency", baseCurrencyUnitValue);
        				}
        				if(eleType.getResourceParameters() != null) {
        					ResourceParameters resourceParams = eleType.getResourceParameters();
        					if(resourceParams.getQuantity() != null) {
        						FloatingParameterType quantityVal = (FloatingParameterType) resourceParams.getQuantity().getParameterValue().get(0);
    	            			properties.put("quantity", quantityVal.getValue()); 
        					}
        					if(resourceParams.getAvailability() != null) {
        						FloatingParameterType workingHoursVal = (FloatingParameterType) resourceParams.getAvailability().getParameterValue().get(0);
    	            			properties.put("workinghours", workingHoursVal.getValue()); 
        					}
        				}
        			}
        		}
        	}
        }
        return properties;
    }

    protected FlowElement findElementInContainer(FlowElementsContainer container, String id) {
        List<FlowElement> currentContainerElems = container.getFlowElements();
        
        for (FlowElement fElement : currentContainerElems) {
            if (fElement.getId().equals(id) ) {
                return fElement;
            } else if (fElement instanceof FlowElementsContainer) {
                FlowElement fe = findElementInContainer((FlowElementsContainer) fElement, id);
                if (fe != null) {
                    return fe;
                }
            }
        }
        
        return null;
    }
    
    public double calculatePathProbability(SimulationPath path) {
        double probability = 100;
        Set<String> processedSourceElems = new LinkedHashSet<String>();
        for (String sequenceFlowId : path.getSequenceFlowsIds()) {
            String sourceElemId = path.getSeqenceFlowsSources().get(sequenceFlowId);
            // need to test if given source (gateway) was already processed as it can be twice or more when loops are
            // modeled in the process, for probability counting only single outgoing flow is taken - currently supports
            // only XOR and AND gateways
            if (processedSourceElems.contains(sourceElemId)) {
                continue;
            }
            double transitionProbability = (Double) getSimulationDataForNode(sequenceFlowId).get(SimulationConstants.PROBABILITY);
            if (transitionProbability >= 0) {
                probability = probability * (transitionProbability / 100);
                processedSourceElems.add(sourceElemId);
            }
        }
        // calculate probability based on boundary events
        for (String sequenceFlowId : path.getBoundaryEventIds()) {
            double transitionProbability = (Double) getSimulationDataForNode(sequenceFlowId).get(SimulationConstants.PROBABILITY);
            if (transitionProbability >= 0) {
                probability = probability * (transitionProbability / 100);
            }
        }
        double result = probability / 100;

        path.setProbability(result);
        
        return result;
    }

    public Map<String, Object> getProcessDataForNode(Node node) {

        Map<String, Object> nodeProperties = new HashMap<String, Object>();
        FlowElement flowElement = null;
                List<RootElement> rootElements = def.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                flowElement = findElementInContainer((FlowElementsContainer) root, (String) node.getMetaData().get("UniqueId"));
                break;
            }
        }

        if (flowElement != null) {
            if (flowElement instanceof ScriptTask) {

               nodeProperties.put("node.type", "ScriptTask");
            } else if (flowElement instanceof BusinessRuleTask) {

                nodeProperties.put("node.type", "BusinessRuleTask");
            } else if (flowElement instanceof UserTask) {

                nodeProperties.put("node.type", "UserTask");
            } else if (flowElement instanceof SendTask) {

                nodeProperties.put("node.type", "SendTask");
            } else if (flowElement instanceof ServiceTask) {

                nodeProperties.put("node.type", "ServiceTask");
            } else if (flowElement instanceof ReceiveTask) {

                nodeProperties.put("node.type", "ReceiveTask");
            } else if (flowElement instanceof ManualTask) {

                nodeProperties.put("node.type", "ManualTask");
            } else if (flowElement instanceof InclusiveGateway) {

                nodeProperties.put("node.type", "InclusiveGateway");
            } else if (flowElement instanceof ExclusiveGateway) {

                nodeProperties.put("node.type", "ExclusiveGateway");
            } else if (flowElement instanceof ParallelGateway) {

                nodeProperties.put("node.type", "ParallelGateway");
            } else if (flowElement instanceof BoundaryEvent) {
                BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && !defs.isEmpty()) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }

                nodeProperties.put("node.type", "BoundaryEvent:"+eventDef);
            } else if (flowElement instanceof IntermediateCatchEvent) {
                IntermediateCatchEvent boundaryEvent = (IntermediateCatchEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && !defs.isEmpty()) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "IntermediateCatchEvent:"+eventDef);
            } else if (flowElement instanceof IntermediateThrowEvent) {
                IntermediateThrowEvent boundaryEvent = (IntermediateThrowEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && !defs.isEmpty()) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "IntermediateThrowEvent:"+eventDef);
            } else if (flowElement instanceof StartEvent) {
                StartEvent boundaryEvent = (StartEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && !defs.isEmpty()) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "StartEvent:"+eventDef);
            } else if (flowElement instanceof EndEvent) {
                EndEvent boundaryEvent = (EndEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && !defs.isEmpty()) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "EndEvent:"+eventDef);
            }
        } else {
            nodeProperties.put("node.type", "unknown");
        }
        return nodeProperties;
    }

    private static byte[] getBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    private Scenario getDefaultScenario(Definitions def) {
    	if(def.getRelationships() != null && !def.getRelationships().isEmpty()) {
        	// current support for single relationship
        	Relationship relationship = def.getRelationships().get(0);
        	for(ExtensionAttributeValue extattrval : relationship.getExtensionValues()) {
                FeatureMap extensionElements = extattrval.getValue();
                @SuppressWarnings("unchecked")
                List<BPSimDataType> bpsimExtension = (List<BPSimDataType>) extensionElements.get(BpsimPackage.Literals.DOCUMENT_ROOT__BP_SIM_DATA, true);
                if(bpsimExtension != null && !bpsimExtension.isEmpty()) {
                    BPSimDataType bpmsim = bpsimExtension.get(0);
                	if(bpmsim.getScenario() != null && !bpmsim.getScenario().isEmpty()) {
                		return bpmsim.getScenario().get(0);
                	}
                }
        	}
        }
    	return null;
    }

    protected String getEventDefinitionAsString(EventDefinition eventDefinition) {
        if (eventDefinition instanceof SignalEventDefinition) {
            return "signalEventDefinition";
        } else if (eventDefinition instanceof MessageEventDefinition) {
            return "messageEventDefinition";
        } else if (eventDefinition instanceof LinkEventDefinition) {
            return "linkEventDefinition";
        } else if (eventDefinition instanceof CompensateEventDefinition) {
            return "compensateEventDefinition";
        } else if (eventDefinition instanceof ErrorEventDefinition) {
            return "errorEventDefinition";
        } else if (eventDefinition instanceof TimerEventDefinition) {
            return "timerEventDefinition";
        } else if (eventDefinition instanceof ConditionalEventDefinition) {
            return "conditionalEventDefinition";
        } else if (eventDefinition instanceof CancelEventDefinition) {
            return "cancelEventDefinition";
        }

        return "unknown";
    }
}
