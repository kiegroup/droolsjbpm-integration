/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.vsm.ServiceManager;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;

public class XStreamFromXmlVsmTransformer extends BaseEmitter
    implements
    Transformer {

    private XStreamResolverStrategy strategy;

    public XStreamFromXmlVsmTransformer(XStreamResolverStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void receive(Object object,
                        PipelineContext context) {            
        Object payload = null;
        try {
            Document d = ( Document ) object;
            
            String name = d.getDocumentElement().getAttribute( "lookup" );            
            
            XStream xstream = this.strategy.lookup( name );
            
            if ( xstream == null ) {
                throw new IllegalArgumentException( "Unable to lookup XStream parser using name '" + name + "'" );
            }
            
            ServiceManagerPipelineContextImpl vsmContext = ( ServiceManagerPipelineContextImpl ) context;            
            ServiceManager sm = vsmContext.getServiceManager();
            CommandExecutor executor  = sm.lookup( name );
            
            if ( executor == null ) {
                throw new IllegalArgumentException( "Unable to lookup CommandExecutor using name '" + name + "'" );
            }            
            
            vsmContext.setCommandExecutor( executor );
            
            ClassLoader cl = null;
            if ( executor instanceof StatefulKnowledgeSessionImpl ) {
                cl = ((ReteooRuleBase)(( StatefulKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
                xstream.setClassLoader( cl );
            } else if ( executor instanceof StatelessKnowledgeSessionImpl ) {
                cl = ((ReteooRuleBase)(( StatelessKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
                
            } else {
                throw new IllegalArgumentException( "Unable to set ClassLoader on " + executor );
            }
            
            xstream.setClassLoader( cl );
            vsmContext.setClassLoader( cl );
            
            payload = xstream.unmarshal( new DomReader( d ) );  
            
            context.getProperties().put( "xstream-instance", xstream );
            
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( payload,
              context );
    }
}