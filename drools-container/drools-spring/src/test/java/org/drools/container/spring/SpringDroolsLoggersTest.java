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

package org.drools.container.spring;

import org.drools.Person;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.process.*;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.Channel;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpringDroolsLoggersTest {

    static ClassPathXmlApplicationContext context = null;
    @BeforeClass
	public static void runBeforeClass() {
         context = new ClassPathXmlApplicationContext( "org/drools/container/spring/loggers.xml" );
    }
    
    @Test
    public void testStatelessKnowledgeLogger() throws Exception {
        StatefulKnowledgeSession statefulSession = (StatefulKnowledgeSession) context.getBean( "statefulSession" );
        List<String> list = new ArrayList<String>();
        statefulSession.setGlobal( "list", list );
        statefulSession.insert(new Person("Darth", "Cheddar", 50));
        statefulSession.fireAllRules();

    }
}
