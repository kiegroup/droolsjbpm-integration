package org.kie.server.api.marshalling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.junit.After;
import org.junit.Test;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.api.marshalling.objects.DateObject;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class JSONMarshallerTest {
    
    @After
    public void cleanup() {
        System.clearProperty("org.kie.server.json.format.date");
    }

    @Test
    public void testMarshallDateObject() {
        String expectedString = String.format("{%n" +
                "  \"localDate\" : \"2017-01-01\",%n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",%n" +
                "  \"localTime\" : \"10:10:10\",%n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+01:00\"%n" +
                "}");

        Marshaller marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );

        DateObject dateObject = new DateObject();
        dateObject.setLocalDate( LocalDate.of( 2017, 1, 1 ) );
        dateObject.setLocalDateTime( LocalDateTime.of( 2017, 1, 1, 10, 10, 10 ) );
        dateObject.setLocalTime( LocalTime.of( 10, 10, 10 ) );
        dateObject.setOffsetDateTime( OffsetDateTime.of( LocalDateTime.of( 2017, 1, 1, 10, 10, 10 ), ZoneOffset.ofHours( 1 ) ) );

        String dateObjectString = marshaller.marshall( dateObject );
        assertNotNull( dateObjectString );

        assertEquals( expectedString, dateObjectString );
    }

    @Test
    public void testUnmarshallDateObject() {
        String expectedString = "{\n" +
                "  \"localDate\" : \"2017-01-01\",\n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",\n" +
                "  \"localTime\" : \"10:10:10\",\n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+01:00\"\n" +
                "}";

        Marshaller marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );

        DateObject dateObject = marshaller.unmarshall( expectedString, DateObject.class );
        assertNotNull( dateObject );

        assertEquals( LocalDate.of( 2017, 1, 1 ), dateObject.getLocalDate() );
        assertEquals( LocalDateTime.of( 2017, 1, 1, 10, 10, 10 ), dateObject.getLocalDateTime() );
        assertEquals( LocalTime.of( 10, 10, 10 ), dateObject.getLocalTime() );
        assertEquals( OffsetDateTime.of( LocalDateTime.of( 2017, 1, 1, 10, 10, 10 ), ZoneOffset.ofHours( 1 ) ), dateObject.getOffsetDateTime() );
    }
    
    public static class Holder {
        private String h;
        
        public String getH() {
            return h;
        }
        
        public void setH(String h) {
            this.h = h;
        }
    }
    
    public static class Ref {
        @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
        @JsonSerialize(using = JSONMarshaller.PassThruSerializer.class)
        private Object r;
        
        public Object getR() {
            return r;
        }
        
        public void setR(Object r) {
            this.r = r;
        }
        
    }
    
    @Test
    public void testRecursiveMap() {
        Map outerMap = new HashMap<>();
        Map innerMap = new HashMap<>();
        Holder holder = new Holder();
        holder.setH("myValueInH");
        
        innerMap.put("level2", holder);
        outerMap.put("level1", innerMap);
        
        Marshaller marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );
        
        Map mu_outerMap = marshaller.unmarshall( marshaller.marshall( outerMap ), Map.class );
        Map mu_innerMap = marshaller.unmarshall( marshaller.marshall( innerMap ), Map.class );
        
        Ref ref = new Ref();
        ref.setR(innerMap);
        
        Ref mu_ref = marshaller.unmarshall( marshaller.marshall( ref ), Ref.class );
        
        assertEquals( "verify that Ref.r is not being serialized with JSONMarshaller.WrappingObjectSerializer, but with the specified one in @JsonSerialize",
                mu_innerMap.entrySet(), ((Map)mu_ref.getR()).entrySet() );
    }
    
    @Test
    public void testMarshallFormatDateObject() throws ParseException {
        System.setProperty("org.kie.server.json.format.date", "true");
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ").parse("2018-01-01T10:00:00.000+0100");
        String expectedString = "\"2018-01-01T10:00:00.000+0100\"";

        Marshaller marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );
        
        String dateObjectString = marshaller.marshall( date );
        assertNotNull( dateObjectString );

        assertEquals( expectedString, dateObjectString );
                
    }

}
