package org.kie.server.integrationtests.jbpm.util;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.task.UserGroupCallback;

public class FixedUserGroupCallbackImpl implements UserGroupCallback {

    @Override
    public boolean existsUser(String s) {
        return true;
    }

    @Override
    public boolean existsGroup(String s) {
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String s, List<String> list, List<String> list1) {
        ArrayList<String> groups = new ArrayList<String>();

        return groups;
    }
}
