/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring.beans;

import org.drools.grid.ExecutionNode;
import org.drools.grid.generic.GenericConnection;
import org.drools.grid.local.LocalConnection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ExecutionNodeBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private String            id;
    private GenericConnection connection = new LocalConnection();
    private ExecutionNode     node;

    public Object getObject() throws Exception {
        return node;
    }

    public Class< ? extends ExecutionNode> getObjectType() {
        return ExecutionNode.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        node = connection.getExecutionNode();
        node.setId( id );
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setConnection(GenericConnection connection) {
        this.connection = connection;
    }

    public GenericConnection getConnection() {
        return connection;
    }

}
