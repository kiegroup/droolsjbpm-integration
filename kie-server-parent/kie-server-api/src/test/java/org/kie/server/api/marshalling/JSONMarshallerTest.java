package org.kie.server.api.marshalling;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kie.server.api.marshalling.objects.DateObject;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JSONMarshallerTest {

    @Test
    public void testMarshallDateObject() {
        String expectedString = "{\n" +
                "  \"localDate\" : \"2017-01-01\",\n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",\n" +
                "  \"localTime\" : \"10:10:10\",\n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+01:00\"\n" +
                "}";

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

    @Test
    public void testBigDecimal() {
        Marshaller marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );
        
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("a", "b");
        ctx.put("b", new BigDecimal(1234));
        String json = marshaller.marshall( ctx );
        
        System.out.println(json);
        
        Map<String, Object> unmarshall = marshaller.unmarshall(json, Map.class);
        
        assertThat( unmarshall, hasEntry( "a", "b" ) );
        assertThat( unmarshall, hasEntry( "b", BigDecimal.valueOf( 1234 ) ) );
    }
}
