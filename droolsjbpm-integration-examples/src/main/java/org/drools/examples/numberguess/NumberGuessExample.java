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

package org.drools.examples.numberguess;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;

import java.util.Random;

public class NumberGuessExample {

    public static final void main(String[] args) {
        KieSession ksession = KieServices.Factory.get()
                                        .getKieClasspathContainer()
                                        .newKieSession("NumberGuessKS");

//        KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "log/numberguess");

        ksession.insert( new GameRules( 100,
                                        5 ) );
        ksession.insert( new RandomNumber() );
        ksession.insert( new Game() );

        ksession.startProcess( "Number Guess" );
        ksession.fireAllRules();

//        logger.close();

        ksession.dispose();
    }

    public static class RandomNumber {
        private int randomNumber;

        public RandomNumber() {
            this.randomNumber = new Random().nextInt( 100 );
        }

        public int getValue() {
            return this.randomNumber;
        }
    }

    public static class Guess {
        private int value;

        public Guess(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public String toString() {
            return "Guess " + this.value;
        }
    }

    public static class GameRules {
        private int maxRange;
        private int allowedGuesses;

        public GameRules(int maxRange,
                         int allowedGuesses) {
            this.maxRange = maxRange;
            this.allowedGuesses = allowedGuesses;
        }

        public int getAllowedGuesses() {
            return allowedGuesses;
        }

        public int getMaxRange() {
            return maxRange;
        }

        public static boolean isNumber(String s) {
            try {
                Integer.parseInt(s);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static class Game {
        private int biggest;
        private int smallest;
        private int guessCount;

        public Game() {
            this.guessCount = 0;
            this.biggest = 0;
            this.smallest = 100;
        }

        public void incrementGuessCount() {
            guessCount++;
        }

        public int getBiggest() {
            return this.biggest;
        }

        public int getSmallest() {
            return this.smallest;
        }

        public int getGuessCount() {
            return this.guessCount;
        }

        public void setGuessCount(int guessCount) {
            this.guessCount = guessCount;
        }

        public void setBiggest(int biggest) {
            this.biggest = biggest;
        }

        public void setSmallest(int smallest) {
            this.smallest = smallest;
        }
    }
}
