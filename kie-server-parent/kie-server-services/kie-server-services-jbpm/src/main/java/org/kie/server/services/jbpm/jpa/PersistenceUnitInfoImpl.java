/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.jbpm.jpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

    private String persistenceUnitName;
    private String persistenceProviderClassName;
    private String transactionType = "JTA";
    private String jtaDataSource;
    private String nonJtaDataSource;
    private List<String> mappingFileNames = new ArrayList<String>();
    private List<URL> jarFileUrls = new ArrayList<URL>();
    private URL persistenceUnitRootUrl;
    private List<String> managedClassNames = new ArrayList<String>();
    private boolean excludeUnlistedClasses;
    private String sharedCacheMode = SharedCacheMode.NONE.toString();
    private String validationMode = ValidationMode.NONE.toString();
    private Properties properties = new Properties();
    private String persistenceXMLSchemaVersion;

    private Set<ClassTransformer> classTransformers = new HashSet<ClassTransformer>();

    private ClassLoader classLoader;
    private ClassLoader tmpClassLoader;

    private InitialContext initialContext;

    public PersistenceUnitInfoImpl(InitialContext initialContext, ClassLoader classLoader) {
        this.initialContext = initialContext;
        this.classLoader = classLoader;
        this.tmpClassLoader = classLoader;
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(transactionType);
    }

    @Override
    public DataSource getJtaDataSource() {
        if (jtaDataSource == null) {
            return null;
        }
        try {
            return (DataSource) initialContext.lookup(jtaDataSource);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to find jta data source under name " + jtaDataSource);
        }
    }

    @Override
    public DataSource getNonJtaDataSource() {
        if (nonJtaDataSource == null) {
            return null;
        }
        try {
            return (DataSource) initialContext.lookup(nonJtaDataSource);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to find non jta data source under name " + nonJtaDataSource);
        }
    }

    @Override
    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.valueOf(sharedCacheMode);
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.valueOf(validationMode);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return persistenceXMLSchemaVersion;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        classTransformers.add(transformer);
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return tmpClassLoader;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setJtaDataSource(String jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    public void setNonJtaDataSource(String nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        this.mappingFileNames = mappingFileNames;
    }

    public void setJarFileUrls(List<URL> jarFileUrls) {
        this.jarFileUrls = jarFileUrls;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        this.managedClassNames = managedClassNames;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public void setSharedCacheMode(String sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    public void setValidationMode(String validationMode) {
        this.validationMode = validationMode;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    public void setClassTransformers(Set<ClassTransformer> classTransformers) {
        this.classTransformers = classTransformers;
    }

    public void addProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public void addMappingFile(String mappingFileName) {
        mappingFileNames.add(mappingFileName);
    }

    public void addJarFileUrl(String jarFileUrl) {
        try {
            jarFileUrls.add(new URL(jarFileUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addManagedClassName(String managedClassName) {
        managedClassNames.add(managedClassName);
    }

    @Override
    public String toString() {
        return "PersistenceUnitInfoImpl{" +
                "persistenceUnitName='" + persistenceUnitName + '\'' +
                ", persistenceProviderClassName='" + persistenceProviderClassName + '\'' +
                '}';
    }
}
