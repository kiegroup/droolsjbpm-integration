package org.drools.sample;

import org.kie.KnowledgeBaseFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;
import org.drools.sample.model.Fire;
import org.drools.sample.model.Room;
import org.drools.sample.model.Sprinkler;

import java.io.File;
import java.util.Random;

public class KContainerTest {

    public static final void main(String[] args) {
        StatefulKnowledgeSession ksession = KnowledgeBaseFactory.getStatefulKnowlegeSession("FireAlarmKBase.session");

        int roomsNumber = 3;

        // init
        Room[] rooms = new Room[roomsNumber];
        for (int i = 0; i < rooms.length; i++) {
            Room room = new Room("Room" + i);
            ksession.insert(room);
            Sprinkler sprinkler = new Sprinkler(room);
            ksession.insert(sprinkler);
            rooms[i] = room;
        }

        // go!
        Random random = new Random();
        int roomNr = random.nextInt(roomsNumber);
        FactHandle fact = ksession.insert(new Fire(rooms[roomNr]));;
        ksession.fireAllRules();
        ksession.retract(fact);
        ksession.fireAllRules();
    }
}
