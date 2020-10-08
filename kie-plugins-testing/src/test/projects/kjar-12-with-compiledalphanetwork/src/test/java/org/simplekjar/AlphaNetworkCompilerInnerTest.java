///*
// * Copyright 2018 Red Hat, Inc. and/or its affiliates.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.simplekjar;
//
//import java.lang.reflect.Constructor;
//import java.util.List;
//
//import org.drools.ancompiler.CompiledNetwork;
//import org.drools.ancompiler.ObjectTypeNodeCompiler;
//import org.drools.core.impl.InternalKnowledgeBase;
//import org.drools.core.reteoo.ObjectSinkPropagator;
//import org.drools.core.reteoo.ObjectTypeNode;
//import org.drools.core.reteoo.Rete;
//import org.junit.Test;
//import org.kie.api.KieServices;
//import org.kie.api.runtime.KieContainer;
//import org.kie.api.runtime.KieSession;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class AlphaNetworkCompilerInnerTest  {
//
//    @Test
//    public void testAlphaNetworkCompiler() throws Exception {
//
//        KieServices ks = KieServices.Factory.get();
//        KieContainer kieContainer = ks.getKieClasspathContainer();
//
//        KieSession kSession = null;
//        try {
//
//            kSession = kieContainer.newKieSession("kbase-compiled-alphanetwork.session");
//
//            ClassLoader classLoader = kieContainer.getClassLoader();
//            Class<?> aClass = Class.forName("org.compiledalphanetwork.Person", true, classLoader);
//            Constructor<?> constructor = aClass.getConstructor(String.class);
//            Object lucaFactA = constructor.newInstance("Luca");
//
//            kSession.insert(lucaFactA);
//            int rulesFired = kSession.fireAllRules();
//            kSession.dispose();
//
//            assertEquals(1, rulesFired);
//
//            assertReteIsAlphaNetworkCompiled(kSession);
//
//        } finally {
//            kSession.dispose();
//        }
//    }
//
//    protected void assertReteIsAlphaNetworkCompiled(KieSession ksession) {
//        Rete rete = ((InternalKnowledgeBase) ksession.getKieBase()).getRete();
//        List<ObjectTypeNode> objectTypeNodes = ObjectTypeNodeCompiler.objectTypeNodes(rete);
//        for(ObjectTypeNode otn : objectTypeNodes) {
//            ObjectSinkPropagator objectSinkPropagator = otn.getObjectSinkPropagator();
//            System.out.println(objectSinkPropagator.getClass().getCanonicalName());
//            assertTrue(objectSinkPropagator instanceof CompiledNetwork);
//        }
//    }
//}
//
