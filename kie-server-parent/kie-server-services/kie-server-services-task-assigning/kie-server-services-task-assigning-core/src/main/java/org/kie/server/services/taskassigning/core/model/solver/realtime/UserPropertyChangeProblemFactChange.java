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

package org.kie.server.services.taskassigning.core.model.solver.realtime;

import java.util.Map;
import java.util.Set;

import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.kie.server.services.taskassigning.core.model.solver.realtime.ProblemFactChangeUtil.releaseNonPinnedTasks;

public class UserPropertyChangeProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private User user;

    private boolean enabled;

    private Map<String, Object> attributes;

    private Map<String, Set<Object>> labelValues;

    private Set<Group> groups;

    public UserPropertyChangeProblemFactChange(User user, boolean enabled, Map<String, Object> attributes,
                                               Map<String, Set<Object>> labelValues, Set<Group> groups) {
        this.user = user;
        this.enabled = enabled;
        this.attributes = attributes;
        this.labelValues = labelValues;
        this.groups = groups;
    }

    public User getUser() {
        return user;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Map<String, Set<Object>> getLabelValues() {
        return labelValues;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        final User workingUser = scoreDirector.lookUpWorkingObjectOrReturnNull(user);
        if (workingUser == null) {
            return;
        }
        scoreDirector.beforeProblemPropertyChanged(workingUser);
        workingUser.setEnabled(enabled);
        workingUser.setAllLabelValues(labelValues);
        workingUser.setAttributes(attributes);
        workingUser.setGroups(groups);
        scoreDirector.afterProblemPropertyChanged(workingUser);
        releaseNonPinnedTasks(workingUser, scoreDirector);
        scoreDirector.triggerVariableListeners();
    }
}
