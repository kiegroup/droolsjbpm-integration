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
public class ReleaseIdFilterTest {
    private static final ReleaseIdFilter FULL_FILTER = new ReleaseIdFilter("org.test", "example-artifactId", "1.0.0.Final");
    private static final ReleaseIdFilter GROUP_ID_ARTIFACT_ID_FILTER = new ReleaseIdFilter.Builder().groupId("org.test").artifactId("example-artifactId").build();
    private static final ReleaseIdFilter GROUP_ID_VERSION_FILTER = new ReleaseIdFilter.Builder().groupId("org.test").version("1.0.0.Final").build();
    private static final ReleaseIdFilter ARTIFACT_ID_VERSION_FILTER = new ReleaseIdFilter.Builder().artifactId("example-artifactId").version("1.0.0.Final").build();
    private static final ReleaseIdFilter GROUP_ID_FILTER = new ReleaseIdFilter.Builder().groupId("org.test").build();
    private static final ReleaseIdFilter ARTIFACT_ID_FILTER = new ReleaseIdFilter.Builder().artifactId("example-artifactId").build();
    private static final ReleaseIdFilter VERSION_FILTER = new ReleaseIdFilter.Builder().version("1.0.0.Final").build();

    private static final ReleaseId MATCHING_RELEASE_ID = new ReleaseId("org.test", "example-artifactId", "1.0.0.Final");
    private static final ReleaseId NON_MATCHING_RELEASE_ID = new ReleaseId("foo", "bar", "baz");

    @Parameterized.Parameters(name = "{index}: filter={0}; releaseId={1}; expecting to match={2}")
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                {
                        {FULL_FILTER, MATCHING_RELEASE_ID, true},
                        {FULL_FILTER, NON_MATCHING_RELEASE_ID, false},

                        {GROUP_ID_ARTIFACT_ID_FILTER, MATCHING_RELEASE_ID, true},
                        {GROUP_ID_ARTIFACT_ID_FILTER, NON_MATCHING_RELEASE_ID, false},
                        {GROUP_ID_VERSION_FILTER, MATCHING_RELEASE_ID, true},
                        {GROUP_ID_VERSION_FILTER, NON_MATCHING_RELEASE_ID, false},

                        {ARTIFACT_ID_VERSION_FILTER, MATCHING_RELEASE_ID, true},
                        {ARTIFACT_ID_VERSION_FILTER, NON_MATCHING_RELEASE_ID, false},
                        {GROUP_ID_FILTER, MATCHING_RELEASE_ID, true},
                        {GROUP_ID_FILTER, NON_MATCHING_RELEASE_ID, false},
                        {ARTIFACT_ID_FILTER, MATCHING_RELEASE_ID, true},
                        {ARTIFACT_ID_FILTER, NON_MATCHING_RELEASE_ID, false},
                        {VERSION_FILTER, MATCHING_RELEASE_ID, true},
                        {VERSION_FILTER, NON_MATCHING_RELEASE_ID, false}
                }
        ));

        return data;
    }

    @Parameterized.Parameter(0)
    public ReleaseIdFilter filter;

    @Parameterized.Parameter(1)
    public ReleaseId releaseId;

    @Parameterized.Parameter(2)
    public boolean expectedResult;

    @Test
    public void testAccept() {
        boolean result = filter.accept(releaseId);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
