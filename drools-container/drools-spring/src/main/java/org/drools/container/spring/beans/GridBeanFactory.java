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


import java.util.HashMap;

import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class GridBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private String id;
    private GridImpl grid;
//    private String type;
//    private GenericConnection connection;
//
    public Object getObject() throws Exception {
        return this.grid;
    }
//
    public Class<Grid> getObjectType() {
        return Grid.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        this.grid = new GridImpl( new HashMap<String, Object>() );
        ((GridImpl)this.grid).addService( WhitePages.class, new WhitePagesImpl() );
        
//        connection = new GridConnection();
//        connection.addExecutionNode(new LocalNodeConnector());
//        connection.addDirectoryNode(new LocalDirectoryConnector());
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

//    public void setType(String type) {
//        this.type = type;
//    }
//
//    public String getType() {
//        return type;
//    }

}
