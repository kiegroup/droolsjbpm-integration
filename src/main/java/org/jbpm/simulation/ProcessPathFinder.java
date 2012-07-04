package org.jbpm.simulation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jbpm.simulation.PathContext.Type;
import org.jbpm.simulation.util.JBPMBpmn2ResourceFactoryImpl;
import org.jbpm.simulation.util.JBPMBpmn2ResourceImpl;

public class ProcessPathFinder {

	private List<FlowElement> processElements = new ArrayList<FlowElement>();
	private Map<String, FlowElement> catchingEvents = new HashMap<String, FlowElement>();
	private List<PathContext> completePaths = new ArrayList<PathContext>();
	private Stack<PathContext> paths = new Stack<PathContext>();
	
	public void findPath(String bpmn2) throws IOException {
		findPath(getDefinitions(streamToString(this.getClass()
				.getResourceAsStream(bpmn2))));
	}

	public void findPath(Definitions def) throws IOException {
		List<RootElement> rootElements = def.getRootElements();
		for (RootElement root : rootElements) {
			if (root instanceof Process) {
				Process process = (Process) root;
				// find flow elements and traverse it find path
				List<FlowElement> flowElements = process.getFlowElements();
				for (FlowElement fElement : flowElements) {
					if (fElement instanceof StartEvent) {
						processElements.add(0, fElement);
					} else if (fElement instanceof IntermediateCatchEvent) {
						String key = null;
						List<EventDefinition> eventDefinitions = ((IntermediateCatchEvent) fElement)
								.getEventDefinitions();
						if (eventDefinitions != null) {
							for (EventDefinition edef : eventDefinitions) {
								if (edef instanceof SignalEventDefinition) {
									key = ((SignalEventDefinition) edef)
											.getSignalRef();
								} else if (edef instanceof MessageEventDefinition) {
									key = ((MessageEventDefinition) edef)
											.getMessageRef().getId();
								} else if (edef instanceof LinkEventDefinition) {
									key = ((LinkEventDefinition) edef).getName();
								} else if (edef instanceof CompensateEventDefinition) {
									key = ((CompensateEventDefinition) edef)
											.getActivityRef().getId();
								}
								if (key != null) {
									catchingEvents.put(key, fElement);
								}
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
		for (PathContext context : this.paths) {
			if (context.getType() != PathContext.Type.ROOT) {
				this.completePaths.add(context);
			}
		}
	}

	protected void traverseGraph(FlowElement startAt) {
		PathContext context = getContextFromStack();
		addToPath(startAt, context);
		List<SequenceFlow> outgoing = null;
		if (startAt instanceof StartEvent) {

			outgoing = ((StartEvent) startAt).getOutgoing();
		} else if (startAt instanceof SubProcess) {
            SubProcess subProcess = ((SubProcess) startAt);
            
            // process internal nodes of the sub process
            List<FlowElement> sbElements = subProcess.getFlowElements();
            StartEvent start = null;
            for (FlowElement sbElement : sbElements) {
                
                if (sbElement instanceof StartEvent) {
                    start = (StartEvent) sbElement;
                    break;
                }
            }
            boolean canBeFinsihed = getContextFromStack().isCanBeFinished();
            getContextFromStack().setCanBeFinished(false);
            traverseGraph(start);
            getContextFromStack().setCanBeFinished(canBeFinsihed);
            
            outgoing = subProcess.getOutgoing();
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

		if (outgoing != null && !outgoing.isEmpty()) {

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
			} else if (startAt instanceof Activity) {

				handleBoundaryEvent(startAt);
			} else {

				handleSimpleNode(outgoing);
			}
		} else {
			List<EventDefinition> throwDefinitions = getEventDefinitions(startAt);

			if (throwDefinitions != null && throwDefinitions.size() > 0) {
				for (EventDefinition def : throwDefinitions) {
					String key = "";
					if (def instanceof SignalEventDefinition) {
						key = ((SignalEventDefinition) def).getSignalRef();
					} else if (def instanceof MessageEventDefinition) {
						key = ((MessageEventDefinition) def).getMessageRef()
								.getId();
					} else if (def instanceof LinkEventDefinition) {
						key = ((LinkEventDefinition) def).getName();
					} else if (def instanceof CompensateEventDefinition) {
						key = ((CompensateEventDefinition) def)
								.getActivityRef().getId();
					}

					FlowElement catchEvent = this.catchingEvents.get(key);

					if (catchEvent != null) {
						traverseGraph(catchEvent);
					} else {
						// not supported event definition
						finalizePath();
					}
				}
			} else {
				finalizePath();
			}
		}
	}

	protected List<EventDefinition> getEventDefinitions(FlowElement startAt) {
		List<EventDefinition> throwDefinitions = null;

		if (startAt instanceof IntermediateThrowEvent) {
			throwDefinitions = ((IntermediateThrowEvent) startAt)
					.getEventDefinitions();

		} else if (startAt instanceof EndEvent) {
			EndEvent end = (EndEvent) startAt;

			throwDefinitions = end.getEventDefinitions();
		}

		return throwDefinitions;
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

		if (context.isCanBeFinished() /*
									 * && context.getType() !=
									 * PathContext.Type.ROOT
									 */) {
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
			addToPath(seqFlow, separatePath);
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
			addToPath(seqFlow, getContextFromStack());
			traverseGraph(target);
		}
	}

	protected void handleInclusiveGateway(List<SequenceFlow> outgoing) {
		// firstly cover simple xor based - number of paths is equal to number
		// of outgoing
		handleExclusiveGateway(outgoing);

		// next cover all combinations of paths
		if (outgoing.size() > 2) {
			List<SequenceFlow> copy = new ArrayList<SequenceFlow>(outgoing);
			List<SequenceFlow> andCombination = null;
			for (SequenceFlow flow : outgoing) {

				// first remove one that we currently processing as that is not
				// a combination
				copy.remove(flow);

				for (SequenceFlow copyFlow : copy) {
					PathContext separatePath = getContextFromStack()
							.cloneCurrent();
					this.paths.push(separatePath);

					andCombination = new ArrayList<SequenceFlow>();
					andCombination.add(flow);
					andCombination.add(copyFlow);

					handleParallelGateway(andCombination);
				}
			}
		}

		// lastly cover and based - is single path that goes through all at the
		// same time
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
			addToPath(seqFlow, context);
			traverseGraph(target);
		}
	}

	private void handleBoundaryEvent(FlowElement startAt) {
		PathContext context = getContextFromStack();
		List<SequenceFlow> outgoing = ((Activity) startAt).getOutgoing();
		List<BoundaryEvent> bEvents = ((Activity) startAt)
				.getBoundaryEventRefs();
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

	public Definitions getDefinitions(String xml) {
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet
					.getResourceFactoryRegistry()
					.getExtensionToFactoryMap()
					.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
							new JBPMBpmn2ResourceFactoryImpl());
			resourceSet.getPackageRegistry().put(
					"http://www.omg.org/spec/BPMN/20100524/MODEL",
					Bpmn2Package.eINSTANCE);
			JBPMBpmn2ResourceImpl resource = (JBPMBpmn2ResourceImpl) resourceSet
					.createResource(URI
							.createURI("inputStream://dummyUriWithValidSuffix.xml"));
			resource.getDefaultLoadOptions().put(
					JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
			resource.setEncoding("UTF-8");
			Map<String, Object> options = new HashMap<String, Object>();
			options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			resource.load(is, options);

			EList<Diagnostic> warnings = resource.getWarnings();

			if (warnings != null && !warnings.isEmpty()) {
				for (Diagnostic diagnostic : warnings) {
					System.out.println("Warning: " + diagnostic.getMessage());
				}
			}

			EList<Diagnostic> errors = resource.getErrors();
			if (errors != null && !errors.isEmpty()) {
				for (Diagnostic diagnostic : errors) {
					System.out.println("Error: " + diagnostic.getMessage());
				}
				throw new IllegalStateException(
						"Error parsing process definition");
			}

			return ((DocumentRoot) resource.getContents().get(0))
					.getDefinitions();
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public static String streamToString(InputStream is) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
}
