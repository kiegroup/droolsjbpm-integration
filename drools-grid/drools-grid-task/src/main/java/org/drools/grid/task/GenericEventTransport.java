/**
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

package org.drools.grid.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.drools.eventmessaging.EventTriggerTransport;
import org.drools.eventmessaging.Payload;
import org.drools.grid.generic.GenericIoWriter;
import org.drools.grid.generic.Message;
import org.drools.task.service.Command;
import org.drools.task.service.CommandName;

public class GenericEventTransport implements EventTriggerTransport {
    private String uuid;
    private Map<String, GenericIoWriter> sessions;
    private int responseId;
    private boolean remove;
    
    GenericEventTransport(String uuid, int responseId, Map<String, GenericIoWriter> sessions, boolean remove) {
        this.uuid = uuid;
        this.responseId = responseId;
        this.sessions = sessions;
        this.remove = remove;
    }

    public void trigger(Payload payload) {        
        GenericIoWriter session = sessions.get( uuid );
        List args = new ArrayList( 1 );
        args.add( payload );
        Command resultsCmnd = new Command( responseId, CommandName.EventTriggerResponse, args);
        session.write(new Message(Integer.parseInt(uuid),
                                        responseId,
                                        false,
                                        resultsCmnd ), null);
    }
    
    public boolean isRemove() {
        return this.remove;
    }
}
