/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.model.definition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.ItemList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "query-definitions")
public class QueryDefinitionList implements ItemList<QueryDefinition> {

    @XmlElement(name="queries")
    private QueryDefinition[] queries;

    public QueryDefinitionList() {
    }

    public QueryDefinitionList(QueryDefinition[] queries) {
        this.queries = queries;
    }

    public QueryDefinitionList(List<QueryDefinition> queries) {
        this.queries = queries.toArray(new QueryDefinition[queries.size()]);
    }

    public QueryDefinition[] getQueries() {
        return queries;
    }

    public void setQueries(QueryDefinition[] queries) {
        this.queries = queries;
    }

    @Override
    public List<QueryDefinition> getItems() {
        if (queries == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(queries);
    }
}
