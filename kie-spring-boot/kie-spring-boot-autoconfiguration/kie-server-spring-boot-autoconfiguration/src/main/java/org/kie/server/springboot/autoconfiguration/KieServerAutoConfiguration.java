/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.springboot.autoconfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.AbstractJaxrsClassesScanServer;
import org.appformer.maven.support.AFReleaseId;
import org.appformer.maven.support.MinimalPomParser;
import org.appformer.maven.support.PomModel;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerContainerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.springboot.ImmutableSpringBootKieServerImpl;
import org.kie.server.springboot.SpringBootKieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

@Configuration
@ConditionalOnClass({ KieServerImpl.class })
@EnableConfigurationProperties(KieServerProperties.class)
public class KieServerAutoConfiguration extends AbstractJaxrsClassesScanServer {
    
    @Value("${cxf.path:/}")
    private String cxfPath;

    private static final Logger logger = LoggerFactory.getLogger(KieServerAutoConfiguration.class);
    
    private KieServerProperties properties;   
    private IdentityProvider identityProvider;
    private List<Object> endpoints;
    
    private boolean jaxrsComponentScanEnabled;
    
    private KieServerImpl kieServer;

    public KieServerAutoConfiguration(KieServerProperties properties, Optional<IdentityProvider> identityProvider,
            @Value("${cxf.jaxrs.classes-scan:false}")boolean jaxrsComponentScanEnabled) {
        this.properties = properties;
        this.jaxrsComponentScanEnabled = jaxrsComponentScanEnabled;
        if (identityProvider.isPresent()) {
            this.identityProvider = identityProvider.get();
        }   
        if (!jaxrsComponentScanEnabled) {
            System.setProperty("cxf.jaxrs.classes-scan-packages", "");
        }
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "kieServerExtension")
    public KieServerExtension kieServerExtension() {        
        return new KieServerContainerExtension();
    }   

    @Bean(destroyMethod="destroy")
    @ConditionalOnMissingBean(name = "kieServer")
    public KieServer kieServer(List<KieServerExtension> extensions, List<KieContainerResource> containers) {
        System.setProperty(KieServerConstants.CFG_SB_CXF_PATH, cxfPath);
        System.setProperty(KieServerConstants.KIE_SERVER_CONTROLLER, properties.getControllers());
        System.setProperty(KieServerConstants.KIE_SERVER_LOCATION, properties.getLocation());
        if (KieServerEnvironment.getServerId() == null) {
            String serverName = properties.getServerName();
            String serverId = properties.getServerId();

            KieServerEnvironment.setServerId(serverId.toString());
            KieServerEnvironment.setServerName(serverName);
        }
        logger.info("KieServer (id {} (name {})) started initialization process", KieServerEnvironment.getServerId(), KieServerEnvironment.getServerName());
        if (properties.isClassPathContainer()) {
            kieServer = new ImmutableSpringBootKieServerImpl(extensions, identityProvider, containers);
        } else {
            kieServer = new SpringBootKieServerImpl(extensions, identityProvider);
        }
        kieServer.init();
        
        KieServerRestImpl kieServerResource = new KieServerRestImpl(kieServer);
        this.endpoints = new ArrayList<>();
        endpoints.add(kieServerResource);
        
        // next add any resources from server extensions
        List<KieServerExtension> activeExtensions = kieServer.getServerExtensions();

        for (KieServerExtension extension : activeExtensions) {
            LinkedHashSet<Object> resources = new LinkedHashSet<>(extension.getAppComponents(SupportedTransports.REST));
            endpoints.addAll(resources);
        }        
        logger.info("KieServer (id {}) started successfully", KieServerEnvironment.getServerId());
        return kieServer;
    }
    
