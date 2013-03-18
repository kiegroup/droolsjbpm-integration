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
package org.drools.grid.remote.command;

import java.io.Serializable;
import java.util.Iterator;

import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

/**
 *
 * @author salaboy
 */
public class QueryResultsDescriptor implements QueryResults, Serializable{
    private String queryName;
    private String queryId;
    private int size;

    public QueryResultsDescriptor(String queryName, String queryId, int size) {
        this.queryName = queryName;
        this.queryId = queryId;
        this.size = size;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String[] getIdentifiers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<QueryResultsRow> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int size() {
        return this.size;
    }
    
}
