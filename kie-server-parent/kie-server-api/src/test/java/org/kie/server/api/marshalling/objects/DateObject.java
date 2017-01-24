package org.kie.server.api.marshalling.objects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.kie.internal.jaxb.LocalDateTimeXmlAdapter;
import org.kie.internal.jaxb.LocalDateXmlAdapter;
import org.kie.internal.jaxb.LocalTimeXmlAdapter;
import org.kie.internal.jaxb.OffsetDateTimeXmlAdapter;
import org.kie.internal.xstream.LocalDateTimeXStreamConverter;
import org.kie.internal.xstream.LocalDateXStreamConverter;
import org.kie.internal.xstream.LocalTimeXStreamConverter;
import org.kie.internal.xstream.OffsetDateTimeXStreamConverter;

/**
 * TODO Remove @XStreamConverter for java.time attributes once converters are provided by XStream out of the box.
 *
 * @see <a href="https://github.com/x-stream/xstream/issues/75">XStream#75</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "date-object")
@XStreamAlias( "date-object" )
public class DateObject {

    @XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
    @XStreamConverter(LocalDateXStreamConverter.class)
    private LocalDate localDate;

    @XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
    @XStreamConverter(LocalDateTimeXStreamConverter.class)
    private LocalDateTime localDateTime;

    @XmlJavaTypeAdapter(LocalTimeXmlAdapter.class)
    @XStreamConverter(LocalTimeXStreamConverter.class)
    private LocalTime localTime;

    @XmlJavaTypeAdapter(OffsetDateTimeXmlAdapter.class)
    @XStreamConverter(OffsetDateTimeXStreamConverter.class)
    private OffsetDateTime offsetDateTime;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate( LocalDate localDate ) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime( LocalDateTime localDateTime ) {
        this.localDateTime = localDateTime;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime( LocalTime localTime ) {
        this.localTime = localTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime( OffsetDateTime offsetDateTime ) {
        this.offsetDateTime = offsetDateTime;
    }

}
