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

import org.drools.runtime.pipeline.Join;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Splitter;

public class IterateSplitter extends BaseEmitter
    implements
    Splitter {
    
    private Join join;

    public void receive(Object object,
                       PipelineContext context) {
        if ( object instanceof Iterable ) {
            for ( Object result : ((Iterable) object) ) {
                emit( result,
                      context );
            }
        } else {
            emit( object,
                  context );
        }
        
        if ( this.join != null ) {
            this.join.completed( context);
        }
    }

    public void setJoin(Join join) {
        this.join = join;
    }

}
