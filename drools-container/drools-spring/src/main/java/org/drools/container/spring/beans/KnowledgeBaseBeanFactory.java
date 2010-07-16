package org.drools.container.spring.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.util.JAXBResult;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.RuleBaseConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.impl.JaxbConfigurationImpl;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.grid.ExecutionNode;
import org.drools.grid.local.LocalConnection;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.process.core.WorkDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class KnowledgeBaseBeanFactory
    implements
    FactoryBean,
    InitializingBean {

    private KnowledgeBaseConfiguration  conf;
    
    private KnowledgeBase               kbase;
    private ExecutionNode               node;
    private List<DroolsResourceAdapter> resources = Collections.emptyList();

    public Object getObject() throws Exception {
        return kbase;
    }

    public Class< ? extends KnowledgeBase> getObjectType() {
        return KnowledgeBase.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if ( node == null ) {
            node = new LocalConnection().getExecutionNode();
        }
         
        KnowledgeBuilder kbuilder = node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();
        if ( this.conf != null ) {
            kbase = node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase(conf);
        } else {
            kbase = node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase( );
        }
 
        List<JaxbConfigurationImpl> xsds = new ArrayList<JaxbConfigurationImpl>();
        
        for ( DroolsResourceAdapter res : resources ) {            
            if ( res.getResourceType().equals( ResourceType.XSD ) ) {
                xsds.add( ( JaxbConfigurationImpl ) res.getResourceConfiguration() );
            }
            
            if ( res.getResourceConfiguration() == null ) {
                kbuilder.add( res.getDroolsResource(),
                              res.getResourceType() );
            } else {
                kbuilder.add( res.getDroolsResource(),
                              res.getResourceType(),
                              res.getResourceConfiguration() );
            }
        }

        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if ( !errors.isEmpty() ) {
            throw new RuntimeException( errors.toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        
        KnowledgeBaseImpl kbaseImpl = ( KnowledgeBaseImpl ) kbase;
        kbaseImpl.jaxbClasses = new ArrayList();
        for ( JaxbConfigurationImpl conf : xsds ) {
            kbaseImpl.jaxbClasses.add( conf.getClasses() );            
        }

    }   

    public KnowledgeBaseConfiguration getConf() {
        return conf;
    }

    public void setConf(KnowledgeBaseConfiguration conf) {
        this.conf = conf;
    }

    public KnowledgeBase getKbase() {
        return kbase;
    }

    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    public ExecutionNode getNode() {
        return node;
    }

    public List<DroolsResourceAdapter> getResources() {
        return resources;
    }

    public void setResources(List<DroolsResourceAdapter> resources) {
        this.resources = resources;
    }

    public void setNode(ExecutionNode executionNode) {
        this.node = executionNode;
    }

}
