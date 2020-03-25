/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.server.services.taskassigning.core.model.DefaultLabels;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.planning.data.DefaultUserAffinitiesValueExtractor.USER_AFFINITIES_ATTRIBUTE;

public class DefaultUserAffinitiesValueExtractorTest extends AbstractStringListValueAttributeMapValueExtractorTest<Map<String, Object>, User, DefaultUserAffinitiesValueExtractor> {

    @Override
    protected DefaultUserAffinitiesValueExtractor createExtractor() {
        return new DefaultUserAffinitiesValueExtractor();
    }

    @Override
    protected Class<User> getExpectedType() {
        return User.class;
    }

    @Override
    protected int getExpectedPriority() {
        return 1;
    }

    @Override
    protected String getExpectedLabelName() {
        return DefaultLabels.AFFINITIES.name();
    }

    @Override
    protected String getExpectedAttributeName() {
        return USER_AFFINITIES_ATTRIBUTE;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{mockUser(USER_AFFINITIES_ATTRIBUTE, String.format("%s", VALUE1)), mockSet(VALUE1)});
        data.add(new Object[]{mockUser(USER_AFFINITIES_ATTRIBUTE, String.format(" %s , ,%s", VALUE1, VALUE2)), mockSet(VALUE2, VALUE1)});
        data.add(new Object[]{mockUser(USER_AFFINITIES_ATTRIBUTE, null), mockSet()});
        return data;
    }

    @Test
    public void getFromPropertyAttributeName() {
        System.setProperty(DefaultUserAffinitiesValueExtractor.USER_AFFINITIES_ATTRIBUTE_PROPERTY_NAME, CUSTOM_NAME);
        assertThat(createExtractor().getAttributeName()).isEqualTo(CUSTOM_NAME);
        System.clearProperty(DefaultUserAffinitiesValueExtractor.USER_AFFINITIES_ATTRIBUTE_PROPERTY_NAME);
    }
}