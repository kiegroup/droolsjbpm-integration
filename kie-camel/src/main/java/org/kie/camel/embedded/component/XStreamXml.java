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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.model.dataformat.XStreamDataFormat;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.runtime.help.impl.XStreamXML.AbortWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamXML.BatchExecutionResultConverter;
import org.drools.core.runtime.help.impl.XStreamXML.CompleteWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamXML.DeleteConverter;
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
import org.drools.core.runtime.help.impl.XStreamXML.SetGlobalConverter;
import org.drools.core.runtime.help.impl.XStreamXML.SignalEventConverter;
import org.drools.core.runtime.help.impl.XStreamXML.StartProcessConvert;

public class XStreamXml {

    public static XStreamDataFormat newXStreamMarshaller(XStreamDataFormat xstreamDataFormat) {
        XStreamHelper.setAliases(xstreamDataFormat);
        // For Camel 3.x we rely on the configured aliases and default XStream settings
        return xstreamDataFormat;
    }

}
