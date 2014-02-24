package org.jbpm.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
    
    public void removeFromPath(FlowElement element, PathContext context) {
        if (context.getType() == Type.ROOT) {
            context.removePathElement(element);
        } else {
            // add nodes to all active contexts
            for (PathContext ctx : this.paths) {
                if (ctx.getType() == PathContext.Type.ACTIVE) {
                    ctx.removePathElement(element);
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
        
        clone.setPathElements(new LinkedHashSet<FlowElement>(toclone.getPathElements()));
        clone.setVisitedSplitPoint(new LinkedHashSet<FlowElement>(toclone.getVisitedSplitPoint()));
        
        this.paths.push(clone);
        return clone;
    }
    
    public PathContext cloneGivenWithoutPush(PathContext toclone) {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinishedNoIncrement(toclone.isCanBeFinished());
        clone.setCanBeFinishedCounter(toclone.getCanBeFinishedCounter());
        
        clone.setPathElements(new LinkedHashSet<FlowElement>(toclone.getPathElements()));
        clone.setVisitedSplitPoint(new LinkedHashSet<FlowElement>(toclone.getVisitedSplitPoint()));
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
        List<FlowElement> list = new ArrayList<FlowElement>(context.getPathElements());
        Collections.sort(list, new Comparator<FlowElement>() {
    
            public int compare(FlowElement o1, FlowElement o2) {
                
                return o1.getId().compareTo(o2.getId());
            }
        });
        
        for (FlowElement fe : list) {
            pathIdElements.append(fe.getId());
        }
        context.setPathId("Path"+pathIdElements.toString().hashCode()+ "-" + this.completePaths.size());
        this.completePaths.add(context);
    }
}
