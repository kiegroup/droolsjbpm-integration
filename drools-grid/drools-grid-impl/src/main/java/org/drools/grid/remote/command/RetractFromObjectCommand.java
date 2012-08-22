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

package org.drools.grid.remote.command;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.common.DefaultFactHandle;
import org.drools.common.InternalFactHandle;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

@XmlAccessorType(XmlAccessType.NONE)
public class RetractFromObjectCommand
        implements GenericCommand<Object> {

    private Object object;

    public RetractFromObjectCommand() {
    }

    public RetractFromObjectCommand(Object object) {
        this.object = object;
    }

    public Object execute(Context context) {
        StatefulKnowledgeSession ksession = ((KnowledgeCommandContext) context).getStatefulKnowledgesession();
        System.out.println("OBJECT INSIDE THE COMMAND: "+this.object);
        FactHandle handle = ksession.getFactHandle(this.object);
        if ( handle != null ) {
            // objects may not be in the WM (anymore). The remote client is not guaranteed to have up-to-date information
            ksession.getWorkingMemoryEntryPoint( ((InternalFactHandle)handle).getEntryPoint().getEntryPointId() ).retract( handle );
        }
        return null;
    }

    public Object getObject() {
        return this.object;
    }

    public String toString() {
        return "session.retractFromObject( " + object + " );";
    }

}
