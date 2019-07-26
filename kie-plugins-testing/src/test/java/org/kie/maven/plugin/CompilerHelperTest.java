/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.maven.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.drools.compiler.kie.builder.impl.DrlProject;
import org.drools.compiler.kie.builder.impl.FileKieModule;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.drools.core.common.ProjectClassLoader;
import org.drools.core.rule.KieModuleMetaInfo;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.rule.TypeMetaInfo;
import org.drools.core.util.FileManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.type.Role;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;

import static ch.qos.logback.core.encoder.ByteArrayUtil.hexStringToByteArray;
import static ch.qos.logback.core.encoder.ByteArrayUtil.toHexString;
import static org.assertj.core.api.Assertions.*;

public class CompilerHelperTest {

    private static Log log;
    protected FileManager fileManager;

    @Before
    public void setUp() {
        log = new SystemStreamLog();
        this.fileManager = new FileManager();
        this.fileManager.setUp();
    }

    @After
    public void tearDown() {
        this.fileManager.tearDown();
    }

    @Test
    public void getCompilationIDTest() {
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> map = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        map.put("compilation.ID",
                uuid);
        String compilationID = helper.getCompilationID(map,
                                                       log);
        assertThat(compilationID).isEqualTo(uuid);
    }

    @Test
    public void getWrongCompilationIDTest() {
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> map = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        map.put("compilation..ID",
                uuid);
        String compilationID = helper.getCompilationID(map,
                                                       log);
        assertThat(compilationID).isEqualTo("main");
    }

