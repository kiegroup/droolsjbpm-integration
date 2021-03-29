/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.core.impl.InternalKnowledgeBase;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.internal.io.ResourceFactory;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper;
import org.kie.pmml.pmml_4_2.PMML4ExecutionHelper.PMML4ExecutionHelperFactory;
import org.kie.pmml.pmml_4_2.PMMLRequestDataBuilder;
import org.kie.scanner.KieMavenRepository;
import org.kie.scanner.KieURLClassLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.api.pmml.PMMLConstants.KIE_PMML_IMPLEMENTATION;
import static org.kie.api.pmml.PMMLConstants.LEGACY;

public class XStreamMarshallerTest {

    @Test
    public void testPMMLLegacyResult() {
        try {
            System.setProperty(KIE_PMML_IMPLEMENTATION.getName(), LEGACY.getName());
            System.out.println(System.getProperties().keySet());
            PMML4ExecutionHelper helper = PMML4ExecutionHelperFactory.getExecutionHelper("Sample Score",
                                                                                         ResourceFactory.newClassPathResource("test_scorecard.pmml"),
                                                                                         null);
            PMMLRequestData request = new PMMLRequestDataBuilder("123", "Sample Score")
                    .addParameter("age", 33.0, Double.class)
                    .addParameter("occupation", "SKYDIVER", String.class)
                    .addParameter("residenceState", "KN", String.class)
                    .addParameter("validLicense", true, Boolean.class)
                    .build();
            helper.addPossiblePackageName("org.drools.scorecards.example");
            PMML4Result resultHolder = helper.submitRequest(request);
            KieBase kb = helper.getKbase();
            KieServices ks = KieServices.Factory.get();
            KieRepository repo = ks.getRepository();
            ReleaseId relid = ((InternalKnowledgeBase) kb).getResolvedReleaseId();
            KieModule m = repo.getKieModule(relid);

            KieMavenRepository kmp = KieMavenRepository.getKieMavenRepository();
            String pomFileName = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "pom.xml";
            try (FileOutputStream fos = new FileOutputStream(pomFileName)) {
                ByteArrayInputStream bais = (ByteArrayInputStream) ((MemoryKieModule) m).getMemoryFileSystem().getFile("META-INF/maven/org.default/artifact/pom.xml").getContents();
                int readVal;
                do {
                    readVal = bais.read();
                    if (readVal >= 0) {
                        fos.write(readVal);
                    }
                } while (readVal >= 0);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail();
            }
            File pomFile = new File(pomFileName);
            kmp.installArtifact(relid, (InternalKieModule) m, pomFile);

            Set<Class<?>> extraClasses = new HashSet<>();
            ClassLoader clParent = this.getClass().getClassLoader();
            URL urls[] = new URL[1];
            try {
                File f = kmp.resolveArtifact(relid).getFile();
                urls[0] = f.toURI().toURL();
            } catch (MalformedURLException mux) {

            }
            ClassLoader clToUse = new KieURLClassLoader(urls, clParent);

            Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, clToUse);
            String marshalled = marshaller.marshall(resultHolder);
            System.out.println(marshalled);

            PMML4Result result = marshaller.unmarshall(marshalled, PMML4Result.class);
            Object o = result.getResultValue("CalculatedScore", null);
            Object o1 = result.getResultValue("CalculatedScore", "value");

            o = result.getResultValue("ScoreCard", null);
            assertNotNull(o);
            o1 = result.getResultValue("ScoreCard", "ranking");
            assertNotNull(o1);
            assertTrue(LinkedHashMap.class.isAssignableFrom(o1.getClass()));
            @SuppressWarnings("rawtypes")
            LinkedHashMap map = (LinkedHashMap) o1;
            assertTrue(map.containsKey("LX00"));
            assertTrue(map.containsKey("RES"));
            assertTrue(map.containsKey("CX2"));
            assertEquals(-1.0, map.get("LX00"));
            assertEquals(-10.0, map.get("RES"));
            assertEquals(-30.0, map.get("CX2"));
            @SuppressWarnings("rawtypes")
            Iterator iter = map.keySet().iterator();
            assertEquals("LX00", iter.next());
            assertEquals("RES", iter.next());
            assertEquals("CX2", iter.next());
        } finally {
            System.clearProperty(KIE_PMML_IMPLEMENTATION.getName());
        }
    }

}
