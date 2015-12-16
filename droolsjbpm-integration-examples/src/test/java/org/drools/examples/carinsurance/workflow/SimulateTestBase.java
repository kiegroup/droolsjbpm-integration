/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package org.drools.examples.carinsurance.workflow;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.conf.ClockTypeOption;

public class SimulateTestBase {

    protected void createKJar(String... pairs) throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for ( int i = 0; i < pairs.length; i += 2 ) {
            String id = pairs[i];
            String rule = pairs[i + 1];

            kfs.write( "src/kbases/" + id + "/org/test/rule" + i + ".drl", rule );

            KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                    .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                    .setEventProcessingMode( EventProcessingOption.STREAM );

            KieSessionModel ksession1 = kBase1.newKieSessionModel( id+".KSession1" )
                    .setType( KieSessionModel.KieSessionType.STATEFUL )
                    .setClockType( ClockTypeOption.get( "pseudo" ) );
        }

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        assertTrue(kieBuilder.buildAll().getResults().getMessages().isEmpty());
    }

    protected ReleaseId createKJarWithMultipleResources(String id,
                                                   String[] resources,
                                                   ResourceType[] types) throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for ( int i = 0; i < resources.length; i++ ) {
            String res = resources[i];
            String type = types[i].getDefaultExtension();

            kfs.write( "src/main/resources/" + id + "/org/test/res" + i + "." + type, res );
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel( id )
                .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM );

        KieSessionModel ksession1 = kBase1.newKieSessionModel( id + ".KSession1" )
                .setType( KieSessionModel.KieSessionType.STATEFUL )
                .setClockType( ClockTypeOption.get( "pseudo" ) );

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        assertTrue(kieBuilder.buildAll().getResults().getMessages().isEmpty());
        return kieBuilder.getKieModule().getReleaseId();
    }

    protected String readInputStreamReaderAsString(InputStreamReader in) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(in);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