    @Test
    public void shareStoreWithMapTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);
        ProjectClassLoader pcl = ProjectClassLoader.createProjectClassLoader();
        String hexContent = "e04fd020ea3a6910a2d808002b30309d";
        String className = "org/my/Clazz";
        //we store bytes of hexString for an easy compare
        pcl.storeClass(className,
                       hexStringToByteArray(hexContent));

        //call the class under test
        helper.shareStoreWithMap(pcl,
                                 compilationID,
                                 kieMap,
                                 log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append("ProjectClassloaderStore");
        kieMap.get(sbTypes.toString());
        Map store = (Map) kieMap.get(sbTypes.toString());
        assertThat(store).isNotNull();
        assertThat(store).hasSize(1);
        byte[] bytez = (byte[]) store.get(className + ".class");
        assertThat(bytez).isNotNull();
        String hex = toHexString(bytez);
        assertThat(hex).isEqualTo(hexContent);
    }

    @Test
    public void shareNoStoreWithMapTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);
        ProjectClassLoader pcl = ProjectClassLoader.createProjectClassLoader();

        //call the class under test
        helper.shareStoreWithMap(pcl,
                                 compilationID,
                                 kieMap,
                                 log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append("ProjectClassloaderStore");
        kieMap.get(sbTypes.toString());
        Map store = (Map) kieMap.get(sbTypes.toString());
        assertThat(store).isNull();
    }

    @Test
    public void shareWrongStoreWithMapTest() throws Exception {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        String compilationID = UUID.randomUUID().toString();
        Map<String,Object> kieMap = Collections.singletonMap("compilation.ID", compilationID);

        File dir = new File(System.getProperty("user.dir")
                                    + File.separator + "dir" + File.separator);
        URL url = dir.toURL();
        ClassLoader cl = new URLClassLoader(new URL[]{url});
        //call the class under test
        helper.shareStoreWithMap(cl,
                                 compilationID,
                                 kieMap,
                                 log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append("ProjectClassloaderStore");
        kieMap.get(sbTypes.toString());
        Map store = (Map) kieMap.get(sbTypes.toString());
        assertThat(store).isNull();
    }

    @Test
    public void shareKieObjectsTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);
        KieModule kieModule = createKieModule("fol4",
                                              false,
                                              "1.0-SNAPSHOT");

        //method to test
        helper.shareKieObjectsWithMap((InternalKieModule) kieModule,
                                      compilationID,
                                      kieMap,
                                      log);

        //verify
        StringBuilder sbModelMetaInfo = new StringBuilder(compilationID).append(".").append(KieModuleMetaInfo.class.getName());
        StringBuilder sbkModule = new StringBuilder(compilationID).append(".").append(FileKieModule.class.getName());
        kieMap.get(sbkModule.toString());
        KieModuleMetaInfo metaInfo = (KieModuleMetaInfo) kieMap.get(sbModelMetaInfo.toString());
        MemoryKieModule module = (MemoryKieModule) kieMap.get(sbkModule.toString());
        assertThat(metaInfo).isNotNull();
        assertThat(module).isNotNull();
    }

    @Test
    public void shareNullKieObjectsTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);

        //method to test
        helper.shareKieObjectsWithMap(null,
                                      null,
                                      kieMap,
                                      log);

        //verify
        StringBuilder sbModelMetaInfo = new StringBuilder(compilationID).append(".").append(KieModuleMetaInfo.class.getName());
        StringBuilder sbkModule = new StringBuilder(compilationID).append(".").append(FileKieModule.class.getName());
        kieMap.get(sbkModule.toString());
        KieModuleMetaInfo metaInfo = (KieModuleMetaInfo) kieMap.get(sbModelMetaInfo.toString());
        MemoryKieModule module = (MemoryKieModule) kieMap.get(sbkModule.toString());
        assertThat(metaInfo).isNull();
        assertThat(module).isNull();
    }

    @Test
    public void shareTypesFromMapTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);

        TypeDeclaration typeDeclaration = new TypeDeclaration();
        typeDeclaration.setRole(Role.Type.EVENT);
        typeDeclaration.setKind(TypeDeclaration.Kind.CLASS);
        typeDeclaration.setValid(true);
        typeDeclaration.setFormat(TypeDeclaration.Format.POJO);
        typeDeclaration.setNature(TypeDeclaration.Nature.DECLARATION);
        TypeMetaInfo info = new TypeMetaInfo(typeDeclaration);

        TypeDeclaration typeDec = new TypeDeclaration();
        typeDec.setRole(Role.Type.FACT);
        typeDec.setKind(TypeDeclaration.Kind.ENUM);
        typeDec.setValid(true);
        typeDec.setFormat(TypeDeclaration.Format.TEMPLATE);
        typeDec.setNature(TypeDeclaration.Nature.DEFINITION);
        TypeMetaInfo infoTwo = new TypeMetaInfo(typeDec);

        Map<String, TypeMetaInfo> typesMetaInfos = new HashMap<>();
        typesMetaInfos.put("idk",
                           info);
        typesMetaInfos.put("idkTwo",
                           infoTwo);

        //call the class under test
        helper.shareTypesMetaInfoWithMap(typesMetaInfos,
                                         kieMap,
                                         compilationID,
                                         log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append(TypeMetaInfo.class.getName());
        Set<String> typesMetaInfosSet = (Set) kieMap.get(sbTypes.toString());
        assertThat(typesMetaInfosSet).isNotNull();
        assertThat(typesMetaInfosSet).hasSize(1);
        assertThat(typesMetaInfosSet.iterator().next()).isEqualTo("idk");
    }

    @Test
    public void shareNoEventTypesFromMapTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);

        TypeDeclaration typeDeclaration = new TypeDeclaration();
        typeDeclaration.setRole(Role.Type.FACT);
        typeDeclaration.setKind(TypeDeclaration.Kind.CLASS);
        typeDeclaration.setValid(true);
        typeDeclaration.setFormat(TypeDeclaration.Format.POJO);
        typeDeclaration.setNature(TypeDeclaration.Nature.DECLARATION);
        TypeMetaInfo info = new TypeMetaInfo(typeDeclaration);

        TypeDeclaration typeDec = new TypeDeclaration();
        typeDec.setRole(Role.Type.FACT);
        typeDec.setKind(TypeDeclaration.Kind.ENUM);
        typeDec.setValid(true);
        typeDec.setFormat(TypeDeclaration.Format.TEMPLATE);
        typeDec.setNature(TypeDeclaration.Nature.DEFINITION);
        TypeMetaInfo infoTwo = new TypeMetaInfo(typeDec);

        Map<String, TypeMetaInfo> typesMetaInfos = new HashMap<>();
        typesMetaInfos.put("idk",
                           info);
        typesMetaInfos.put("idkTwo",
                           infoTwo);

        //call the class under test
        helper.shareTypesMetaInfoWithMap(typesMetaInfos,
                                         kieMap,
                                         compilationID,
                                         log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append(TypeMetaInfo.class.getName());
        Set<String> typesMetaInfosSet = (Set) kieMap.get(sbTypes.toString());
        assertThat(typesMetaInfosSet).isNull();
    }

    //Drools code to create a KieModule

    public static String generateBeansXML() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">\n" +
                "</beans>";
    }

    @Test
    public void shareNullTypesFromMapTest() {
        //prepare
        CompilerHelper helper = new CompilerHelper();
        Map<String, Object> kieMap = new HashMap<>();
        String compilationID = UUID.randomUUID().toString();
        kieMap.put("compilation.ID",
                   compilationID);

        //call the class under test
        helper.shareTypesMetaInfoWithMap(null,
                                         kieMap,
                                         compilationID,
                                         log);

        //verify
        StringBuilder sbTypes = new StringBuilder(compilationID).append(".").append(TypeMetaInfo.class.getName());
        Set<String> typesMetaInfosSet = (Set) kieMap.get(sbTypes.toString());
        assertThat(typesMetaInfosSet).isNull();
    }

    public KieModule createKieModule(String namespace,
                                     boolean createJar,
                                     String version) {
        KieModuleModel kproj = new KieModuleModelImpl();

        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel(namespace + ".KBase1")
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM)
                .addPackage(namespace + ".KBase1")
                .setDefault(true);

        kieBaseModel1.newKieSessionModel(namespace + ".KSession1")
                .setType(KieSessionModel.KieSessionType.STATELESS)
                .setClockType(ClockTypeOption.get("realtime"))
                .setDefault(true);

        kieBaseModel1.newKieSessionModel(namespace + ".KSession2")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kieBaseModel1.newKieSessionModel(namespace + ".KSessionDefault")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"))
                .setDefault(true);

        KieBaseModel kieBaseModel2 = kproj.newKieBaseModel(namespace + ".KBase2")
                .setEqualsBehavior(EqualityBehaviorOption.IDENTITY)
                .addPackage(namespace + ".KBase2")
                .setEventProcessingMode(EventProcessingOption.CLOUD);

        kieBaseModel2.newKieSessionModel(namespace + ".KSession3")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        KieServices ks = KieServices.Factory.get();

        KieFileSystemImpl kfs = (KieFileSystemImpl) ks.newKieFileSystem();
        kfs.write("src/main/resources/META-INF/beans.xml",
                  generateBeansXML());
        kfs.writeKModuleXML(((KieModuleModelImpl) kproj).toXML());

        ReleaseId releaseId = ks.newReleaseId(namespace,
                                              "art1",
                                              version);
        kfs.generateAndWritePomXML(releaseId);

        String kBase1R1 = getRule(namespace + ".test1",
                                  "rule1",
                                  version);
        String kBase1R2 = getRule(namespace + ".test1",
                                  "rule2",
                                  version);

        String kbase2R1 = getRule(namespace + ".test2",
                                  "rule1",
                                  version);
        String kbase2R2 = getRule(namespace + ".test2",
                                  "rule2",
                                  version);

        String fldKB1 = "src/main/resources/" + kieBaseModel1.getName().replace('.',
                                                                                '/');
        String fldKB2 = "src/main/resources/" + kieBaseModel2.getName().replace('.',
                                                                                '/');

        kfs.write(fldKB1 + "/rule1.drl",
                  kBase1R1.getBytes());
        kfs.write(fldKB1 + "/rule2.drl",
                  kBase1R2.getBytes());
        kfs.write(fldKB2 + "/rule1.drl",
                  kbase2R1.getBytes());
        kfs.write(fldKB2 + "/rule2.drl",
                  kbase2R2.getBytes());

        kfs.write("src/main/java/org/drools/compiler/cdi/test/KProjectTestClass" + namespace + ".java",
                  generateKProjectTestClass(kproj,
                                            namespace));

        KieBuilder kBuilder = ks.newKieBuilder(kfs);

        kBuilder.buildAll(DrlProject.class);
        MemoryKieModule kieModule = (MemoryKieModule) kBuilder.getKieModule();
        return kieModule;
    }

    public String getRule(String packageName,
                          String ruleName,
                          String version) {
        String s = "package " + packageName + "\n" +
                "global java.util.List list;\n" +
                "rule " + ruleName + " when \n" +
                "then \n" +
                "  list.add(\"" + packageName + ":" + ruleName + ":" + version + "\"); " +
                "end \n" +
                "";
        return s;
    }

    public String generateKProjectTestClass(KieModuleModel kproject,
                                            String namespace) {

        return "package org.drools.compiler.cdi.test;\n" +
                "import javax.inject.Named;\n" +
                "import javax.inject.Inject;\n" +
                "import javax.inject.Inject;\n" +
                "import javax.enterprise.event.Observes;\n" +
                "import " + KieBase.class.getName() + ";\n" +
                "import " + KieSession.class.getName() + ";\n" +
                "import " + StatelessKieSession.class.getName() + ";\n" +
                "import " + org.kie.api.cdi.KBase.class.getName() + ";\n" +
                "import " + org.kie.api.cdi.KSession.class.getName() + ";\n" +
                "import " + KPTest.class.getName() + ";\n" +
                "import " + KProjectTestClass.class.getName() + ";\n" +

                "@KPTest(\"" + namespace + "\") \n" +
                "public class KProjectTestClass" + namespace + " implements KProjectTestClass {\n" +
                "    private @Inject @KBase(\"" + namespace + ".KBase1\")  " +
                "    KieBase kBase1; \n" +
                "    public KieBase getKBase1() {\n" +
                "        return kBase1;\n" +
                "    }\n" +
                "    private @Inject @KBase(\"" + namespace + ".KBase2\") " +
                "    KieBase kBase2; \n" +
                "    public KieBase getKBase2() {\n" +
                "        return kBase2;\n" +
                "    }\n" +
                "    private @Inject @KBase(\"" + namespace + ".KBase3\") \n" +
                "    KieBase kBase3; \n" +
                "    public KieBase getKBase3() {\n" +
                "        return kBase3;\n" +
                "    }\n" +
                "    private @Inject @KSession(\"" + namespace + ".KSession1\") StatelessKieSession kBase1kSession1; \n" +
                "    public StatelessKieSession getKBase1KSession1() {\n" +
                "        return kBase1kSession1;\n" +
                "    }\n" +
                "    private @Inject @KSession(\"" + namespace + ".KSession2\") KieSession kBase1kSession2; \n" +
                "    public KieSession getKBase1KSession2() {\n" +
                "        return kBase1kSession2;\n" +
                "    }\n" +
                "    private @Inject @KSession(\"" + namespace + ".KSession3\") KieSession kBase2kSession3; \n" +
                "    public KieSession getKBase2KSession3() {\n" +
                "        return kBase2kSession3;\n" +
                "    }\n" +
                "    private @Inject @KSession(\"" + namespace + ".KSession4\") StatelessKieSession kBase3kSession4; \n" +
                "    public StatelessKieSession getKBase3KSession4() {\n" +
                "        return kBase3kSession4;\n" +
                "    }\n" +
                "}\n";
    }
}
