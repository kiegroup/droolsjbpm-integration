/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
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
import org.drools.benchmark.model.manners.*;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import java.io.*;
import java.util.*;

public class MannersBenchmark extends AbstractBenchmark {

    private KieSession ksession;

    @Override
    public void init(BenchmarkDefinition definition) {
        KieBase kbase = createKnowledgeBase(createKnowledgeBuilder("manners.drl"));
        ksession = kbase.newKieSession();

        InputStream is = getClass().getClassLoader().getResourceAsStream("manners128.dat");
        List list = getInputObjects( is );
        for ( Iterator it = list.iterator(); it.hasNext(); ) {
            Object object = it.next();
            ksession.insert( object );
        }

        ksession.insert( new Count( 1 ) );
    }

    public void execute(int repNr) {
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }

    /**
     * Convert the facts from the <code>InputStream</code> to a list of
     * objects.
     */
    private List<Object> getInputObjects(InputStream inputStream) {
        List<Object> list = new ArrayList<Object>();

        try {
            BufferedReader br = new BufferedReader( new InputStreamReader( inputStream ) );

            String line;
            while ( (line = br.readLine()) != null ) {
                if ( line.trim().length() == 0 || line.trim().startsWith( ";" ) ) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer( line,
                                                          "() " );
                String type = st.nextToken();

                if ( "guest".equals( type ) ) {
                    if ( !"name".equals( st.nextToken() ) ) {
                        throw new IOException( "expected 'name' in: " + line );
                    }
                    String name = st.nextToken();
                    if ( !"sex".equals( st.nextToken() ) ) {
                        throw new IOException( "expected 'sex' in: " + line );
                    }
                    String sex = st.nextToken();
                    if ( !"hobby".equals( st.nextToken() ) ) {
                        throw new IOException( "expected 'hobby' in: " + line );
                    }
                    String hobby = st.nextToken();

                    Guest guest = new Guest( name,
                                             Sex.resolve( sex ),
                                             Hobby.resolve( hobby ) );

                    list.add( guest );
                }

                if ( "last_seat".equals( type ) ) {
                    if ( !"seat".equals( st.nextToken() ) ) {
                        throw new IOException( "expected 'seat' in: " + line );
                    }
                    list.add( new LastSeat( new Integer( st.nextToken() ).intValue() ) );
                }

                if ( "context".equals( type ) ) {
                    if ( !"state".equals( st.nextToken() ) ) {
                        throw new IOException( "expected 'state' in: " + line );
                    }
                    list.add( new Context( st.nextToken() ) );
                }
            }
            inputStream.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read inputstream properly.", e);
        }

        return list;
    }
}
