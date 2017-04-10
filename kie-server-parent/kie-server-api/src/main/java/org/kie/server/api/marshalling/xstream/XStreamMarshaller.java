/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.drools.core.runtime.help.impl.XStreamXML;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetReleaseIdCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.commands.optaplanner.CreateSolverCommand;
import org.kie.server.api.commands.optaplanner.DisposeSolverCommand;
import org.kie.server.api.commands.optaplanner.GetBestSolutionCommand;
import org.kie.server.api.commands.optaplanner.GetSolverStateCommand;
import org.kie.server.api.commands.optaplanner.GetSolversCommand;
import org.kie.server.api.commands.optaplanner.UpdateSolverStateCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.dmn.DMNContextKS;
import org.kie.server.api.model.dmn.DMNDecisionInfo;
import org.kie.server.api.model.dmn.DMNDecisionResultKS;
import org.kie.server.api.model.dmn.DMNMessageKS;
import org.kie.server.api.model.dmn.DMNModelInfo;
import org.kie.server.api.model.dmn.DMNModelInfoList;
import org.kie.server.api.model.dmn.DMNNodeStub;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.SolverInstance;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

public class XStreamMarshaller
        implements Marshaller {

    private static final Logger logger = LoggerFactory.getLogger(XStreamMarshaller.class);
    protected XStream xstream;
    protected ClassLoader classLoader;
    protected Map<String, Class> classNames = new HashMap<String, Class>();

    // Optional marshaller extensions to handle new types / configure custom behavior
    private static final List<XStreamMarshallerExtension> EXTENSIONS;

    static {
        logger.debug("XStreamMarshaller extensions init");
        ServiceLoader<XStreamMarshallerExtension> plugins = ServiceLoader.load(XStreamMarshallerExtension.class);
        List<XStreamMarshallerExtension> loadedPlugins = new ArrayList<>();
        plugins.forEach(plugin -> {
            logger.info("XStreamMarshallerExtension implementation found: {}", plugin.getClass().getName());
            loadedPlugins.add(plugin);
        });
        EXTENSIONS = Collections.unmodifiableList(loadedPlugins);
    }

    public XStreamMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {
        this.classLoader = classLoader;
        buildMarshaller(classes, classLoader);

        configureMarshaller(classes, classLoader);
        // Extend the marshaller with optional extensions
        EXTENSIONS.forEach(ext -> ext.extend(this));
    }

    protected void buildMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {
        this.xstream = XStreamXML.newXStreamMarshaller( new XStream( new PureJavaReflectionProvider() ) {
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(chainMapperWrappers(new ArrayList<>(EXTENSIONS), next)) {
                    public Class realClass(String elementName) {

                        Class customClass = classNames.get(elementName);
                        if (customClass != null) {
                            return customClass;
                        }
                        return super.realClass(elementName);
                    }
                };
            }
        });
    }

    private MapperWrapper chainMapperWrappers(List<XStreamMarshallerExtension> extensions, MapperWrapper last) {
        if (extensions.isEmpty()) {
            return last;
        } else {
            XStreamMarshallerExtension head = extensions.remove(0);
            return head.chainMapperWrapper(chainMapperWrappers(extensions, last));
        }
    }

    protected void configureMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {
        this.xstream.setClassLoader( classLoader );

        AbstractScoreXStreamConverter.registerScoreConverters(xstream);

        this.xstream.processAnnotations( CommandScript.class );
        this.xstream.processAnnotations( CallContainerCommand.class );
        this.xstream.processAnnotations( CreateContainerCommand.class );
        this.xstream.processAnnotations( DisposeContainerCommand.class );
        this.xstream.processAnnotations( GetContainerInfoCommand.class );
        this.xstream.processAnnotations( GetScannerInfoCommand.class );
        this.xstream.processAnnotations( UpdateScannerCommand.class );
        this.xstream.processAnnotations( GetReleaseIdCommand.class );
        this.xstream.processAnnotations( UpdateReleaseIdCommand.class );
        this.xstream.processAnnotations( GetServerInfoCommand.class );
        this.xstream.processAnnotations( ListContainersCommand.class );
        this.xstream.processAnnotations( ServiceResponsesList.class );
        this.xstream.processAnnotations( ServiceResponse.class );
        this.xstream.processAnnotations( KieContainerResourceList.class );
        this.xstream.processAnnotations( KieContainerResource.class );
        this.xstream.processAnnotations( ReleaseId.class );
        this.xstream.processAnnotations( KieContainerStatus.class );
        this.xstream.processAnnotations( KieScannerResource.class );
        this.xstream.processAnnotations( KieServerInfo.class );

        this.xstream.processAnnotations( ReleaseIdFilter.class );
        this.xstream.processAnnotations( KieContainerStatusFilter.class );
        this.xstream.processAnnotations( KieContainerResourceFilter.class );

        this.xstream.processAnnotations( SolverInstance.class );
        this.xstream.processAnnotations( CreateSolverCommand.class );
        this.xstream.processAnnotations( DisposeSolverCommand.class );
        this.xstream.processAnnotations( GetBestSolutionCommand.class );
        this.xstream.processAnnotations( GetSolversCommand.class );
        this.xstream.processAnnotations( GetSolverStateCommand.class );
        this.xstream.processAnnotations( UpdateSolverStateCommand.class );
        
        this.xstream.processAnnotations( DMNContextKS.class );
        this.xstream.processAnnotations( DMNResultKS.class );
        this.xstream.processAnnotations( DMNNodeStub.class );
        this.xstream.processAnnotations( DMNMessageKS.class );
        this.xstream.processAnnotations( DMNDecisionResultKS.class);
        this.xstream.processAnnotations( DMNModelInfoList.class );
        this.xstream.processAnnotations( DMNModelInfo.class );
        this.xstream.processAnnotations( DMNDecisionInfo.class);
        
        if (classes != null) {
            for (Class<?> clazz : classes) {
                this.xstream.processAnnotations( clazz );
                this.classNames.put(clazz.getName(), clazz);
            }
        }
    }

    @Override
    public String marshall(Object objectInput) {
        return xstream.toXML( objectInput );
    }

    @Override
    public <T> T unmarshall(String input, Class<T> type) {
        return (T) xstream.fromXML( input );
    }


    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public MarshallingFormat getFormat() {
        return MarshallingFormat.XSTREAM;
    }

    @Override
    public String toString() {
        return "Marshaller{ XSTREAM }";
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.xstream.setClassLoader( classLoader );
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public XStream getXstream() {
        return xstream;
    }

}
