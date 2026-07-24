/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.embedded.component;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.model.dataformat.XStreamDataFormat;
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
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.drools.core.runtime.rule.impl.FlatQueryResults;

public class XStreamHelper {
    public static void setAliases(XStreamDataFormat dataFormat) {
        // Build the alias map we want to apply
        Map<String, String> aliases = new HashMap<String, String>();
        aliases.put("batch-execution", BatchExecutionCommandImpl.class.getName());
        aliases.put("insert", InsertObjectCommand.class.getName());
        aliases.put("modify", ModifyCommand.class.getName());
        aliases.put("setters", SetterImpl.class.getName());
        aliases.put("retract", DeleteCommand.class.getName());
        aliases.put("insert-elements", InsertElementsCommand.class.getName());
        aliases.put("start-process", StartProcessCommand.class.getName());
        aliases.put("signal-event", SignalEventCommand.class.getName());
        aliases.put("complete-work-item", CompleteWorkItemCommand.class.getName());
        aliases.put("abort-work-item", AbortWorkItemCommand.class.getName());
        aliases.put("set-global", SetGlobalCommand.class.getName());
        aliases.put("get-global", GetGlobalCommand.class.getName());
        aliases.put("get-object", GetObjectCommand.class.getName());
        aliases.put("get-objects", GetObjectsCommand.class.getName());
        aliases.put("execution-results", ExecutionResultImpl.class.getName());
        aliases.put("fire-all-rules", FireAllRulesCommand.class.getName());
        aliases.put("query", QueryCommand.class.getName());
        aliases.put("query-results", FlatQueryResults.class.getName());
        aliases.put("fact-handle", DefaultFactHandle.class.getName());

        Class<?> dfClass = dataFormat.getClass();

        // First try Map-based aliases (used by some Camel/XStream implementations)
        try {
            java.lang.reflect.Method getAliases = dfClass.getMethod("getAliases");
            Object current = getAliases.invoke(dataFormat);
            if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>) current;
                if (map == null) {
                    map = new HashMap<String, String>();
                }
                map.putAll(aliases);
                java.lang.reflect.Method setAliases = dfClass.getMethod("setAliases", Map.class);
                setAliases.invoke(dataFormat, map);
                return;
            }
        } catch (NoSuchMethodException e) {
            // ignore and try list-based API
        } catch (Exception e) {
            // ignore and fall through to list-based API
        }

        // Fallback to List<PropertyDefinition>-based aliases used in newer Camel versions
        try {
            java.lang.reflect.Method getAliases = dfClass.getMethod("getAliases");
            Object current = getAliases.invoke(dataFormat);
            @SuppressWarnings({"rawtypes", "unchecked"})
            List list = (current instanceof List) ? (List) current : new ArrayList();

            Class<?> pdClass = Class.forName("org.apache.camel.model.PropertyDefinition");
            java.lang.reflect.Method setKey = pdClass.getMethod("setKey", String.class);
            java.lang.reflect.Method setValue = pdClass.getMethod("setValue", String.class);

            for (Map.Entry<String, String> e : aliases.entrySet()) {
                Object prop = pdClass.getDeclaredConstructor().newInstance();
                setKey.invoke(prop, e.getKey());
                setValue.invoke(prop, e.getValue());
                list.add(prop);
            }

            java.lang.reflect.Method setAliases = dfClass.getMethod("setAliases", List.class);
            setAliases.invoke(dataFormat, list);
        } catch (Exception e) {
            // best-effort only; swallow to remain compatible across Camel versions
        }
    }
}
