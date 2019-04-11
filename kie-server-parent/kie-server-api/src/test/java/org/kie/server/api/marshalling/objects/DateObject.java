/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.kie.soup.commons.xstream.LocalDateTimeXStreamConverter;
import org.kie.soup.commons.xstream.LocalDateXStreamConverter;
import org.kie.soup.commons.xstream.LocalTimeXStreamConverter;
import org.kie.soup.commons.xstream.OffsetDateTimeXStreamConverter;

/**
 * TODO Remove @XStreamConverter for java.time attributes once converters are provided by XStream out of the box.
 * Maybe keep this for backward compatibility (KIE Soup converters should keep working even if they're no longer needed)
 * and use {@link DateObjectUnannotated} when testing out-of-the-box time (un)marshalling.
 * @see <a href="https://github.com/x-stream/xstream/issues/75">XStream#75</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "date-object")
@XStreamAlias("date-object")
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

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }
}
