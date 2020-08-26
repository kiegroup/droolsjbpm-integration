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

package org.jbpm.springboot.autoconfigure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.util.ClassUtils;

public final class EntityManagerFactoryHelper {

    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    private EntityManagerFactoryHelper() {
        // nothing
    }

    public static LocalContainerEntityManagerFactoryBean create(ApplicationContext applicationContext, DataSource dataSource, JpaProperties jpaProperties, String puName, String location) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName(puName);
        factoryBean.setPersistenceXmlLocation(location);
        factoryBean.setJtaDataSource(dataSource);
        factoryBean.setJpaPropertyMap(jpaProperties.getProperties());
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setPrepareConnection(false);
        factoryBean.setJpaVendorAdapter(adapter);

        List<PersistenceUnitPostProcessor> postProcessors = new ArrayList<>();
        String packagesToScan = jpaProperties.getProperties().get("entity-scan-packages");
        if (packagesToScan != null) {
            postProcessors.add(new PersistenceUnitPostProcessor() {

                @Override
                public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
                    Set<TypeFilter> entityTypeFilters = new LinkedHashSet<>(3);
                    entityTypeFilters.add(new AnnotationTypeFilter(Entity.class, false));
                    entityTypeFilters.add(new AnnotationTypeFilter(Embeddable.class, false));
                    entityTypeFilters.add(new AnnotationTypeFilter(MappedSuperclass.class, false));

                    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();


                    for (String pkg : packagesToScan.split(",")) {
                        try {
                            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                                             ClassUtils.convertClassNameToResourcePath(pkg) + CLASS_RESOURCE_PATTERN;
                            Resource[] resources = resourcePatternResolver.getResources(pattern);
                            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                            for (Resource resource : resources) {
                                if (resource.isReadable()) {
                                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                                    String className = reader.getClassMetadata().getClassName();
                                    if (matchesFilter(reader, readerFactory, entityTypeFilters)) {
                                        pui.addManagedClassName(className);
                                    } else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                                        pui.addManagedPackage(className.substring(0, className.length() - PACKAGE_INFO_SUFFIX.length()));
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            throw new PersistenceException("Failed to scan classpath for unlisted entity classes", ex);
                        }
                    }
                    

                }

                private boolean matchesFilter(MetadataReader reader, MetadataReaderFactory readerFactory, Set<TypeFilter> entityTypeFilters) throws IOException {
                    for (TypeFilter filter : entityTypeFilters) {
                        if (filter.match(reader, readerFactory)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        postProcessors.addAll(applicationContext.getBeansOfType(JBPMPersistenceUnitPostProcessor.class).values());
        factoryBean.setPersistenceUnitPostProcessors(postProcessors.toArray(new PersistenceUnitPostProcessor[0]));
        return factoryBean;
    }
}
