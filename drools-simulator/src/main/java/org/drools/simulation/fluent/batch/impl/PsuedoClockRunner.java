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

import org.drools.core.command.ConversationManager;
import org.drools.core.command.RequestContextImpl;
import org.drools.core.command.impl.ContextImpl;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.world.impl.ContextManagerImpl;
import org.drools.simulation.fluent.batch.Batch;
import org.kie.api.command.Command;
import org.kie.internal.command.Context;
import org.kie.internal.command.ContextManager;

import java.util.*;

public class PsuedoClockRunner {
    private Map<String, Context>  appContexts;

    private long                   counter;

    //private PriorityQueue<Step>           queue;

    public PsuedoClockRunner() {
        appContexts = new HashMap<String, Context>();

    }

    public Map<String, Context> getAppContexts() {
        return appContexts;
    }

    public Context execute(List<? extends Batch> batches) {
        Map<String, Context>  requestContexts = new HashMap<String, Context>();
        if ( batches.get(0) instanceof AfterBatchCommand ) {
            List<AfterBatchCommand> clone = new ArrayList<AfterBatchCommand>();
            for (Batch batch : batches) {
                clone.add((AfterBatchCommand)batch);
            }
            Collections.sort(clone, BatchSorter.instance);
            batches = clone;
        }

        ContextManager ctxManager = new ContextManagerImpl(appContexts);
        ConversationManager cvnManager = new ConversationManager();

        RequestContextImpl requestCtx = new RequestContextImpl(counter++,
                                                               ctxManager,
                                                               cvnManager);

        for (Batch batch : batches) {
            for (Command cmd : batch.getCommands() ) {
                Object returned = ((GenericCommand)cmd).execute(requestCtx);
                if ( returned != null ) {
                    requestCtx.setLastReturned(returned);
                }
            }
        }

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

//        Map<String, Map<String, Object>> results = new HashMap<String, Map<String, Object>>();
//        for ( Step s = queue.remove(); !queue.isEmpty(); s = queue.remove() ) {
//            Context ctx = s.getApplicationContext();
//            Batch b = s.getBatch();
//            for (Command cmd : b.getCommands() ) {
//                ((GenericCommand)cmd).execute(ctx);
//            }
//        }

        return requestCtx;

    }



    public static class BatchSorter implements Comparator<AfterBatchCommand> {
        public static BatchSorter instance = new BatchSorter();


        @Override
        public int compare(AfterBatchCommand o1, AfterBatchCommand o2) {
            if(o1.getDistance() > o2.getDistance()) {
                return 1;
            }
            else if(o1.getDistance() < o2.getDistance()) {
                return -1;
            }

            return 0;
        }

    }
}
