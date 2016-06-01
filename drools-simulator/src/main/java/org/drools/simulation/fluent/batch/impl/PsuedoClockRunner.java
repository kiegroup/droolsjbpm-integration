/*
 * Copyright 2011 JBoss Inc
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

package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.impl.ContextImpl;
import org.drools.core.command.impl.GenericCommand;
import org.drools.simulation.fluent.batch.Batch;
import org.drools.simulation.fluent.batch.BatchRuns;
import org.kie.api.command.Command;
import org.kie.internal.command.Context;

import java.util.*;

public class PsuedoClockRunner {
//    private Map<String, Context>  appContexts;
//    private Map<String, Context>  conversationContexts;


    private Map<String, Context>  contexts;
    //private Map<String, List<Step>> steps;

    private PriorityQueue<Step>           queue;

    public PsuedoClockRunner() {
        contexts = new HashMap<String, Context>();
    }

    public Map<String, Map<String, Object>> execute(BatchRuns batchRuns) {
        Map<String, Context>  requestContexts = new HashMap<String, Context>();

        long distance = 0;

//        for ( String ctxName : batchRuns.getBatches().keySet() ) {
//            List<Step> steps = new ArrayList<Step>();
//            Context ctx = requestContexts.get(ctxName);
//            if (ctx == null) {
//                ContextImpl requestCtx = new ContextImpl(ctxName, null);
//                Context appCtx = contexts.get(ctxName);
//                requestCtx.setParent(appCtx);
//            }
//            //Context ctx = contexts.get(ctxName);
//
//            //queue.addAll(new Step( ctx, batchRun.getBatches().get(ctxName)));
//            for ( Batch b : batchRuns.getBatches().get(ctxName) ) {
//                queue.add(new Step( ctx, b ));
//            }
//        }

        Map<String, Map<String, Object>> results = new HashMap<String, Map<String, Object>>();
        for ( Step s = queue.remove(); !queue.isEmpty(); s = queue.remove() ) {
            Context ctx = s.getContext();
            Batch b = s.getBatch();
            for (Command cmd : b.getCommands() ) {
                ((GenericCommand)cmd).execute(ctx);
            }
        }

        return null;

    }

    public static class Step {
        private Context ctx;
        private Batch batch;

        public Step(Context ctx, Batch batch) {
            this.ctx = ctx;
            this.batch = batch;
        }

        public Context getContext() {
            return ctx;
        }

        public Batch getBatch() {
            return batch;
        }
    }
}
