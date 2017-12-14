/*
 * Copyright 2005 JBoss Inc
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

package org.kie.project.example;

import org.drools.core.util.Drools;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import static org.junit.Assert.assertEquals;

public class Main {

    public static void main(String[] args) {
        KieServices ks = KieServices.get();

        // Install example1 in the local maven repo before to do this
        KieContainer kContainer = ks.newKieContainer(ks.newReleaseId("org.kie", "kie-maven-plugin-example", Drools.getFullVersion()));

        KieSession kSession = kContainer.newKieSession("FireAlarmKBase.session");

        Object room = createRoom(kContainer, "101");
        kSession.insert(room);
        Object sprinkler = createSprinkler(kContainer, room);
        kSession.insert(sprinkler);
        Object fire = createFire(kContainer, room);
        FactHandle fireFH = kSession.insert(fire);

        int rules = kSession.fireAllRules();
        assertEquals(2, rules);

        kSession.delete(fireFH);
        rules = kSession.fireAllRules();
        assertEquals(3, rules);
    }

    private static Object createRoom(KieContainer kContainer, String roomNr) {
        Object o = null;
        try {
            Class cl = kContainer.getClassLoader().loadClass("org.kie.sample.model.Room");
            o = cl.getConstructor(new Class[]{String.class}).newInstance(roomNr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    private static Object createSprinkler(KieContainer kContainer, Object room) {
        Object o = null;
        try {
            Class cl = kContainer.getClassLoader().loadClass("org.kie.sample.model.Sprinkler");
            o = cl.getConstructor(new Class[]{kContainer.getClassLoader().loadClass("org.kie.sample.model.Room")}).newInstance(room);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    private static Object createFire(KieContainer kContainer, Object room) {
        Object o = null;
        try {
            Class cl = kContainer.getClassLoader().loadClass("org.kie.sample.model.Fire");
            o = cl.getConstructor(new Class[]{kContainer.getClassLoader().loadClass("org.kie.sample.model.Room")}).newInstance(room);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
