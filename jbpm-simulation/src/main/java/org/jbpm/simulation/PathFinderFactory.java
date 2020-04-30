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

import java.io.File;
import java.io.InputStream;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.jbpm.simulation.impl.BPMN2PathFinderImpl;

public class PathFinderFactory {

    public static PathFinder getInstance(String bpmn2Xml) {
        return new BPMN2PathFinderImpl(bpmn2Xml);
    }
    
    public static PathFinder getInstance(File bpmn2Xml) {
        return new BPMN2PathFinderImpl(bpmn2Xml);
    }
    
    public static PathFinder getInstance(Definitions bpmn2Defs) {
        return new BPMN2PathFinderImpl(bpmn2Defs);
    }
    
    public static PathFinder getInstance(InputStream bpmn2Stream) {
        return new BPMN2PathFinderImpl(bpmn2Stream);
    }
    
    public static PathFinder getInstance(FlowElementsContainer bpmn2Container) {
        return new BPMN2PathFinderImpl(bpmn2Container);
    }
}
