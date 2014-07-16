/*
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

package org.kie.server.services.impl;

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.AbortWorkItemCommand;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.ModifyCommand;
import org.drools.core.command.runtime.rule.ModifyCommand.SetterImpl;
import org.drools.core.command.runtime.rule.QueryCommand;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.runtime.help.impl.XStreamXML.AbortWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamXML.BatchExecutionResultConverter;
import org.drools.core.runtime.help.impl.XStreamXML.CompleteWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamXML.FactHandleConverter;
import org.drools.core.runtime.help.impl.XStreamXML.FireAllRulesConverter;
import org.drools.core.runtime.help.impl.XStreamXML.GetGlobalConverter;
import org.drools.core.runtime.help.impl.XStreamXML.GetObjectConverter;
import org.drools.core.runtime.help.impl.XStreamXML.GetObjectsConverter;
import org.drools.core.runtime.help.impl.XStreamXML.InsertConverter;
import org.drools.core.runtime.help.impl.XStreamXML.InsertElementsConverter;
import org.drools.core.runtime.help.impl.XStreamXML.ModifyConverter;
import org.drools.core.runtime.help.impl.XStreamXML.QueryConverter;
import org.drools.core.runtime.help.impl.XStreamXML.QueryResultsConverter;
import org.drools.core.runtime.help.impl.XStreamXML.RetractConverter;
import org.drools.core.runtime.help.impl.XStreamXML.SetGlobalConverter;
import org.drools.core.runtime.help.impl.XStreamXML.SignalEventConverter;
import org.drools.core.runtime.help.impl.XStreamXML.StartProcessConvert;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.drools.core.runtime.rule.impl.FlatQueryResults;

import com.thoughtworks.xstream.XStream;

public class XStreamXml {

    public static XStream newXStreamMarshaller(ClassLoader classLoader) {
        XStream xstream = new XStream();
        xstream.setClassLoader(classLoader);
        setAliases(xstream);

        xstream.addImplicitCollection(BatchExecutionCommandImpl.class, "commands");

        registerConverters(xstream);

        return xstream;
    }

    private static void registerConverters(XStream xstream) {
        xstream.registerConverter(new InsertConverter(xstream));
        xstream.registerConverter(new RetractConverter(xstream));
        xstream.registerConverter(new ModifyConverter(xstream));
        xstream.registerConverter(new GetObjectConverter(xstream));
        xstream.registerConverter(new InsertElementsConverter(xstream));
        xstream.registerConverter(new FireAllRulesConverter(xstream));
        xstream.registerConverter(new StartProcessConvert(xstream));
        xstream.registerConverter(new SignalEventConverter(xstream));
        xstream.registerConverter(new CompleteWorkItemConverter(xstream));
        xstream.registerConverter(new AbortWorkItemConverter(xstream));
        xstream.registerConverter(new QueryConverter(xstream));
        xstream.registerConverter(new SetGlobalConverter(xstream));
        xstream.registerConverter(new GetGlobalConverter(xstream));
        xstream.registerConverter(new GetObjectsConverter(xstream));
        xstream.registerConverter(new BatchExecutionResultConverter(xstream));
        xstream.registerConverter(new QueryResultsConverter(xstream));
        xstream.registerConverter(new FactHandleConverter(xstream));
    }

    public static void setAliases(XStream xstream) {
        xstream.alias("batch-execution",
                BatchExecutionCommandImpl.class);
        xstream.alias("insert",
                InsertObjectCommand.class);
        xstream.alias("modify",
                ModifyCommand.class);
        xstream.alias("setters",
                SetterImpl.class);
        xstream.alias("retract",
                DeleteCommand.class);
        xstream.alias("insert-elements",
                InsertElementsCommand.class);
        xstream.alias("start-process",
                StartProcessCommand.class);
        xstream.alias("signal-event",
                SignalEventCommand.class);
        xstream.alias("complete-work-item",
                CompleteWorkItemCommand.class);
        xstream.alias("abort-work-item",
                AbortWorkItemCommand.class);
        xstream.alias("set-global",
                SetGlobalCommand.class);
        xstream.alias("get-global",
                GetGlobalCommand.class);
        xstream.alias("get-object",
                GetObjectCommand.class);
        xstream.alias("get-objects",
                GetObjectsCommand.class);
        xstream.alias("execution-results",
                ExecutionResultImpl.class);
        xstream.alias("fire-all-rules",
                FireAllRulesCommand.class);
        xstream.alias("query",
                QueryCommand.class);
        xstream.alias("query-results",
                FlatQueryResults.class);
        xstream.alias("fact-handle",
                DefaultFactHandle.class);
    }

}
