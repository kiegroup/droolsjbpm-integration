/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.springboot.datasources;


import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnClass({ XADataSource.class })
public class JBPMDataSourceAutoConfiguration {

    private XADataSource xaDataSource;
    private XADataSourceWrapper wrapper;
    
    public JBPMDataSourceAutoConfiguration(XADataSourceWrapper wrapper) {
        this.wrapper = wrapper;
    }
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() throws Exception {
        this.xaDataSource = createXaDataSource();        
        return this.wrapper.wrapDataSource(xaDataSource);
    } 
    
    /*
     * Optional quartz configuration - by default same data source is used for transactional Quartz work
     * and new one (from properties quartz.datasource) for unmanaged access
     */
    
    @Bean
    @ConditionalOnMissingBean(name = "quartzDataSource")
    @ConditionalOnProperty(name = {"jbpm.quartz.enabled", "jbpm.quartz.db"}, havingValue="true")
    public DataSource quartzDataSource(DataSource dataSource) {
        return dataSource;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "quartzDatasourceProperties")
    @ConfigurationProperties("quartz.datasource")
    public DataSourceProperties quartzDatasourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "quartzPoolProperties")
    @ConfigurationProperties("quartz.datasource.dbcp2")
    public Map<String, Object> quartzPoolProperties() {
        return new HashMap<>();
    }

    @Bean
    @ConditionalOnMissingBean(name = "quartzNotManagedDataSource")
    @ConditionalOnProperty(name = {"jbpm.quartz.enabled", "jbpm.quartz.db"}, havingValue="true")
    public DataSource quartzNotManagedDataSource() {
        DataSource ds = quartzDatasourceProperties().initializeDataSourceBuilder().build();
        Map<String, Object> poolProperties = quartzPoolProperties();
        
        MapConfigurationPropertySource properties = new MapConfigurationPropertySource(poolProperties);
        Binder binder = new Binder(properties);
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(ds));
        
        return ds;
    }
    
    /*
     * Helper methods
     */

    private XADataSource createXaDataSource() {
        DataSourceProperties dataSourceProperties = dataSourceProperties();
        
        String className = dataSourceProperties.getXa().getDataSourceClassName();
        if (!StringUtils.hasLength(className)) {
            className = DatabaseDriver.fromJdbcUrl(dataSourceProperties.determineUrl())
                    .getXaDataSourceClassName();
        }
        Assert.state(StringUtils.hasLength(className),
                "No XA DataSource class name specified");
        XADataSource dataSource = createXaDataSourceInstance(className);
        bindXaProperties(dataSource, dataSourceProperties);
        return dataSource;
    }

    private XADataSource createXaDataSourceInstance(String className) {
        try {
            Class<?> dataSourceClass = ClassUtils.forName(className, this.getClass().getClassLoader());
            Object instance = BeanUtils.instantiate(dataSourceClass);
            Assert.isInstanceOf(XADataSource.class, instance);
            return (XADataSource) instance;
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to create XADataSource instance from '" + className + "'", ex);
        }
    }

    private void bindXaProperties(XADataSource target, DataSourceProperties properties) {
        MapConfigurationPropertySource values = new MapConfigurationPropertySource();
        values.put("user", properties.determineUsername());
        values.put("password", properties.determinePassword());
        values.put("url", properties.determineUrl());
        values.putAll(properties.getXa().getProperties());
        
        ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();
        aliases.addAliases("user", "username");
        
        Binder binder = new Binder(values.withAliases(aliases));
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(target));
    }
}
