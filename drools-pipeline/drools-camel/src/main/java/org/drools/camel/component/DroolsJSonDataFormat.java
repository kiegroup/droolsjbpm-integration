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

package org.drools.camel.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.grid.ExecutionNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.ExecutionNodePipelineContextImpl;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author salaboy
 */
public class DroolsJSonDataFormat extends DroolsXStreamDataFormat
    implements
    DataFormat {

    public DroolsJSonDataFormat() {
        xstreamStrategy = new XStreamResolverStrategy() {
            public XStream lookup(String name) {
                return BatchExecutionHelper.newJSonMarshaller();
            }
        };
    }
}