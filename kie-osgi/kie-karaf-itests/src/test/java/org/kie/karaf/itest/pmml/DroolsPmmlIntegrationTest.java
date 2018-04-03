/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest.pmml;

import static java.util.Collections.singletonList;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class DroolsPmmlIntegrationTest extends AbstractPmmlIntegrationTest {

    /*
     * Unfortunately using @Ignore doesn't work with pax-exam. So we need to
     * re-declare the method here with @Test. Once kie-pmml is functional and
     * can do testing, we should push this method up again to
     * AbstractPmmlIntegrationTest and remove it here.
     */
    @Test
    @Override
    public void testExecute1() throws Exception {
        super.testExecute1();
    }

    @Override
    protected List<String> getKiePmmlFeatures() {
        return singletonList("drools-pmml");
    }
}
