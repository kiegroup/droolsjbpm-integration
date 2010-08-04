/*
 *  Copyright 2010 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.drools.grid;

import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.Message;

/**
 *
 * @author salaboy
 */
public interface GenericHumanTaskConnector extends GenericIoWriter{

    public String getId() throws ConnectorException;

    void connect() throws ConnectorException;

    void disconnect() throws ConnectorException;

    public HumanTaskNodeService getHumanTaskNodeService() throws ConnectorException;

    public GenericConnection getConnection();

    Message write(Message msg) throws ConnectorException;

    public ConnectorType getConnectorType();

}
