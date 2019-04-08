/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Unannotated means without KIE Soup custom/temporary converters (see {@link DateObject}).
 * {@code JsonFormat} is OK since it's part of Jackson library and it's needed to enforce keeping of time zone offset.
 */
public class DateObjectUnannotated {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate localDate;

    // Without this, Jackson will serialize the date as a timestamp (e.g. "localDateTime" : [ 2017, 7, 17, 17, 17, 17 ]).
    // The string shape will instruct Jackson (JavaTime module) to format the date in ISO-8601 (default).
    // Option B is to set org.kie.server.json.format.date=true, this will make JSONMarshaller (KIE Server) disable
    // Jackson's feature that serializes dates as timestamps.
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime localDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalTime localTime;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ssZ",
            without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
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
