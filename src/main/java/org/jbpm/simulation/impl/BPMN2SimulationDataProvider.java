package org.jbpm.simulation.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Node;
import org.eclipse.bpmn2.*;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.CostParameters;
import org.jboss.drools.DecimalParameterType;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.ElementParametersType;
import org.jboss.drools.FloatingParameterType;
import org.jboss.drools.NormalDistributionType;
import org.jboss.drools.Parameter;
import org.jboss.drools.ParameterValue;
import org.jboss.drools.PoissonDistributionType;
import org.jboss.drools.ProcessAnalysisDataType;
import org.jboss.drools.RandomDistributionType;
import org.jboss.drools.ResourceParameters;
import org.jboss.drools.Scenario;
import org.jboss.drools.UniformDistributionType;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.util.BPMN2Utils;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

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
        Map<String, Object> properties = new HashMap<String, Object>();
        Scenario scenario = getDefaultScenario(def);
        if(scenario != null) {
        	String baseTimeUnitValue = "";
        	String baseCurrencyUnitValue = "";
        	if(scenario.getScenarioParameters() != null) {
        		baseCurrencyUnitValue = scenario.getScenarioParameters().getBaseCurrencyUnit();
        		baseTimeUnitValue = scenario.getScenarioParameters().getBaseTimeUnit().getName();
            }
        	
        	if(scenario.getElementParameters() != null) {
        		for(ElementParametersType eleType : scenario.getElementParameters()) {
        			if(eleType.getElementId().equals(nodeId)) {
        				if(eleType.getControlParameters() != null && eleType.getControlParameters().getProbability() != null) {
        					FloatingParameterType valType = (FloatingParameterType) eleType.getControlParameters().getProbability().getParameterValue().get(0);
                			properties.put("probability", valType.getValue());
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
                    			} else if(paramValue instanceof RandomDistributionType) {
                    				RandomDistributionType rdt = (RandomDistributionType) paramValue;
                    				properties.put("max", rdt.getMax());
                    				properties.put("min", rdt.getMin());
                    				properties.put("distributiontype", "random");
                    			} else if(paramValue instanceof PoissonDistributionType) {
                    				PoissonDistributionType pdt = (PoissonDistributionType) paramValue;
                    				properties.put("mean", pdt.getMean());
                    				properties.put("distributiontype", "poisson");
                    			}
                    			if(eleType.getTimeParameters().getTimeUnit() != null) {
                    				properties.put("timeunit", eleType.getTimeParameters().getTimeUnit().getName());
                    			} else {
                    				properties.put("timeunit", baseTimeUnitValue);
                    			}
                                if(eleType.getTimeParameters().getWaitTime() != null) {
                                    FloatingParameterType waittimeType = (FloatingParameterType) eleType.getTimeParameters().getWaitTime().getParameterValue().get(0);
                                    properties.put("waittime", waittimeType.getValue());
                                }
        					}
        				}
        				if(eleType.getCostParameters() != null) {
        					CostParameters costParams = eleType.getCostParameters();
        					if(costParams.getUnitCost() != null) {
        						DecimalParameterType unitCostVal = (DecimalParameterType) costParams.getUnitCost().getParameterValue().get(0);
        						properties.put("unitcost", unitCostVal.getValue().toString()); 
        					}
        					properties.put("currency", costParams.getCurrencyUnit() == null ? baseCurrencyUnitValue : costParams.getCurrencyUnit());
        				}
        				if(eleType.getResourceParameters() != null) {
        					ResourceParameters resourceParams = eleType.getResourceParameters();
        					if(resourceParams.getQuantity() != null) {
        						FloatingParameterType quantityVal = (FloatingParameterType) resourceParams.getQuantity().getParameterValue().get(0);
    	            			properties.put("quantity", quantityVal.getValue()); 
        					}
        					if(resourceParams.getWorkinghours() != null) {
        						FloatingParameterType workingHoursVal = (FloatingParameterType) resourceParams.getWorkinghours().getParameterValue().get(0);
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
        for (String sequenceFlowId : path.getSequenceFlowsIds()) {
            double transitionProbability = (Double) getSimulationDataForNode(sequenceFlowId).get(SimulationConstants.PROBABILITY);
            if (transitionProbability > 0) {
                probability = probability * (transitionProbability / 100);
            }
        }
        // calculate probability based on boundary events
        for (String sequenceFlowId : path.getBoundaryEventIds()) {
            double transitionProbability = (Double) getSimulationDataForNode(sequenceFlowId).get(SimulationConstants.PROBABILITY);
            if (transitionProbability > 0) {
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

                if (defs != null && defs.size() > 0) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }

                nodeProperties.put("node.type", "BoundaryEvent:"+eventDef);
            } else if (flowElement instanceof IntermediateCatchEvent) {
                IntermediateCatchEvent boundaryEvent = (IntermediateCatchEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && defs.size() > 0) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "IntermediateCatchEvent:"+eventDef);
            } else if (flowElement instanceof IntermediateThrowEvent) {
                IntermediateThrowEvent boundaryEvent = (IntermediateThrowEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && defs.size() > 0) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "IntermediateThrowEvent:"+eventDef);
            } else if (flowElement instanceof StartEvent) {
                StartEvent boundaryEvent = (StartEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && defs.size() > 0) {
                    eventDef = getEventDefinitionAsString(defs.get(0));
                }
                nodeProperties.put("node.type", "StartEvent:"+eventDef);
            } else if (flowElement instanceof EndEvent) {
                EndEvent boundaryEvent = (EndEvent) flowElement;
                List<EventDefinition> defs = boundaryEvent.getEventDefinitions();
                String eventDef = "";

                if (defs != null && defs.size() > 0) {
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
    	if(def.getRelationships() != null && def.getRelationships().size() > 0) {
        	// current support for single relationship
        	Relationship relationship = def.getRelationships().get(0);
        	for(ExtensionAttributeValue extattrval : relationship.getExtensionValues()) {
                FeatureMap extensionElements = extattrval.getValue();
                @SuppressWarnings("unchecked")
                List<ProcessAnalysisDataType> processAnalysisExtensions = (List<ProcessAnalysisDataType>) extensionElements.get(DroolsPackage.Literals.DOCUMENT_ROOT__PROCESS_ANALYSIS_DATA, true);
                if(processAnalysisExtensions != null && processAnalysisExtensions.size() > 0) {
                	ProcessAnalysisDataType processAnalysis = processAnalysisExtensions.get(0);
                	if(processAnalysis.getScenario() != null && processAnalysis.getScenario().size() > 0) {
                		return processAnalysis.getScenario().get(0);
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
