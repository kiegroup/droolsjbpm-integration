package org.kie.server.services.marshalling.xstream;

import com.thoughtworks.xstream.XStream;
import org.drools.core.runtime.help.impl.XStreamXML;
import org.kie.server.api.commands.*;
import org.kie.server.services.marshalling.Marshaller;

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
    }

    @Override
    public String marshall(Object objectInput) {
        return xstream.toXML( objectInput );
    }

    @Override
    public Object unmarshall(String serializedInput) {
        return xstream.fromXML( serializedInput );
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
