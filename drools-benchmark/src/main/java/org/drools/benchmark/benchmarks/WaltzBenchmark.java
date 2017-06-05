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

package org.drools.benchmark.benchmarks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.drools.benchmark.BenchmarkDefinition;
import org.drools.benchmark.model.waltz.Line;
import org.drools.benchmark.model.waltz.Stage;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;

public class WaltzBenchmark extends AbstractBenchmark {

    private KieBase kbase;
    private List<Line> lines = new ArrayList<Line>();

    @Override
    public void init(BenchmarkDefinition definition) {
        try {
            kbase = readRule();
            loadLines("/waltz50.dat");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(int repNr) {
        KieSession session = kbase.newKieSession();
        for (Line l : lines) {
            session.insert( l );
        }
        session.insert( new Stage( Stage.DUPLICATE ) );
        session.fireAllRules();
        session.dispose();
    }

    private KieBase readRule() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newInputStreamResource(getClass().getResourceAsStream("/waltz.drl")), ResourceType.DRL );
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages( kbuilder.getKnowledgePackages() );
        return kbase;
    }

    private void loadLines(final String filename) throws IOException {
        if (!lines.isEmpty()) {
            return;
        }

        BufferedReader reader = new BufferedReader( new InputStreamReader( WaltzBenchmark.class.getResourceAsStream( filename ) ) );
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(".*make line \\^p1 ([0-9]*) \\^p2 ([0-9]*).*");
        String line = reader.readLine();

        while ( line != null ) {
            final Matcher m = pat.matcher( line );
            if ( m.matches() ) {
                Line l = new Line( Integer.parseInt( m.group( 1 ) ), Integer.parseInt( m.group( 2 ) ) );
                lines.add( l );
            }
            line = reader.readLine();
        }
        reader.close();
    }
}
