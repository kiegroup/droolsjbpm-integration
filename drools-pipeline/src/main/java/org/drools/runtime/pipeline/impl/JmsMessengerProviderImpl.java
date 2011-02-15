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

package org.drools.runtime.pipeline.impl;

import java.util.Properties;

import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.JmsMessengerProvider;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.pipeline.Service;

public class JmsMessengerProviderImpl
    implements
    JmsMessengerProvider {
    public Service newJmsMessenger(Pipeline pipeline,
                                   Properties properties,
                                   String destinationName,
                                   ResultHandlerFactory resultHandlerFactory) {
        return new JmsMessenger( pipeline,
                                 properties,
                                 destinationName,
                                 resultHandlerFactory );
    }

    public Action newJmsUnwrapMessageObject() {
        return new JmsUnwrapMessageObject();
    }

}
