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
import java.util.List;

import org.apache.camel.model.dataformat.XStreamDataFormat;
import org.drools.core.runtime.help.impl.CommandsObjectContainer;
import org.drools.core.runtime.help.impl.ObjectsObjectContainer;
import org.drools.core.runtime.help.impl.ParameterContainer;
import org.drools.core.runtime.help.impl.RowItemContainer;
import org.drools.core.runtime.help.impl.WorkItemResultsContainer;
import org.drools.core.runtime.help.impl.XStreamJSon.CommandsContainerConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonAbortWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonBatchExecutionCommandConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonBatchExecutionResultConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonCompleteWorkItemConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonFactHandleConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonFireAllRulesConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonGetGlobalConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonGetObjectConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonGetObjectsConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonInsertConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonInsertElementsConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonModifyConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonQueryConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonQueryResultsConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonRetractConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonSetGlobalConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonSignalEventConverter;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonStartProcessConvert;
import org.drools.core.runtime.help.impl.XStreamJSon.RowItemConverter;

public class XStreamJson {
        public static XStreamDataFormat newJSonMarshaller(XStreamDataFormat xstreamDataFormat) {
        xstreamDataFormat.setDriver("json");
        XStreamHelper.setAliases(xstreamDataFormat);
        // For Camel 3.x we rely on aliases and the default XStream JSON configuration
        return xstreamDataFormat;
    }


}
