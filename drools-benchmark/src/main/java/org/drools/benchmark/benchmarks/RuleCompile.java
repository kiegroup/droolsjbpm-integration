/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.benchmark.benchmarks;

import org.drools.benchmark.*;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.io.*;

import java.io.*;
import java.util.*;

public class RuleCompile extends AbstractBenchmark {

    private String[] drlFiles;
    private Map<String, String[]> drls;

    private KnowledgeBuilder kbuilder;

    public RuleCompile(String drlFile) {
        this.drlFiles = drlFile.split(",");
    }

    public void init(BenchmarkDefinition definition) {
        drls = prepareDrls(definition.getRepetitions());
        kbuilder = createKnowledgeBuilder(drlFiles);
    }

    private Map<String, String[]> prepareDrls(int repetitions) {
        Map<String, String[]> map = new HashMap<String, String[]>();
        for (String drlFile : drlFiles) {
            String drl = readFile(drlFile);
            String[] drls = new String[repetitions];
            for (int i = 0; i < drls.length; i++) drls[i] = drl.replaceAll("\\_", "" + i);
            map.put(drlFile, drls);
        }
        return map;
    }

    private String readFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName));
            br = new BufferedReader(isr);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line).append(LINE_SEPARATOR);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (br != null) br.close();
                if (isr != null) isr.close();
            } catch (Exception e) {
                // Ignore
            }
        }
        return sb.toString();
    }

    public void execute(int repNr) {
        for (String drlFile : drlFiles) {
            String drl = drls.get(drlFile)[repNr];
            kbuilder.add(ResourceFactory.newByteArrayResource(drl.getBytes()), ResourceType.DRL);
            if (kbuilder.hasErrors()) {
                System.err.println(drl);
                throw new RuntimeException(kbuilder.getErrors().toString());
            }
        }
    }
}
