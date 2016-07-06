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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.jbpm.simulation.PathContext.Type;

public class PathContextManager {

    private final int maxPathSize = Integer.parseInt(System.getProperty("org.jbpm.simulation.max.paths", "100"));

    private Stack<PathContext> paths = new Stack<PathContext>();
    private List<PathContext> completePaths = new ArrayList<PathContext>();
    private Set<String> completedPathsIds = new HashSet<String>();
    
    protected Map<String, FlowElement> catchingEvents = null;

    public Map<String, FlowElement> getCatchingEvents() {
        return catchingEvents;
    }

    public void setCatchingEvents(Map<String, FlowElement> catchingEvents) {
        this.catchingEvents = catchingEvents;
    }
    
    public PathContext getContextFromStack() {
        checkSize();
        if (this.paths.isEmpty()) {
            this.paths.push(new PathContext());
        }

        return this.paths.peek();
    }

    public Stack<PathContext> getContextsFromStack() {
        checkSize();
        if (this.paths.isEmpty()) {
            this.paths.push(new PathContext());
        }
        Stack<PathContext> contexts = new Stack<PathContext>();
        for (PathContext ctx : this.paths) {
            if (ctx.getType() == PathContext.Type.ACTIVE) {
                contexts.add(cloneGivenWithoutPush(ctx));
            }
        }
        if (contexts.isEmpty()) {
            contexts.add(this.paths.peek());
        }
        return contexts;
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

    public void finalizePathOnLeave() {

        Iterator<PathContext> it = paths.iterator();
        while (it.hasNext()) {
            PathContext context = it.next();

            if (context.isCanBeFinished() && context.getType() == Type.ACTIVE) {

                // no outgoing sequence flow means end of path
                PathContext completePath = context;
                completePath.setType(Type.COMPLETED);
                addToCompleted(completePath);
                it.remove();
            }
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

        int elementsId = pathIdElements.toString().hashCode();
        context.setPathId("Path" + elementsId + "-" + this.completePaths.size());
        if (!completedPathsIds.contains(elementsId+"")) {
            this.completePaths.add(context);
            completedPathsIds.add(elementsId+"");
        }
    }

    protected void checkSize() {
        if (paths.size() > maxPathSize) {
            throw new RuntimeException("Unable to calculate paths of the process - max size (" + maxPathSize + ") of paths exceeded");
        }
    }

}
