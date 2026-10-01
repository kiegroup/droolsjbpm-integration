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

import org.kie.internal.runtime.helper.BatchExecutionHelper;

public class XStreamXml {

    /**
     * Configures {@code xstreamDataFormat} by delegating to
     * {@link BatchExecutionHelper#newXStreamMarshaller()} which creates an {@link com.thoughtworks.xstream.XStream}
     * with all Drools XML converters pre-registered (InsertConverter, ModifyConverter, etc.).
     * Any pending custom converters already queued on {@code xstreamDataFormat} are flushed
     * once {@link DroolsXStreamDataFormat#setXstream} is called.
     */
    public static DroolsXStreamDataFormat newXStreamMarshaller(DroolsXStreamDataFormat xstreamDataFormat) {
        xstreamDataFormat.setXstream(BatchExecutionHelper.newXStreamMarshaller());
        return xstreamDataFormat;
    }

}
