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

package org.drools.benchmark.model.cep;

public class Match {
    private final Figure figure;
    private final Letter letter;

    public Match(Figure figure, Letter letter) {
        this.figure = figure;
        this.letter = letter;
    }

    @Override
    public String toString() {
        return "Match{" +
                "figure=" + figure +
                ", letter=" + letter +
                '}';
    }
}
