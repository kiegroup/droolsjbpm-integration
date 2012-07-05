package org.jbpm.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.PathContext.Type;

public class PathContextManager {

    private Stack<PathContext> paths = new Stack<PathContext>();
    private List<PathContext> completePaths = new ArrayList<PathContext>();
    
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
                if (ctx.getType() != PathContext.Type.ROOT) {
                    ctx.addPathElement(element);
                }
            }
        }
    }
    
    public PathContext cloneGiven(PathContext toclone) {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinished(toclone.isCanBeFinished());
        
        clone.setPathElements(new ArrayList<FlowElement>(toclone.getPathElements()));
        
        this.paths.push(clone);
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
            this.completePaths.add(completePath);
        }
    }
    
    public void complete() {
        for (PathContext context : this.paths) {
            if (context.getType() != PathContext.Type.ROOT) {
                this.completePaths.add(context);
            }
        }
    }
    
    public List<PathContext> getCompletePaths() {
        return completePaths;
    }
}
