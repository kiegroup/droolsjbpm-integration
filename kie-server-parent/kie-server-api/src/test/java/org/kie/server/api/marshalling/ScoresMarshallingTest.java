/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.model.instance.ScoreWrapper;
import org.kie.server.api.model.instance.SolverInstance;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@RunWith(Parameterized.class)
public class ScoresMarshallingTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB},
                                {MarshallingFormat.JSON},
                                {MarshallingFormat.XSTREAM}
                        }
        ));

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MarshallingFormat marshallingFormat;

    private Marshaller marshaller;

    @Before
    public void setUp() {
        marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void testMarshallHardSoftScore() {
        HardSoftScore score = HardSoftScore.valueOf(10, 20);
        HardSoftScore result = marshallUnmarshallScore(score);

        assertNotNull(result);
        assertEquals(10, result.getHardScore());
        assertEquals(20, result.getSoftScore());
    }

    @SuppressWarnings("unchecked")
    private <S extends Score<?>> S marshallUnmarshallScore(S toBeMarshalled) {
        ScoreWrapper wrapper = new ScoreWrapper(toBeMarshalled);

        SolverInstance instance = new SolverInstance();
        instance.setScoreWrapper(wrapper);

        String marshalledSolver = marshaller.marshall(instance);
        SolverInstance result = marshaller.unmarshall(marshalledSolver, SolverInstance.class);

        return (S) result.getScoreWrapper().toScore();
    }
}
