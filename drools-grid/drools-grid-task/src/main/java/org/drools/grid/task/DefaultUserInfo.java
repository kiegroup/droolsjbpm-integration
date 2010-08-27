package org.drools.grid.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.task.Group;
import org.drools.task.OrganizationalEntity;
import org.drools.task.UserInfo;

public class DefaultUserInfo
    implements
    UserInfo {

    private Map<Group, List<OrganizationalEntity>> groups       = new HashMap<Group, List<OrganizationalEntity>>();

    private Map<OrganizationalEntity, String>      emails       = new HashMap<OrganizationalEntity, String>();

    private Map<OrganizationalEntity, String>      languages    = new HashMap<OrganizationalEntity, String>();

    private Map<OrganizationalEntity, String>      displayNames = new HashMap<OrganizationalEntity, String>();

    public Map<Group, List<OrganizationalEntity>> getGroups() {
        return this.groups;
    }

    public void setGroups(Map<Group, List<OrganizationalEntity>> groups) {
        this.groups = groups;
    }

    public Map<OrganizationalEntity, String> getEmails() {
        return this.emails;
    }

    public void setEmails(Map<OrganizationalEntity, String> emails) {
        this.emails = emails;
    }

    public String getEmailForEntity(OrganizationalEntity entity) {
        return this.emails.get( entity );
    }

    public Map<OrganizationalEntity, String> getDisplayNames() {
        return this.displayNames;
    }

    public void setDisplayNames(Map<OrganizationalEntity, String> displayNames) {
        this.displayNames = displayNames;
    }

    public Map<OrganizationalEntity, String> getLanguages() {
        return this.languages;
    }

    public void setLanguages(Map<OrganizationalEntity, String> languages) {
        this.languages = languages;
    }

    public Iterator<OrganizationalEntity> getMembersForGroup(Group group) {
        return this.groups.get( group ).iterator();
    }

    public boolean hasEmail(Group group) {
        return this.emails.containsKey( group );
    }

    public String getDisplayName(OrganizationalEntity entity) {
        String displayName = this.displayNames.get( entity );
        return (displayName != null) ? displayName : entity.getId();
    }

    public String getLanguageForEntity(OrganizationalEntity entity) {
        return this.languages.get( entity );
    }

}
