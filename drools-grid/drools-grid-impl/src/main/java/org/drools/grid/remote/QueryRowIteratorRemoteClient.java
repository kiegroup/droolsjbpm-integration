/*
 * Copyright 2011 JBoss Inc..
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
package org.drools.grid.remote;

import java.util.Iterator;

import org.kie.api.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class QueryRowIteratorRemoteClient implements Iterator<QueryResultsRow>{

    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public QueryResultsRow next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
