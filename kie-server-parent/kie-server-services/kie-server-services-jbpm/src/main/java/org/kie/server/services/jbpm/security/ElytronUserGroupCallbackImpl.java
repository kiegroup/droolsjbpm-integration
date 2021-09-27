/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.security;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jbpm.services.task.identity.AbstractUserGroupInfo;
import org.kie.api.task.UserGroupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.auth.principal.NamePrincipal;
import org.wildfly.security.auth.realm.FileSystemSecurityRealm;
import org.wildfly.security.auth.server.ModifiableRealmIdentity;
import org.wildfly.security.auth.server.ModifiableRealmIdentityIterator;
import org.wildfly.security.auth.server.NameRewriter;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.authz.Attributes;

public class ElytronUserGroupCallbackImpl
        extends AbstractUserGroupInfo
        implements UserGroupCallback {

    public static final Logger logger = LoggerFactory.getLogger(ElytronUserGroupCallbackImpl.class);

    private static final String FOLDER_PATH = "org.kie.server.services.jbpm.security.filesystemrealm.folder-path";
    private static final String ENCODING = "org.kie.server.services.jbpm.security.filesystemrealm.encoded";
    private static final String LEVELS = "org.kie.server.services.jbpm.security.filesystemrealm.levels";

    public static final String DEFAULT_FILE_SYSTEM_REALM_PATH = System.getProperty("jboss.server.config.dir") + "/kie-fs-realm-users";
    public static final Integer DEFAULT_FILE_SYSTEM_LEVELS = 2;
    public static final Boolean DEFAULT_FILE_SYSTEM_ENCODED = true;

    private final String folderPath;
    private final int levels;
    private final boolean encoded;

    public ElytronUserGroupCallbackImpl() {
        this.folderPath = System.getProperty(FOLDER_PATH,
                                             DEFAULT_FILE_SYSTEM_REALM_PATH);
        levels = getLevels();
        encoded = getEncoded();
    }

    private boolean getEncoded() {
        try {
            return Boolean.valueOf(System.getProperty(ENCODING,
                                                      DEFAULT_FILE_SYSTEM_ENCODED.toString()));
        } catch (NumberFormatException e) {
        }
        return DEFAULT_FILE_SYSTEM_ENCODED;
    }

    private int getLevels() {
        try {
            return Integer.valueOf(System.getProperty(LEVELS,
                                                      DEFAULT_FILE_SYSTEM_LEVELS.toString()));
        } catch (NumberFormatException e) {
        }
        return DEFAULT_FILE_SYSTEM_LEVELS;
    }

    public boolean existsUser(String userId) {
        FileSystemSecurityRealm realm = getRealm();
        ModifiableRealmIdentity identity = realm.getRealmIdentityForUpdate(new NamePrincipal(userId));
        return identity != null;
    }

    public boolean existsGroup(String groupId) {
        try {
            final ModifiableRealmIdentityIterator realmIdentityIterator = getRealm().getRealmIdentityIterator();
            while (realmIdentityIterator.hasNext()) {
                final ModifiableRealmIdentity identity = realmIdentityIterator.next();
                final Attributes attributes = identity.getAttributes();
                final Attributes.Entry roles = attributes.get("role");
                for (String role : roles) {
                    if (Objects.equals(groupId, role)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve a list of existing groups for an user.", this);
        }
        return false;
    }

    public List<String> getGroupsForUser(String userId) {
        List<String> result = new ArrayList<>();

        try {
            FileSystemSecurityRealm realm = getRealm();
            ModifiableRealmIdentity identity = realm.getRealmIdentityForUpdate(new NamePrincipal(userId));

            for (String role : identity.getAttributes().get("role")) {
                result.add(role);
            }
        } catch (RealmUnavailableException e) {
            logger.error("Could not retrieve the File System Security Realm.", this);
        }

        return result;
    }

    private FileSystemSecurityRealm getRealm() {
        return new FileSystemSecurityRealm(Paths.get(folderPath), NameRewriter.IDENTITY_REWRITER, levels, encoded);
    }
}
