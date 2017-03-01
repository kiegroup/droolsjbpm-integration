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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import org.kie.server.api.commands.optaplanner.*;
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
import org.kie.server.api.model.dmn.DMNNodeStub;
import org.kie.server.api.model.dmn.DMNResultKS;
import org.kie.server.api.model.instance.SolverInstance;
import org.optaplanner.persistence.xstream.api.score.AbstractScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.bendable.BendableScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.bendablebigdecimal.BendableBigDecimalScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.bendablelong.BendableLongScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoft.HardMediumSoftScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardsoft.HardSoftScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardsoftdouble.HardSoftDoubleScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.hardsoftlong.HardSoftLongScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.simple.SimpleScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.simplebigdecimal.SimpleBigDecimalScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.simpledouble.SimpleDoubleScoreXStreamConverter;
import org.optaplanner.persistence.xstream.api.score.buildin.simplelong.SimpleLongScoreXStreamConverter;

public class XStreamMarshaller
        implements Marshaller {

    protected XStream xstream;
    protected ClassLoader classLoader;
    protected Map<String, Class> classNames = new HashMap<String, Class>();

    public XStreamMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {
        this.classLoader = classLoader;
        buildMarshaller(classes, classLoader);

        configureMarshaller(classes, classLoader);
    }

    protected void buildMarshaller( Set<Class<?>> classes, final ClassLoader classLoader ) {

        this.xstream = XStreamXML.newXStreamMarshaller( new XStream( new PureJavaReflectionProvider() ) {

            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
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
}
