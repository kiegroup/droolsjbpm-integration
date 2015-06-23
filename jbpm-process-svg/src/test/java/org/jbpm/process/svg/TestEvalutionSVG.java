/*
 * Copyright 2015 JBoss Inc
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

package org.jbpm.process.svg;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestEvalutionSVG {

    @Test
    public void test() throws Exception {
        List<String> completed = new ArrayList<String>();
        completed.add("_343B16DA-961A-49BF-8697-9A86DEAFBAF4");
        List<String> active = new ArrayList<String>();
        active.add("_6063D302-9D81-4C86-920B-E808A45377C2");
        String svg = SVGImageProcessor.transform(
            TestEvalutionSVG.class.getResourceAsStream("/evaluation-svg.svg"), completed, active);
        // PrintWriter out = new PrintWriter("output.svg");
        // out.print(svg);
        // out.close();
    }

    @Test
    public void testByName() throws Exception {
        List<String> completed = new ArrayList<String>();
        completed.add("Self Evaluation");
        List<String> active = new ArrayList<String>();
        active.add("PM Evaluation");
        String svg = SVGImageProcessor.transformByName(
            TestEvalutionSVG.class.getResourceAsStream("/evaluation-svg.svg"), completed, active);
        // PrintWriter out = new PrintWriter("output.svg");
        // out.print(svg);
        // out.close();
    }

}
