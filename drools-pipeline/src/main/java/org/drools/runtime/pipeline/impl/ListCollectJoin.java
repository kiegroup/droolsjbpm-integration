/**
 * Copyright 2010 JBoss Inc
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

package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.drools.runtime.pipeline.Join;
import org.drools.runtime.pipeline.PipelineContext;

public class ListCollectJoin extends BaseEmitter implements Join {
    private Map<PipelineContext, List> lists;
    
    public ListCollectJoin() {
        lists = new IdentityHashMap<PipelineContext, List>();
    }
    
    public void receive(Object object,
                        PipelineContext context) {
        List list = lists.get( context );
        if ( list == null ) {
            list = new ArrayList();
            lists.put( context, list );
        }
        list.add( object );
    }
    
    public void completed(PipelineContext context) {
        emit( lists.remove( context ), context);
    }    
}
