/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.router.identity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.PasswordCredential;
import org.jboss.logging.Logger;

import static org.kie.server.router.KieServerRouterConstants.ROUTER_IDENTITY_FILE;

public class FileStoredIdentityManager implements IdentityService {

    protected static final Logger log = Logger.getLogger(FileStoredIdentityManager.class);

    private static final String IDENTITY_FILE = System.getProperty(ROUTER_IDENTITY_FILE, "users.properties");
    private File userFile;

    public FileStoredIdentityManager() {
        userFile = new File(IDENTITY_FILE);
        log.infof("Using <%1$s> as user repository", userFile.getAbsolutePath());
    }

    @Override
    public Account verify(Account account) {
        return account;
    }


    @Override
    public Account verify(Credential credential) {
        return null;
    }


    @Override
    public Account verify(String id, Credential credential) {
        if (!(credential instanceof PasswordCredential)) {
            return null;
        }
        PasswordCredential passwordCredential = (PasswordCredential) credential;
        KieServerInstanceAccount account = getAccount(id);
        String challenge = HashUtil.hash(id, new String(passwordCredential.getPassword()));
        if (account != null && challenge.equals(account.getHash())) {
            log.infof("Authentication succesfully for %1$s", id);
            return account;
        }
        log.infof("Authentication failed for %1$s", id);
        return null;
    }

    private KieServerInstanceAccount getAccount(String id) {
        Properties file = loadKieServerInstanceData();
        if (!file.containsKey(id)) {
            return null;
        }
        return new KieServerInstanceAccount(id, file.getProperty(id));
    }

    @Override
    public void addKieServerInstance(String kieServerInstanceId, String password) {
        Properties data = loadKieServerInstanceData();
        data.put(kieServerInstanceId, HashUtil.hash(kieServerInstanceId, password));
        saveKieServerInstanceData(data);
    }

    @Override
    public void removeKieServerInstance(String kieServerInstanceId) {
        Properties data = loadKieServerInstanceData();
        data.remove(kieServerInstanceId);
        saveKieServerInstanceData(data);
    }

    private Properties loadKieServerInstanceData() {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream(userFile)) {
            properties.load(is);
        } catch (IOException e) {
            log.errorf(e, "An error ocurred. File <%1$s> is a directory, it does not exist or it cannot be opened", userFile);
        }
        return properties;
    }

    private void saveKieServerInstanceData(Properties data) {
        try (OutputStream out = new FileOutputStream(userFile)) {
            data.store(out, "Kie Server Router");
        } catch (IOException e) {
            log.errorf(e, "An error ocurred. File <%1$s> is a directory, it cannot be created or it cannot be opened", userFile);
        }
    }

    @Override
    public String id() {
        return "default";
    }
}
