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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.io.Resource;
import org.drools.io.impl.ChangeSetImpl;
import org.drools.io.internal.InternalResource;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeAgentBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private KnowledgeBase               kbase;

    private KnowledgeAgent              kagent;

    private String                      id;

    private String                      newInstance;

    private List<DroolsResourceAdapter> resources = Collections.emptyList();

    public Object getObject() throws Exception {
        return this.kagent;
    }

    public Class<KnowledgeAgent> getObjectType() {
        return KnowledgeAgent.class;
    }

    public void afterPropertiesSet() throws Exception {
        KnowledgeAgentConfiguration kagentConf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
        if ( kbase == null ) {
            throw new RuntimeException( "kagent must reference an existing kbase" );
        }

        kagentConf.setProperty( "drools.agent.newInstance",
                                newInstance );

        this.kagent = KnowledgeAgentFactory.newKnowledgeAgent( this.id,
                                                               this.kbase,
                                                               kagentConf );
        
        if ( resources != null && !resources.isEmpty() ) {
            Collection<Resource> rs = new ArrayList<Resource>();
            for ( DroolsResourceAdapter res : resources ) {
                InternalResource resource = (InternalResource) res.getDroolsResource();
    
                resource.setResourceType( res.getResourceType() );
    
                if ( res.getResourceConfiguration() != null ) {
                    resource.setConfiguration( res.getResourceConfiguration() );
                }
    
                rs.add( resource );
            }
            ChangeSetImpl changeSet = new ChangeSetImpl();
            changeSet.setResourcesAdded( rs );
    
            kagent.applyChangeSet( changeSet );
        }
    }

    public KnowledgeBase getKbase() {
        return kbase;
    }

    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    public KnowledgeAgent getKagent() {
        return kagent;
    }

    public void setKagent(KnowledgeAgent kagent) {
        this.kagent = kagent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNewInstance() {
        return newInstance;
    }

    public void setNewInstance(String newInstance) {
        this.newInstance = newInstance;
    }

    public List<DroolsResourceAdapter> getResources() {
        return resources;
    }

    public void setResources(List<DroolsResourceAdapter> resources) {
        this.resources = resources;
    }

    public boolean isSingleton() {
        return false;
    }

}
