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

package org.kie.remote.services.rest.query.data;


/**
 * This class is a "holder" class to store the following information:
 * <ol>
 * <li>The parameter name, for error messages</li>
 * <li>The action, an integer mapping of the parameter (so that we can use a switch statement)</li>
 * <li>The data, or the value passed with the (query parameter)</li>
 * </ol>
 * Some query parameters, such as certain field values (process instance id, potential owner, etc)
 * can be passed multiple times:
 * 
 * <pre>
 * http://.../../rest/query/runtime/process?processinstanceid=2&processinstanceid=3
 * </pre>
 * 
 * When we process these query parameters on the server side, the JAX-RS logic groups all of the values
 * for one parameter into a list or array.
 * </p>
 * This array of values (for the example above <code>[2,3]</code>) is then assigned to the {@link QueryAction#paramData} field.
 */
public class QueryAction {

    public final String paramName;
    public final int action;
    public final String[] paramData;

    public boolean regex = false;
    public boolean min = false;
    public boolean max = false;

    public QueryAction(String param, int action, String[] data) {
        this.paramName = param;
        this.action = action;
        this.paramData = data;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("[" + action + "] " + paramName + ": (");
        if( paramData.length > 0 ) { 
            out.append(paramData[0]);
            for( int i = 1; i < paramData.length; ++i ) { 
                out.append(", ").append(paramData[i]);
            }
        } 
        return out.append(")").toString();
    }
}