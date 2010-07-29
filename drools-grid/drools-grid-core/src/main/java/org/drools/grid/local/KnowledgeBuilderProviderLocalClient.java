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

/**
 * 
 */
package org.drools.grid.local;

import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.conf.impl.JaxbConfigurationImpl;

import com.sun.tools.xjc.Options;

public class KnowledgeBuilderProviderLocalClient
    implements
    KnowledgeBuilderFactoryService {

    public DecisionTableConfiguration newDecisionTableConfiguration() {
        return KnowledgeBuilderFactory.newDecisionTableConfiguration();
    }

    public KnowledgeBuilder newKnowledgeBuilder() {
        return KnowledgeBuilderFactory.newKnowledgeBuilder();
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBuilderConfiguration conf) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( conf );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( kbase );
    }

    public KnowledgeBuilder newKnowledgeBuilder(KnowledgeBase kbase,
                                                KnowledgeBuilderConfiguration conf) {
        return KnowledgeBuilderFactory.newKnowledgeBuilder( kbase,
                                                            conf );
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration() {
        return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
    }

    public KnowledgeBuilderConfiguration newKnowledgeBuilderConfiguration(Properties properties,
                                                                          ClassLoader... classLoaders) {
        return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration( properties,
                                                                         classLoaders );
    }
    
    public JaxbConfiguration newJaxbConfiguration(Options xjcOpts,
                                                  String systemId) {
        return KnowledgeBuilderFactory.newJaxbConfiguration( xjcOpts, systemId );
    }    

}