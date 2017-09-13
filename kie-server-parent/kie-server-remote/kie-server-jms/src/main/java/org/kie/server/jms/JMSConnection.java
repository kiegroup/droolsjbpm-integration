/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.jms;

import javax.jms.Connection;
import javax.jms.Session;

/*
 * Simple class to hold the values for a joint Connection and Session
 */
public class JMSConnection {
    private Connection connection;
    private Session session;
    
    public JMSConnection(Connection connection, Session session) {
        this.connection = connection;
        this.session = session;
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }
    
}
