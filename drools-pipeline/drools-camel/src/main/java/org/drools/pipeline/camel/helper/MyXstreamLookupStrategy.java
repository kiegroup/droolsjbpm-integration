/*
 *  Copyright 2009 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.pipeline.camel.helper;

import com.thoughtworks.xstream.XStream;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;

/**
 *
 * @author salaboy
 */
public class MyXstreamLookupStrategy implements XStreamResolverStrategy {

    public MyXstreamLookupStrategy() {
    }

    public XStream lookup(String name) {
        return BatchExecutionHelper.newXStreamMarshaller();
    }
}
