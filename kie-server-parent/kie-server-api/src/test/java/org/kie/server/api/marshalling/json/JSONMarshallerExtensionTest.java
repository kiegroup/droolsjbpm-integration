package org.kie.server.api.marshalling.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

import static org.junit.Assert.*;

public class JSONMarshallerExtensionTest {
    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    public void testCustomExtensionMarshaller() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        Calendar calendar = GregorianCalendar.getInstance();
        
        String marshall = marshaller.marshall(calendar);
        assertEquals(marshall, "\""+ FORMATTER.format(calendar.getTime()) +"\"" );
        
        GregorianCalendar unmarshall = marshaller.unmarshall(marshall, GregorianCalendar.class);
        assertEquals(unmarshall, calendar);
    }
    
}
