/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.objects.DateObjectUnannotated;
import org.kie.server.api.model.instance.SolverInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests (un)marshalling of {@link SolverInstance} with best solution that has fields from {@link java.time} package
 * without using custom adapters/converters.
 */
@RunWith(Parameterized.class)
public class SolutionMarshallingTest {

    private static final Logger logger = LoggerFactory.getLogger(SolutionMarshallingTest.class);

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return new ArrayList<>(Arrays.asList(new Object[][]{
                // JAXB doesn't seem to handle java.time.* without using custom adapters
                /*{MarshallingFormat.JAXB},*/
                {MarshallingFormat.JSON},
                {MarshallingFormat.XSTREAM}
        }));
    }

    @Parameterized.Parameter
    public MarshallingFormat marshallingFormat;

    @Test
    public void testMarshallHardSoftScore() {
        SolverInstance solverInstance = new SolverInstance();

        DateObjectUnannotated dateObject = new DateObjectUnannotated();
        dateObject.setLocalDate(LocalDate.of(2018, 8, 18));
        dateObject.setLocalDateTime(LocalDateTime.of(2017, 7, 17, 17, 17, 17));
        dateObject.setLocalTime(LocalTime.of(12, 34, 56));
        dateObject.setOffsetDateTime(OffsetDateTime.of(2019, 2, 4, 20, 57, 11, 0, ZoneOffset.ofHours(1)));

        solverInstance.setBestSolution(dateObject);

        Marshaller marshaller = MarshallerFactory.getMarshaller(
                marshallingFormat,
                Thread.currentThread().getContextClassLoader()
        );

        String marshalledSolverInstance = marshaller.marshall(solverInstance);
        logger.info("Marshalled SolverInstance ({}): {}", marshallingFormat, marshalledSolverInstance);
        assertThat(marshalledSolverInstance)
                .as("Dates should be formatted")
                .contains(
                        "2018-08-18",
                        "2017-07-17T17:17:17",
                        "12:34:56",
                        "2019-02-04T20:57:11+01");

        SolverInstance unmarshalledSolverInstance = marshaller.unmarshall(marshalledSolverInstance, SolverInstance.class);
        DateObjectUnannotated bestSolution = (DateObjectUnannotated) unmarshalledSolverInstance.getBestSolution();
        assertThat(bestSolution.getLocalDate()).isEqualTo(dateObject.getLocalDate());
        assertThat(bestSolution.getLocalDateTime()).isEqualTo(dateObject.getLocalDateTime());
        assertThat(bestSolution.getLocalTime()).isEqualTo(dateObject.getLocalTime());
        assertThat(bestSolution.getOffsetDateTime()).isEqualTo(dateObject.getOffsetDateTime());
    }
}
