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

package org.kie.processmigration.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.kie.processmigration.model.Credentials;
import org.kie.server.client.CredentialsProvider;

public class CredentialsProviderFactoryTest {

    @Test
    public void testNull() {
        String authHeader = null;
        assertNull(CredentialsProviderFactory.getCredentials(authHeader));
    }

    @Test
    public void testBasicNull() {
        String authHeader = "Basic ";
        assertNull(CredentialsProviderFactory.getCredentials(authHeader));
    }

    @Test
    public void testBearerNull() {
        String authHeader = "Bearer ";
        assertNull(CredentialsProviderFactory.getCredentials(authHeader));
    }

    @Test
    public void testBasic() {
        String authHeader = "Basic a2VybWl0OnRoZUZyb2c=";
        Credentials credentials = CredentialsProviderFactory.getCredentials(authHeader);
        assertNotNull(credentials);
        assertEquals("kermit", credentials.getUsername());
        assertEquals("theFrog", credentials.getPassword());
        assertNull(credentials.getToken());
    }

    @Test
    public void testToken() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.";
        String authHeader = "Bearer " + token;
        Credentials credentials = CredentialsProviderFactory.getCredentials(authHeader);
        assertNotNull(credentials);
        assertEquals(token, credentials.getToken());
        assertNull(credentials.getUsername());
        assertNull(credentials.getPassword());
    }

    @Test
    public void testProviderBasic() {
        String authHeader = "Basic a2VybWl0OnRoZUZyb2c=";
        CredentialsProvider provider = CredentialsProviderFactory.getProvider(authHeader);
        assertNotNull(provider);
        assertEquals(authHeader, provider.getAuthorization());
    }

    @Test
    public void testProviderBearer() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.";
        String authHeader = "Bearer " + token;
        CredentialsProvider provider = CredentialsProviderFactory.getProvider(authHeader);
        assertNotNull(provider);
        assertEquals(authHeader, provider.getAuthorization());
    }

    @Test
    public void testProviderBasicCredentials() {
        String authHeader = "Basic a2VybWl0OnRoZUZyb2c=";
        Credentials credentials = new Credentials().setUsername("kermit").setPassword("theFrog");
        CredentialsProvider provider = CredentialsProviderFactory.getProvider(credentials);
        assertNotNull(provider);
        assertEquals(authHeader, provider.getAuthorization());
    }

    @Test
    public void testProviderBearerCredentials() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.";
        String authHeader = "Bearer " + token;
        Credentials credentials = new Credentials().setToken(token);
        CredentialsProvider provider = CredentialsProviderFactory.getProvider(credentials);
        assertNotNull(provider);
        assertEquals(authHeader, provider.getAuthorization());
    }

}
