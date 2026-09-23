package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.jbpm.services.task.identity.MvelUserGroupCallbackImpl;
import org.jbpm.springboot.autoconfigure.EntityManagerFactoryHelper;
import org.kie.api.task.UserGroupCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
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

    @Bean(name = "userGroupCallback")
    public UserGroupCallback userGroupCallback() {
        return new MvelUserGroupCallbackImpl(true);
    }

    @Bean(name = "datasource-replica")
    public DataSource secondaryDataSource(XADataSourceWrapper xaDataSourceWrapper,
                                          @Value("${spring.datasource.second.url}") String url,
                                          @Value("${spring.datasource.second.username}") String username,
                                          @Value("${spring.datasource.second.password}") String password) throws Exception {
        JdbcDataSource xaDataSource = new JdbcDataSource();
        xaDataSource.setURL(url);
        xaDataSource.setUser(username);
        xaDataSource.setPassword(password);
        return xaDataSourceWrapper.wrapDataSource(xaDataSource);
    }

    @Bean(name = "auditEntityManagerFactory")
    @ConditionalOnMissingBean(name = "auditEntityManagerFactory")
    @ConditionalOnProperty(name = "kieserver.audit-replication.consumer", havingValue = "true")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("datasource-replica") DataSource dataSource, JpaProperties jpaProperties) {
        return EntityManagerFactoryHelper.create(applicationContext,
                                                 dataSource,
                                                 jpaProperties,
                                                 "org.jbpm.audit",
                                                 "classpath:/META-INF/jbpm-audit-persistence.xml");
    }

}
