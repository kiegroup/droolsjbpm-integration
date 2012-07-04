package org.jbpm.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.jbpm.simulation.PathContext.Type;

public class ProcessPathFinder {

    private List<FlowElement> processElements = new ArrayList<FlowElement>();
    private List<PathContext> completePaths = new ArrayList<PathContext>();

    private Stack<PathContext> paths = new Stack<PathContext>();
    
    public void finPath(String bpmn2) throws IOException {
        
        
        Bpmn2Resource bpmn2Resource = (Bpmn2Resource) new Bpmn2ResourceFactoryImpl().createResource(URI.createURI("virtual.bpmn2"));
        
        bpmn2Resource.load(this.getClass().getResourceAsStream(bpmn2), null);
        
        EList<EObject> content = bpmn2Resource.getContents();
        
        for (EObject element : content) {
            
            if (element instanceof DocumentRoot) {
                DocumentRoot root = (DocumentRoot) element;
                
                Definitions defs = root.getDefinitions();
                
                EList<EObject> defcontent = defs.eContents();
                
                for (EObject defelement : defcontent) {
                    
                    if (defelement instanceof ItemDefinition) {
//                        System.out.println("Found item def: " + ((ItemDefinition) defelement).getId());
                    } else if (defelement instanceof Process) {
                        Process process = ((Process) defelement);
                        
                        System.out.println("Found process : " + process.getId());
                        
                        // TODO use properties for tests later on
//                        List<Property> properties = process.getProperties();
//                        
//                        for (Property prop : properties) {
//                            System.out.println("Property: " + prop.getId());
//                        }
                        
                        // find flow elements and traverse it find path
                        
                        List<FlowElement> flowElements = process.getFlowElements();
                       
                        for (FlowElement fElement : flowElements) {
                       
                            
                            
                            if (fElement instanceof StartEvent) {
                            
                                processElements.add(0, fElement);
                            }
                        }
                    }
                }
            }
            
        }
        
        // show what was found
        for (FlowElement fe : processElements) {

            if (fe instanceof StartEvent) {
                traverseGraph(fe);
            }
        }
//        // handle single path scnario
//        if (this.paths.size() == 1 && this.completePaths.isEmpty()) {
//            this.completePaths.add(this.paths.pop());
//        } else {
        // handle multi path scenario
            for (PathContext context : this.paths) {
                if (context.getType() != PathContext.Type.ROOT) {
                    this.completePaths.add(context);
                }
            }
        }
//    }

    protected void traverseGraph(FlowElement startAt) {
        
        PathContext context = getContextFromStack();
        addToPath(startAt, context);
        
        List<SequenceFlow> outgoing = null;
        if (startAt instanceof StartEvent) {
            
            outgoing = ((StartEvent) startAt).getOutgoing();
            
        } else if (startAt instanceof Event) {
            
            outgoing = ((Event) startAt).getOutgoing();
        } else if (startAt instanceof Activity) {
        
            outgoing = ((Activity) startAt).getOutgoing();
        } else if (startAt instanceof EndEvent) {
            
           outgoing = ((EndEvent) startAt).getOutgoing();
        } else if (startAt instanceof Gateway) {
           Gateway gateway = ((Gateway) startAt);
           outgoing = gateway.getOutgoing();
        }

        if (outgoing != null && !outgoing.isEmpty() ) {
            
            if (startAt instanceof Gateway) {
                Gateway gateway = ((Gateway) startAt);
                
                if (gateway.getGatewayDirection() == GatewayDirection.DIVERGING) {
                    
                    if (gateway instanceof ExclusiveGateway) {
                    
                        handleExclusiveGateway(outgoing);
                    } else if (gateway instanceof ParallelGateway) {
                        
                        handleParallelGateway(outgoing);
                    } else if (gateway instanceof InclusiveGateway) {
                        
                        handleInclusiveGateway(outgoing);
                    }
                } else {
                    handleSimpleNode(outgoing);
                }
            } else if (startAt instanceof Activity){
                
                handleBoundaryEvent(startAt);
            } else {
            
                handleSimpleNode(outgoing);
            }
        } else {
            finalizePath();
            
        }
        
        
    }
    
