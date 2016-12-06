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

package org.jbpm.simulation;

import java.util.*;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;

public class PathContext {

    private final int maxElementsSize = Integer.parseInt(System.getProperty("org.jbpm.simulation.max.elements", "500"));
    
    public enum Type {
        ROOT,
        ACTIVE,
        COMPLETED,
        TEMP;
    }

    private Set<FlowElement> pathElements = new LinkedHashSet<FlowElement>();
    private Type type;
    private boolean canBeFinished = true;
    private boolean locked = false;
    private String id; 
    private int canBeFinishedCounter = 0;
    private String pathId;

    private Set<FlowElement> visitedSplitPoint = new LinkedHashSet<FlowElement>();

    private FlowElement splitOrigin = null;

    protected int getCanBeFinishedCounter() {
        return canBeFinishedCounter;
    }

    protected void setCanBeFinishedCounter(int canBeFinishedCounter) {
        this.canBeFinishedCounter = canBeFinishedCounter;
    }

    public PathContext() {
    	this.id = String.valueOf(UUID.randomUUID());
    	this.setType(Type.ROOT);
    }
    
    public PathContext(Type type) {
    	this.id = String.valueOf(UUID.randomUUID());
        this.setType(type);
    }
    
    public void addPathElement(FlowElement element) {
        checkSize();
        if (!locked) {
            this.pathElements.add(element);
        }
    }
    
    public void removePathElement(FlowElement element) {
        if (!locked) {
            this.pathElements.remove(element);
        }
    }
    
    public void addAllPathElement(List<SequenceFlow> elements) {
        checkSize();
        if (!locked) {
            this.pathElements.addAll(elements);
        }
    }
    
    public Set<FlowElement> getPathElements() {
        this.pathElements.removeIf(Objects::isNull);
        return this.pathElements;
    }

    public void setPathElements(Set<FlowElement> pathElements) {
        this.pathElements = pathElements;
    }

    public boolean isCanBeFinished() {
        return canBeFinished;
    }
    
    public void setCanBeFinishedNoIncrement(boolean canBeFinished) {
        this.canBeFinished = canBeFinished;
    }

    public void setCanBeFinished(boolean canBeFinished) {
        if (canBeFinished) {
            
            if (canBeFinishedCounter == 1) {
                this.canBeFinished = true;
            }
            canBeFinishedCounter--;
        } else {

            if (canBeFinishedCounter == 0) {
                this.canBeFinished = false;
            } 
            canBeFinishedCounter++;
            
        }
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
    
    public String getId() {
    	return this.id;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public void addVisitedSplitPoint(FlowElement element) {
        this.visitedSplitPoint.add(element);
    }

    public Set<FlowElement> getVisitedSplitPoint() {
        return visitedSplitPoint;
    }

    public void setVisitedSplitPoint(Set<FlowElement> visitedSplitPoint) {
        this.visitedSplitPoint = visitedSplitPoint;
    }


    protected void checkSize() {
        if (pathElements.size() > maxElementsSize) {
            throw new RuntimeException("Unable to calculate path elements of the process - max size (" + maxElementsSize + ") of elements exceeded");
        }
    }

    public FlowElement getSplitOrigin() {
        return splitOrigin;
    }

    public void setSplitOrigin(FlowElement splitOrigin) {
        this.splitOrigin = splitOrigin;
    }


}
