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

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class KieContainerResourceFilterMatchTest {

    private static final ReleaseId MATCHING_RELEASE_ID = new ReleaseId("org.example","test-artifact", "2.0.0.Final");
    private static final ReleaseId NON_MATCHING_RELEASE_ID = new ReleaseId("foo","bar", "baz");

    private static final KieContainerResourceFilter RELEASE_ID_FILTER =
            new KieContainerResourceFilter.Builder().releaseId(MATCHING_RELEASE_ID).build();

    private static final KieContainerResourceFilter STATUS_FILTER =
            new KieContainerResourceFilter.Builder().status(KieContainerStatus.CREATING).build();

    private static final KieContainerResource MATCHING_CONTAINER = new KieContainerResource("id1", MATCHING_RELEASE_ID, KieContainerStatus.CREATING);
    private static final KieContainerResource NON_MATCHING_CONTAINER = new KieContainerResource("id2", NON_MATCHING_RELEASE_ID, KieContainerStatus.STOPPED);

    @Parameterized.Parameters(name = "{index}: filter={0}; MATCHING_RELEASE_ID={1}; expecting to match={2}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                {
                        {RELEASE_ID_FILTER, MATCHING_CONTAINER, true},
                        {RELEASE_ID_FILTER, NON_MATCHING_CONTAINER, false},
                        {STATUS_FILTER, MATCHING_CONTAINER, true},
                        {STATUS_FILTER, NON_MATCHING_CONTAINER, false}

                }
        ));

        return data;
    }

    @Parameterized.Parameter(0)
    public KieContainerResourceFilter filter;

    @Parameterized.Parameter(1)
    public KieContainerResource containerResource;

    @Parameterized.Parameter(2)
    public boolean expectedResult;

    @Test
    public void testAccept() {
        boolean result = filter.accept(containerResource);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
