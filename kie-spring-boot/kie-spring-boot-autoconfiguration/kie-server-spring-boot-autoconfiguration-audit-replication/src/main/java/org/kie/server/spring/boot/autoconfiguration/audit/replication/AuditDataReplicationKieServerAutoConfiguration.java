/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.jbpm.springboot.autoconfigure.EntityManagerFactoryHelper;
import org.jbpm.springboot.autoconfigure.JBPMAutoConfiguration;
import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.springboot.autoconfiguration.KieServerProperties;
import org.kie.soup.xstream.XStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import com.thoughtworks.xstream.XStream;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({JBPMAutoConfiguration.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class AuditDataReplicationKieServerAutoConfiguration {

    private static Logger logger = LoggerFactory.getLogger(AuditDataReplicationKieServerAutoConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment env;


    @Bean
    @ConditionalOnMissingBean(name = "jmsSender")
    public JMSSender createAuditDataReplicationSender() {
        if (env.containsProperty("kieserver.audit-replication.queue")) {
            String endpointName = env.getProperty("kieserver.audit-replication.queue");
            return new JMSSender(endpointName);
        } else {
            String endpointName = env.getProperty("kieserver.audit-replication.topic");
            return new JMSSender(endpointName);
        }
    }


    @Bean
    @ConditionalOnMissingBean(name = "xstreamBean")
    public XStream createXStream() {
        XStream xstream = XStreamUtils.createTrustingXStream();
        String[] voidDeny = {"void.class", "Void.class"};
        xstream.denyTypes(voidDeny);
        return xstream;
    }

    @Bean(name = "auditEntityManagerFactory")
    @ConditionalOnMissingBean(name = "auditEntityManagerFactory")
    @ConditionalOnProperty(name = "kieserver.audit-replication.consumer", havingValue = "true")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaProperties jpaProperties) {
        return EntityManagerFactoryHelper.create(applicationContext,
                                                 dataSource,
                                                 jpaProperties,
                                                 "org.jbpm.audit",
                                                 "classpath:/META-INF/jbpm-audit-persistence.xml");
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditDataReplicationProcessEventListenerProducer")
    @ConditionalOnProperty(name = "kieserver.audit-replication.producer", havingValue = "true")
    public ProcessEventListener createProcessEventListenerProducer() {
        logger.info("Adding AuditDataReplicationProcessEvent from data replication");
        return new AuditDataReplicationProcessEventProducer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditDataReplicationTaskLifeCycleEventListenerProducer")
    @ConditionalOnProperty(name = "kieserver.audit-replication.producer", havingValue = "true")
    public TaskLifeCycleEventListener createTaskLifeCycleEventListener(EntityManagerFactory emf) {
        logger.info("Adding AuditDataReplicationTaskLifeCycleEventListenerProducer from data replication");
        return new AuditDataReplicationTaskLifeCycleEventListenerProducer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditDataReplicationBAMTaskSumaryListenerProducer")
    @ConditionalOnProperty(name = "kieserver.audit-replication.producer", havingValue = "true")
    public TaskLifeCycleEventListener createBAMTaskSummaryListener(EntityManagerFactory emf) {
        logger.info("Adding AuditDataReplicationBAMTaskSumaryListenerProducer from data replication");
        return new AuditDataReplicationBAMTaskSummaryListenerProducer();
    }

    @Bean(name="auditDataReplicationConsumer")
    @ConditionalOnMissingBean(name = "auditDataReplicationConsumer")
    @ConditionalOnProperty(name = "kieserver.audit-replication.consumer", havingValue = "true")
    public AbstractAuditDataReplicationJMSConsumer createAuditDataReplicationConsumer(@Qualifier("auditEntityManagerFactory") EntityManagerFactory emf) {
        logger.info("Adding auditDataReplicationConsumer from data replication");
        if (env.containsProperty("kieserver.audit-replication.queue")) {
            return new AuditDataReplicationJMSQueueConsumer(emf);
        } else {
            return new AuditDataReplicationJMSTopicConsumer(emf);
        }
    }

    @Bean
    public JBPMPersistenceUnitPostProcessor createPersistenceUnitProcessor() {
        logger.info("Adding OverrideId to the persistence Unit processor");
        return new OverrideIdJBPMPersistenceUnitPostProcessor();
    }

}
