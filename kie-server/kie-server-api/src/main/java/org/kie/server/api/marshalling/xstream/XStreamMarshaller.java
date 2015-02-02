package org.kie.server.api.marshalling.xstream;

import com.thoughtworks.xstream.XStream;
import org.drools.core.runtime.help.impl.XStreamXML;
import org.kie.server.api.commands.*;
import org.kie.server.api.model.*;
import org.kie.server.api.marshalling.Marshaller;

public class XStreamMarshaller
        implements Marshaller {

    private XStream xstream;

    public XStreamMarshaller( final ClassLoader classLoader ) {
        this.xstream = XStreamXML.newXStreamMarshaller( new XStream(  ) );
        this.xstream.setClassLoader( classLoader );

        this.xstream.processAnnotations( CommandScript.class );
        this.xstream.processAnnotations( CallContainerCommand.class );
        this.xstream.processAnnotations( CreateContainerCommand.class );
        this.xstream.processAnnotations( DisposeContainerCommand.class );
        this.xstream.processAnnotations( ListContainersCommand.class );
        this.xstream.processAnnotations( ServiceResponsesList.class );
        this.xstream.processAnnotations( ServiceResponse.class );
        this.xstream.processAnnotations( KieContainerResourceList.class );
        this.xstream.processAnnotations( KieContainerResource.class );
        this.xstream.processAnnotations( ReleaseId.class );
        this.xstream.processAnnotations( KieContainerStatus.class );
        this.xstream.processAnnotations( KieScannerResource.class );
        this.xstream.processAnnotations( KieServerInfo.class );
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

    private void registerKieServerAliases() {
        xstream.alias( "script", CommandScript.class );
        xstream.alias( "list-containers", ListContainersCommand.class );
    }


    @Override
    public String toString() {
        return "Marshaller{ XSTREAM }";
    }
}
