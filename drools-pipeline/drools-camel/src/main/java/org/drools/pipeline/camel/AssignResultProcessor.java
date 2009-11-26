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
package org.drools.pipeline.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.impl.AssignObjectAsResult;

/**
 *
 * @author salaboy
 */
public class AssignResultProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        AssignObjectAsResult assignResult = (AssignObjectAsResult) PipelineFactory.newAssignObjectAsResult();
        assignResult.assignResult((PipelineContext) exchange.getProperty("drools-context"), exchange.getIn().getBody());

    }
}