    protected void addToPath(FlowElement element, PathContext context) {
        if (context.getType() == Type.ROOT) {
            context.addPathElement(element);
        } else {
            // add nodes to all active contexts
            for (PathContext ctx : this.paths) {
                if (ctx.getType() != PathContext.Type.ROOT) {
                    ctx.addPathElement(element);
                }
            }
        }
    }

    protected PathContext getContextFromStack() {
        if (this.paths.isEmpty()) {
            this.paths.push(new PathContext());
        }
        
        return this.paths.peek();
    }
    
    protected void finalizePath() {
        PathContext context = getContextFromStack();
        
        
        
        if (context.isCanBeFinished() /*&& context.getType() != PathContext.Type.ROOT*/) {
            // no outgoing sequence flow means end of path
            PathContext completePath = this.paths.pop();
            this.completePaths.add(completePath);
        }
    }
    
    
    public List<PathContext> getCompletePaths() {
        return completePaths;
    }
    
    protected void handleExclusiveGateway(List<SequenceFlow> outgoing) {
        List<PathContext> locked = new ArrayList<PathContext>();
        PathContext context = getContextFromStack();
        for (SequenceFlow seqFlow : outgoing) {
            FlowElement target = seqFlow.getTargetRef();
            
            PathContext separatePath = context.cloneGiven(context);
            this.paths.push(separatePath);
            
            traverseGraph(target);
            separatePath.setLocked(true);
            
            locked.add(separatePath);
        }
        
        // unlock
        for (PathContext ctx : locked) {
            ctx.setLocked(false);
        }
    }

    protected void handleSimpleNode(List<SequenceFlow> outgoing) {
        for (SequenceFlow seqFlow : outgoing) {
            FlowElement target = seqFlow.getTargetRef();
            
            traverseGraph(target);
        }
    }

    
    protected void handleInclusiveGateway(List<SequenceFlow> outgoing) {
        // firstly cover simple xor based - number of paths is equal to number of outgoing
        handleExclusiveGateway(outgoing);
        

        
        // next cover all combinations of paths
        if (outgoing.size() > 2) {
            List<SequenceFlow> copy = new ArrayList<SequenceFlow>(outgoing);
            List<SequenceFlow> andCombination = null;
            for (SequenceFlow flow : outgoing) {
                
                // first remove one that we currently processing as that is not a combination
                copy.remove(flow);
                
                for (SequenceFlow copyFlow : copy) {
                    PathContext separatePath = getContextFromStack().cloneCurrent();
                    this.paths.push(separatePath);
                    
                    andCombination = new ArrayList<SequenceFlow>();
                    andCombination.add(flow);
                    andCombination.add(copyFlow);
                    
                    handleParallelGateway(andCombination);
                }
            }
        }
        
      // lastly cover and based - is single path that goes through all at the same time
      handleParallelGateway(outgoing);
        
    }

    protected void handleParallelGateway(List<SequenceFlow> outgoing) {
        PathContext context = getContextFromStack();
        context.setCanBeFinished(false);
        int counter = 0;
        for (SequenceFlow seqFlow : outgoing) {
            counter++;
            FlowElement target = seqFlow.getTargetRef();
            
            if (counter == outgoing.size()) {
                context.setCanBeFinished(true);
            }
            traverseGraph(target);
        }
    }
    
    private void handleBoundaryEvent(FlowElement startAt) {
        PathContext context = getContextFromStack();
        List<SequenceFlow> outgoing = ((Activity) startAt).getOutgoing();
        List<BoundaryEvent> bEvents = ((Activity) startAt).getBoundaryEventRefs();
        if (bEvents != null && bEvents.size() > 0) {
            
            for (BoundaryEvent bEvent : bEvents) {
                addToPath(bEvent, context);
                outgoing.addAll(bEvent.getOutgoing());
                
            }
            handleInclusiveGateway(outgoing);
        } else {
            handleSimpleNode(outgoing);
        }
        
        
        
    }
}
