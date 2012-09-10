package org.jbpm.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext.Type;

public class PathContextManager {

    private Stack<PathContext> paths = new Stack<PathContext>();
    private List<PathContext> completePaths = new ArrayList<PathContext>();
    
    protected Map<String, FlowElement> catchingEvents = null;

    public Map<String, FlowElement> getCatchingEvents() {
        return catchingEvents;
    }

    public void setCatchingEvents(Map<String, FlowElement> catchingEvents) {
        this.catchingEvents = catchingEvents;
    }
    
    public PathContext getContextFromStack() {
        if (this.paths.isEmpty()) {
            this.paths.push(new PathContext());
        }

        return this.paths.peek();
    }
    
    public void addToPath(FlowElement element, PathContext context) {
        if (context.getType() == Type.ROOT) {
            context.addPathElement(element);
        } else {
            // add nodes to all active contexts
            for (PathContext ctx : this.paths) {
                if (ctx.getType() == PathContext.Type.ACTIVE) {
                    ctx.addPathElement(element);
                }
            }
        }
    }
    
    public void addAllToPath(List<SequenceFlow> elements, PathContext context) {
        if (context.getType() == Type.ROOT) {
            context.addAllPathElement(elements);
        } else {
            // add nodes to all active contexts
            for (PathContext ctx : this.paths) {
                if (ctx.getType() == PathContext.Type.ACTIVE) {
                    ctx.addAllPathElement(elements);
                }
            }
        }
    }
    
    public PathContext cloneGiven(PathContext toclone) {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinishedNoIncrement(toclone.isCanBeFinished());
        clone.setCanBeFinishedCounter(toclone.getCanBeFinishedCounter());
        
        clone.setPathElements(new HashSet<FlowElement>(toclone.getPathElements()));
        
        this.paths.push(clone);
        return clone;
    }
    
    public PathContext cloneGivenWithoutPush(PathContext toclone) {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinishedNoIncrement(toclone.isCanBeFinished());
        clone.setCanBeFinishedCounter(toclone.getCanBeFinishedCounter());
        
        clone.setPathElements(new HashSet<FlowElement>(toclone.getPathElements()));
        toclone.setType(Type.TEMP);
        return clone;
    }
    
    public Stack<PathContext> getPaths() {
        return this.paths;
    }
    
    public void finalizePath() {
        PathContext context = getContextFromStack();
        
        if (context.isCanBeFinished()) {
            
            // no outgoing sequence flow means end of path
            PathContext completePath = this.paths.pop();
            completePath.setType(Type.COMPLETED);
            addToCompleted(completePath);
        }
    }
    
    public void finalizePath(PathContext context) {
        
        if (context.isCanBeFinished()) {

            context.setType(Type.COMPLETED);
            addToCompleted(context);
        }
    }

    
    public void clearCurrentContext() {
        this.paths.pop();
    }
    
    public void complete() {
        for (PathContext context : this.paths) {
            
            if (context.getType() != PathContext.Type.ROOT && context.getType() != PathContext.Type.TEMP) {
                addToCompleted(context);
            }
        }
    }
    
    public List<PathContext> getCompletePaths() {
        return completePaths;
    }
    
    protected void addToCompleted(PathContext context) {
        
        //generate path id
        StringBuffer pathIdElements = new StringBuffer();
        
        for (FlowElement fe : context.getPathElements()) {
            pathIdElements.append(fe.getId());
        }
        context.setPathId("Path"+pathIdElements.toString().hashCode());

        this.completePaths.add(context);
    }
}
