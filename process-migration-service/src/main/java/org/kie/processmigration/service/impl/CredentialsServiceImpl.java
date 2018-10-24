/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.kie.processmigration.model.Credentials;
import org.kie.processmigration.service.CredentialsService;

@ApplicationScoped
public class CredentialsServiceImpl implements CredentialsService {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Credentials get(Long id) {
        try {
            return em.createNamedQuery("Credentials.findByMigrationId", Credentials.class)
                     .setParameter("id", id)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public Credentials save(Credentials credentials) {
        em.persist(credentials);
        return credentials;
    }

    @Override
    @Transactional
    public Credentials delete(Long id) {
        Credentials cred = get(id);
        if (cred != null) {
            em.remove(cred);
            return cred;
        }
        return null;
    }

}
