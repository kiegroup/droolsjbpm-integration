/*
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

/*
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

package org.drools.camel.component;

import java.util.Collection;
import java.util.HashMap;

import javax.naming.Context;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DroolsCamelTestSupport extends CamelTestSupport {
    protected static final Logger LOG = LoggerFactory.getLogger( DroolsCamelTestSupport.class );
    protected GridNode            node;

    public void setNode(GridNode node) {
        this.node = node;
    }

    public GridNode getNode() {
        return node;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        XMLUnit.setIgnoreComments( true );
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );
        XMLUnit.setNormalizeWhitespace( true );
        XMLUnit.setNormalize( true );
    }

    @Override
    protected Context createJndiContext() throws Exception {
        // Overriding this method is necessary in the absence of a spring application context 
        // to bootstrap the whole thing.  Create another Spring based unit test with all the beans
        // defined as below and remove this comment from here.
        //create
        Context context = super.createJndiContext();

        GridImpl grid = new GridImpl( new HashMap() );
//        grid.addService( WhitePages.class,
//                         new WhitePagesImpl() );
        node = grid.createGridNode( "node" );
        context.bind( "node",
                      node );
        configureDroolsContext( context );
        return context;
    }

    protected abstract void configureDroolsContext(Context jndiContext);

    protected StatefulKnowledgeSession registerKnowledgeRuntime(String identifier,
                                                                String rule) {
        KnowledgeBuilder kbuilder = node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        if ( rule != null && rule.length() > 0 ) {
            kbuilder.add( ResourceFactory.newByteArrayResource(rule.getBytes()),
                          ResourceType.DRL );

            if ( kbuilder.hasErrors() ) {
                LOG.info( "Errors while adding rule. ",
                          kbuilder.getErrors() );
            }
        }
        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();
        KnowledgeBase kbase = node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        node.set( identifier,
                  session );

        return session;
    }

    protected void assertXMLEqual(String expected,
                                  String result) throws Exception {
        Diff diff = new Diff( expected,
                              result );
        diff.overrideElementQualifier( new RecursiveElementNameAndTextQualifier() );
        XMLAssert.assertXMLEqual( diff,
                                  true );
    }

    protected void configureDroolsContext() {
        // TODO Auto-generated method stub

    }
}
