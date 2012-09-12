/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.drools.simulation.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.command.Context;
import org.drools.simulation.SimulationPath;
import org.drools.simulation.SimulationStep;
import org.drools.simulation.impl.Simulator.CommandExecutionHandler;

/**
 * StepExecutionHandler implementation that executes the steps of a simulation
 * in 'realtime'.
 * This class will actually sleep the execution thread until the time for the
 * next step has come.
 */
public class RealtimeStepExecutionHandler extends Simulator.DefaultStepExecutionHandler {

    private long lastTime = 0;
    private final float factor;

    /**
     * Same as {@link #RealtimeStepExecutionHandler(float) RealtimeStepExecutionHandler(1)}.
     */
    public RealtimeStepExecutionHandler() {
        this(1);
    }
    
    /**
     * Creates an instance of this execution handler with a specific factor for 
     * the execution time.
     * I.e: A factor of 2 means that the wait time between steps is going to be
     * halved. In the other hand, a factor of 0.5 means that the wait time
     * between steps is going to be doubled.
     * 
     * @param factor a positive number (grater than 0) defining the factor to be used.
     */
    public RealtimeStepExecutionHandler(float factor) {
        if (factor <= 0){
            throw new IllegalArgumentException("factor must be a positive number grater than 0: "+factor);
        }
        this.factor = factor;
    }
    
    /**
     * Executes the simulation step waiting the corresponding time.
     * @param simulatorContext
     * @param pathContext
     * @param path
     * @param step
     * @param executionHandler
     * @return 
     */
    @Override
    public Object execute(SimulatorContext simulatorContext, Context pathContext, SimulationPath path, SimulationStep step, CommandExecutionHandler executionHandler) {
        try {
            Thread.sleep((long)((step.getDistanceMillis()-lastTime)/factor));
            lastTime = step.getDistanceMillis();
            return super.execute(simulatorContext, pathContext, path, step, executionHandler);
        } catch (InterruptedException ex) {
            Logger.getLogger(RealtimeStepExecutionHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
