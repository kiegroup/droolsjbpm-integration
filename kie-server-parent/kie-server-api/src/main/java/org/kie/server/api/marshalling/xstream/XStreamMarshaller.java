/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.marshalling.xstream;

import java.util.Set;

import com.thoughtworks.xstream.XStream;
import org.drools.core.runtime.help.impl.XStreamXML;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;

public class XStreamMarshaller
        implements Marshaller {

    private XStream xstream;
    private final ClassLoader classLoader;

    public XStreamMarshaller( final Set<Class<?>> classes, final ClassLoader classLoader ) {
        this.classLoader = classLoader;
        this.xstream = XStreamXML.newXStreamMarshaller( new XStream(  ) );
        this.xstream.setClassLoader( classLoader );

        this.xstream.processAnnotations( CommandScript.class );
        this.xstream.processAnnotations( CallContainerCommand.class );
        this.xstream.processAnnotations( CreateContainerCommand.class );
        this.xstream.processAnnotations( DisposeContainerCommand.class );
        this.xstream.processAnnotations( GetContainerInfoCommand.class );
        this.xstream.processAnnotations( GetScannerInfoCommand.class );
        this.xstream.processAnnotations( UpdateScannerCommand.class );
        this.xstream.processAnnotations( UpdateReleaseIdCommand.class );
        this.xstream.processAnnotations( GetServerInfoCommand.class );
        this.xstream.processAnnotations( ListContainersCommand.class );
        this.xstream.processAnnotations( ServiceResponsesList.class );
        this.xstream.processAnnotations( ServiceResponse.class );
        this.xstream.processAnnotations( KieContainerResourceList.class );
        this.xstream.processAnnotations( KieContainerResource.class );
        this.xstream.processAnnotations( ReleaseId.class );
        this.xstream.processAnnotations( KieContainerStatus.class );
        this.xstream.processAnnotations( KieScannerResource.class );
        this.xstream.processAnnotations( KieServerInfo.class );

        if (classes != null) {
            for (Class<?> clazz : classes) {
                this.xstream.processAnnotations( clazz );
            }
        }
    }

    @Override
    public String marshall(Object objectInput) {
        return xstream.toXML( objectInput );
    }

    @Override
    public <T> T unmarshall(String input, Class<T> type) {
        return (T) xstream.fromXML( input );
    }


    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public MarshallingFormat getFormat() {
        return MarshallingFormat.XSTREAM;
    }

    @Override
    public String toString() {
        return "Marshaller{ XSTREAM }";
    }
}
