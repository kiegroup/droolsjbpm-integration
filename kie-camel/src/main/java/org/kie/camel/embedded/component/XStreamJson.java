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

public class XStreamJson {

    /**
     * Configures {@code xstreamDataFormat} by delegating to
     * {@link BatchExecutionHelper#newJSonMarshaller()} which creates an {@link com.thoughtworks.xstream.XStream}
     * with the Jettison JSON driver and all Drools JSON converters pre-registered.
     * Any pending custom converters already queued on {@code xstreamDataFormat} are flushed
     * once {@link DroolsXStreamDataFormat#setXstream} is called.
     */
    public static DroolsXStreamDataFormat newJSonMarshaller(DroolsXStreamDataFormat xstreamDataFormat) {
        xstreamDataFormat.setJson(true);
        xstreamDataFormat.setXstream(BatchExecutionHelper.newJSonMarshaller());
        return xstreamDataFormat;
    }

}
