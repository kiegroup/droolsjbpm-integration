/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.grid.services.strategies;

import java.util.Map;
import org.drools.grid.services.ExecutionEnvironment;


/**
 *
 * @author salaboy
 */
public class RandomEnvironmentSelectionStrategy implements ExecutionEnvironmentSelectionStrategy{

    private Map<String, ExecutionEnvironment> environments;
    public ExecutionEnvironment getBestExecutionEnvironment() {
        int elementToGet = (new Double(Math.random() * 100).intValue() % environments.size());
        int counter = 0;
        ExecutionEnvironment selectedEnv = null;
        for(String key : environments.keySet()){
           if(counter == elementToGet){
            selectedEnv = environments.get(key);
           }
           counter++;
        }
        return selectedEnv;
    }

    public void setExecutionEnvironment(Map<String, ExecutionEnvironment> environments) {
        this.environments = environments;
    }



    public ExecutionEnvironment getBestExecutionEnvironment(Map<String, ExecutionEnvironment> executionEnvironments) {
        setExecutionEnvironment(environments);
        return getBestExecutionEnvironment();
    }

}
