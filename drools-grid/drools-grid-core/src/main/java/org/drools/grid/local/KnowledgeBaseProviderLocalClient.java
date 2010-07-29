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

package org.drools.grid.local;

import java.util.Properties;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeSessionConfiguration;

public class KnowledgeBaseProviderLocalClient
    implements
    KnowledgeBaseFactoryService {

    public Environment newEnvironment() {
        return KnowledgeBaseFactory.newEnvironment();
    }

    public KnowledgeBase newKnowledgeBase() {
        return newKnowledgeBase( ( KnowledgeBaseConfiguration ) null );
    }

    public KnowledgeBase newKnowledgeBase(KnowledgeBaseConfiguration conf) {
        return KnowledgeBaseFactory.newKnowledgeBase( conf );
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration() {
        return KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
    }

    public KnowledgeBaseConfiguration newKnowledgeBaseConfiguration(Properties properties,
                                                                    ClassLoader... classLoader) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeSessionConfiguration newKnowledgeSessionConfiguration(Properties properties) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId) {
        // TODO Auto-generated method stub
        return null;
    }

    public KnowledgeBase newKnowledgeBase(String kbaseId,
                                          KnowledgeBaseConfiguration conf) {
        // TODO Auto-generated method stub
        return null;
    }

}
