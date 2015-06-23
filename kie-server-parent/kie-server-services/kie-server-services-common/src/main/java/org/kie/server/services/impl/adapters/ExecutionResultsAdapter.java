/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.services.impl.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.runtime.ExecutionResults;


public class ExecutionResultsAdapter extends XmlAdapter<ExecutionResultImpl, ExecutionResults>{

    @Override
    public ExecutionResults unmarshal(ExecutionResultImpl v) throws Exception {
        return v;
    }

    @Override
    public ExecutionResultImpl marshal(ExecutionResults v) throws Exception {
        return (ExecutionResultImpl)v;
    }

}