    @Bean
    @ConditionalOnBean(name="kieServer")
    public KieServerRegistry kieServerRegistry(KieServer server) {
        return kieServer.getServerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(name="embeddedDeployments")
    @ConditionalOnProperty(name = "kieserver.autoScanDeployments", havingValue="false", matchIfMissing = true)
    public List<KieContainerResource> buildDeployments(KieServerProperties kieServerProperties) {
        return kieServerProperties.getDeployments().stream().map(k -> {
            KieContainerResource resource = new KieContainerResource(k.getContainerId(), k.getReleaseId());
            resource.setResolvedReleaseId(k.getReleaseId());
            resource.setContainerAlias(k.getAlias());
            resource.setReleaseId(k.getReleaseId());
            resource.setStatus(KieContainerStatus.STARTED);
            return resource;
        }).collect(Collectors.toList());
    }

    @Bean
    @ConditionalOnMissingBean(name="autoScanEmbeddedDeployments")
    @ConditionalOnProperty(name = "kieserver.autoScanDeployments", havingValue = "true")
    public List<KieContainerResource> buildAutoScanDeployments(KieServerProperties kieServerProperties) throws IOException {
        ApplicationHome appHome = new ApplicationHome();
        final String folder = "BOOT-INF/classes/KIE-INF/lib/";
        File root = appHome.getSource();
        return discoverDeployments(folder, new FileInputStream(root));
    }

    private List<KieContainerResource>  discoverDeployments(String folder, InputStream inputStream) {
        List<KieContainerResource> files = new ArrayList<>();
        try (ZipInputStream zipFile = new ZipInputStream(inputStream)) {

            ZipEntry entry = null;
            while ((entry = zipFile.getNextEntry()) != null) {
                // we filter outside folder
                if(!entry.getName().startsWith(folder)) {
                    continue;
                }

                int available = zipFile.available();
                if (available <= 0) {
                    continue;
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                int read = 0;
                while ((read = zipFile.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                byte[]content = out.toByteArray();
                out.close();
                Optional<KieContainerResource> resource = scanPossibleDeployment(new ByteArrayInputStream(content));
                if(resource.isPresent()) {
                    files.add(resource.get());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    private Optional<KieContainerResource> scanPossibleDeployment(InputStream inputStream) {
        boolean isDeployable = false;
        KieContainerResource resource = null;
        ZipEntry entry = null;
        try (ZipInputStream zipFile = new ZipInputStream(inputStream)) {
            while ((entry = zipFile.getNextEntry()) != null) {
                if("META-INF/kmodule.xml".contentEquals(entry.getName())) {
                    isDeployable = true;
                }
                if(entry.getName().startsWith("META-INF/maven") && entry.getName().endsWith("pom.xml")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    int read = 0;
                    while ((read = zipFile.read(buffer)) > 0) {
                        out.write(buffer, 0, read);
                    }
                    byte[] content = out.toByteArray();
                    out.close();
                    PomModel model = MinimalPomParser.parse(entry.getName(), new ByteArrayInputStream(content));
                    AFReleaseId pomReleaseId = model.getReleaseId();
                    ReleaseId releaseId = new ReleaseId(pomReleaseId.getGroupId(), pomReleaseId.getArtifactId(), pomReleaseId.getVersion());
                    resource = new KieContainerResource(releaseId);
                    resource.setContainerId(releaseId.getArtifactId() + "-" + releaseId.getVersion());
                    resource.setStatus(KieContainerStatus.STARTED);
                    resource.setResolvedReleaseId(releaseId);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return isDeployable ? Optional.ofNullable(resource) : Optional.empty();
    }

    @Override
    protected void setJaxrsResources(JAXRSServerFactoryBean factory) {
        factory.setServiceBeans(endpoints);
        if (jaxrsComponentScanEnabled) {
            super.setJaxrsResources(factory);        
        }
    }
    
    @Bean    
    public Server jaxRsServer(KieServer server) {
        return super.createJaxRsServer();
    }

    @Override
    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<>(super.getFeatures());
        if (properties.getSwagger().isEnabled()) {
            try {
                Feature feature = (Feature) Class.forName("org.apache.cxf.jaxrs.swagger.Swagger2Feature").newInstance();
                Method method = ReflectionUtils.findMethod(feature.getClass(), "setRunAsFilter", Boolean.TYPE);
                method.invoke(feature, true);
                Method setBasePathMethod = ReflectionUtils.findMethod(feature.getClass(), "setBasePath", String.class);
                setBasePathMethod.invoke(feature, cxfPath);
                features.add(feature);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Swagger feature was enabled but cannot be created", e);
            }
        }
        return features;
    }
    
}
