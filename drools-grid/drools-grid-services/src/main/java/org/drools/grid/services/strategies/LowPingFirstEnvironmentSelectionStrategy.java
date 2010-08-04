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
 *
 * The Environment with lowest ping will be selected
 */
public class LowPingFirstEnvironmentSelectionStrategy implements ExecutionEnvironmentSelectionStrategy {

    private Map<String, ExecutionEnvironment> environments;

    public LowPingFirstEnvironmentSelectionStrategy() {
    }


    public LowPingFirstEnvironmentSelectionStrategy(Map<String, ExecutionEnvironment> environments) {
        this.environments = environments;
    }


    public ExecutionEnvironment getBestGridResource() {
        ExecutionEnvironment selectedEnv = null;
        for(ExecutionEnvironment env : this.environments.values()){
            if(selectedEnv != null){
//                try {
//                    int oldping = Integer.parseInt(selectedEnv.getInfo().get("ping").toString());
//                    long ping = env.ping();
//                    if (oldping > ping) {
//                        selectedEnv = env;
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(LowPingFirstEnvironmentSelectionStrategy.class.getName()).log(Level.SEVERE, null, ex);
//                }

            }

        }

        return selectedEnv;
    }

    public void setExecutionEnvironments(Map<String, ExecutionEnvironment> environments) {
        this.environments = environments;
    }

    public ExecutionEnvironment getBestExecutionEnvironment(Map<String, ExecutionEnvironment> executionEnvironments) {
        setExecutionEnvironments(environments);
        return getBestGridResource();
    }

    
    
   

}
