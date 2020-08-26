package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import javax.sql.DataSource;

import org.jbpm.services.task.identity.MvelUserGroupCallbackImpl;
import org.jbpm.springboot.autoconfigure.EntityManagerFactoryHelper;
import org.kie.api.task.UserGroupCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@SpringBootApplication
@EnableJms
public class ApplicationSender {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationSender.class, args);
    }

    @Autowired
    private ApplicationContext applicationContext;


    @Bean(name="userGroupCallback")
    public UserGroupCallback userGroupCallback() {
        return new MvelUserGroupCallbackImpl(true);
    }

    @Bean(name="datasource-replica")
    @ConfigurationProperties(prefix="spring.datasource.second")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "auditEntityManagerFactory")
    @ConditionalOnMissingBean(name = "auditEntityManagerFactory")
    @ConditionalOnProperty(name = "kieserver.audit-replication.consumer", havingValue = "true")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("datasource-replica") DataSource dataSource, JpaProperties jpaProperties) {
        jpaProperties.getProperties().put("url", "jdbc:h2:mem:kieserver-replication");
        return EntityManagerFactoryHelper.create(applicationContext,
                                                 dataSource,
                                                 jpaProperties,
                                                 "org.jbpm.audit",
                                                 "classpath:/META-INF/jbpm-audit-persistence.xml");
    }

}
