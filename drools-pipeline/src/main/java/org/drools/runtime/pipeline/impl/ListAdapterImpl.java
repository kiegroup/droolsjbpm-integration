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

import java.util.List;

import org.drools.runtime.pipeline.ListAdapter;
import org.drools.runtime.pipeline.PipelineContext;

public class ListAdapterImpl extends BaseStage
    implements
    ListAdapter {
    private List list;
    private boolean      syncAccessor;

    public ListAdapterImpl(List list,
                           boolean syncAccessor) {
        super();
        this.list = list;
        this.syncAccessor = syncAccessor;
    }

    public List getList() {
        if ( this.syncAccessor ) {
            synchronized ( this ) {
                return list;
            }
        } else {
            return list;
        }
    }

    public void setList(List list) {
        if ( this.syncAccessor ) {
            synchronized ( this ) {
                this.list = list;
            }
        } else {
            this.list = list;
        }
    }

    public void receive(Object object,
                       PipelineContext context) {
        this.list.add( object );
    }

}
