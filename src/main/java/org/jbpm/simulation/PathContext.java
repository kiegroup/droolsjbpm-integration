package org.jbpm.simulation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;

public class PathContext {
    
    enum Type {
        ROOT,
        ACTIVE;
    }

    private List<FlowElement> pathElements = new ArrayList<FlowElement>();
    private Type type;
    private boolean canBeFinished = true;
    private boolean locked = false;
    
    public PathContext() {
        this.setType(Type.ROOT);
    }
    
    public PathContext(Type type) {
        this.setType(type);
    }
    
    public void addPathElement(FlowElement element) {
        if (!locked) {
            this.pathElements.add(element);
        }
    }
    
    public List<FlowElement> getPathElements() {
        return this.pathElements;
    }

    public void setPathElements(List<FlowElement> pathElements) {
        this.pathElements = pathElements;
    }
    
    public PathContext cloneCurrent() {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinished(this.isCanBeFinished());
        
        clone.setPathElements(new ArrayList<FlowElement>(getPathElements()));
        
        return clone;
    }
    
    public PathContext cloneGiven(PathContext toclone) {
        
        PathContext clone = new PathContext(Type.ACTIVE);
        clone.setCanBeFinished(toclone.isCanBeFinished());
        
        clone.setPathElements(new ArrayList<FlowElement>(toclone.getPathElements()));
        
        return clone;
    }

    public boolean isCanBeFinished() {
        return canBeFinished;
    }

    public void setCanBeFinished(boolean canBeFinished) {
        this.canBeFinished = canBeFinished;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
