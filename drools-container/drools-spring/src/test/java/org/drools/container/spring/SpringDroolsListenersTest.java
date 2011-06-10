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

import org.drools.ClockType;
import org.drools.Person;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;
import org.drools.SessionConfiguration;
import org.drools.agent.impl.KnowledgeAgentImpl;
import org.drools.common.InternalRuleBase;
import org.drools.conf.EventProcessingOption;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.drools.grid.GridNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ResourceChangeScannerImpl;
import org.drools.io.impl.UrlResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.jbpm.process.instance.impl.humantask.HumanTaskHandler;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SpringDroolsListenersTest {

    @Test
    public void testStatelessAgendaEventListener() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( "org/drools/container/spring/listeners.xml" );

        GridNode node1 = (GridNode) context.getBean( "node1" );
        assertNotNull( node1 );

        GridNode node2 = (GridNode) context.getBean( "node2" );
        assertNotNull( node2 );
    }

}
