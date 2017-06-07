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
import org.drools.benchmark.model.*;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.util.*;

public class FireAlarmBenchmark extends AbstractBenchmark {
    private final int roomsNumber;

    private String[] drlFile;

    private KieSession ksession;

    private Room[] rooms;

    private Random random = new Random();

    public FireAlarmBenchmark(int roomsNumber, String drlFile) {
        this.roomsNumber = roomsNumber;
        this.drlFile = drlFile.split(",");
    }

    @Override
    public void init(BenchmarkDefinition definition) {
        KieBase kbase = createKnowledgeBase(createKnowledgeBuilder(drlFile));
        ksession = kbase.newKieSession();

        rooms = new Room[roomsNumber];
        for (int i = 0; i < roomsNumber; i++) {
            Room room = new Room("Room" + i);
            ksession.insert(room);
            Sprinkler sprinkler = new Sprinkler(room);
            ksession.insert(sprinkler);
            rooms[i] = room;
        }
    }

    public void execute(int repNr) {
        int roomNr = random.nextInt(roomsNumber);
        FactHandle fact = ksession.insert(new Fire(rooms[roomNr]));;
        ksession.fireAllRules();
        ksession.retract(fact);
        ksession.fireAllRules();
    }

    @Override
    public void terminate() {
        ksession.dispose(); // Stateful rule session must always be disposed when finished
    }
}
