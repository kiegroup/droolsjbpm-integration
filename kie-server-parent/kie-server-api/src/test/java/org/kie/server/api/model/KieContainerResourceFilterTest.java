/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class KieContainerResourceFilterTest {

    private static final String GROUP_ID = "org.example";
    private static final String ARTIFACT_ID = "project";
    private static final String VERSION = "1.0.0.Final";

    private static final ReleaseId RELEASE_ID = new ReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);

    @Test
    public void toURLQueryStringGroupIdOnly() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .releaseId(new ReleaseId(GROUP_ID, null, null)).build();
        Assertions.assertThat(filter.toURLQueryString()).isEqualTo("groupId=" + GROUP_ID);
    }

    @Test
    public void toURLQueryStringArtifactIdOnly() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .releaseId(new ReleaseId(null, ARTIFACT_ID, null)).build();
        Assertions.assertThat(filter.toURLQueryString()).isEqualTo("artifactId=" + ARTIFACT_ID);
    }

    @Test
    public void toURLQueryStringVersionOnly() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .releaseId(new ReleaseId(null, null, VERSION)).build();
        Assertions.assertThat(filter.toURLQueryString()).isEqualTo("version=" + VERSION);
    }

    @Test
    public void toURLQueryStringReleaseIdOnly() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder().releaseId(RELEASE_ID).build();
        Assertions.assertThat(filter.toURLQueryString())
                .isEqualTo("groupId=" + GROUP_ID + "&artifactId=" + ARTIFACT_ID + "&version=" + VERSION);
    }

    @Test
    public void toURLQueryStringSingleStatus() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder().status(KieContainerStatus.STARTED).build();
        Assertions.assertThat(filter.toURLQueryString()).isEqualTo("status=STARTED");
    }

    @Test
    public void toURLQueryStringMultipleStatuses() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .statuses(KieContainerStatus.CREATING, KieContainerStatus.STARTED, KieContainerStatus.FAILED)
                .build();
        Assertions.assertThat(filter.toURLQueryString()).isEqualTo("status=CREATING,STARTED,FAILED");
    }

    @Test
    public void toURLQueryStringReleaseIdWithStatus() {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder()
                .releaseId(RELEASE_ID)
                .status(KieContainerStatus.STARTED)
                .build();
        Assertions.assertThat(filter.toURLQueryString())
                .isEqualTo("groupId=" + GROUP_ID + "&artifactId=" + ARTIFACT_ID + "&version=" + VERSION + "&status=STARTED");
    }
}
