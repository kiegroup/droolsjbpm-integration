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

import java.util.Map;

import org.kie.server.services.taskassigning.user.system.api.User;

import static org.kie.server.services.taskassigning.core.model.DefaultLabels.SKILLS;

public class DefaultUserSkillsValueExtractor extends AbstractStringListValueAttributeMapValueExtractor<Map<String, Object>, User> {

    static final String USER_SKILLS_ATTRIBUTE = "skills";

    /**
     * Property for configuring the name of the external user attribute from where the skills will be extracted.
     */
    public static final String USER_SKILLS_ATTRIBUTE_PROPERTY_NAME = DefaultUserSkillsValueExtractor.class.getName() + "." + USER_SKILLS_ATTRIBUTE;

    public DefaultUserSkillsValueExtractor() {
        super(System.getProperty(USER_SKILLS_ATTRIBUTE_PROPERTY_NAME, USER_SKILLS_ATTRIBUTE),
              COMMA_SEPARATOR, User.class, SKILLS.name(), 1);
    }

    @Override
    protected Map<String, Object> getAttributes(User source) {
        return source.getAttributes();
    }
}
