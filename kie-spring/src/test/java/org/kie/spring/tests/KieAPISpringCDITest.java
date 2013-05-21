/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring.tests;

import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.kie.spring.beans.CDIExampleBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.enterprise.inject.spi.Bean;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class KieAPISpringCDITest {

    static ClassPathXmlApplicationContext context = null;
    static Weld w;
    static WeldContainer wc;

    // @BeforeClass
    public static void setup() {
        w = new Weld();
        wc = w.initialize();
        //context = new ClassPathXmlApplicationContext("org/kie/spring/kie-beans-cdi.xml");
    }

//    @Test
//    public void testKieBase() throws Exception {
//        KieBase kbase = (KieBase) context.getBean("drl_kiesample");
//        assertNotNull(kbase);
//    }

    //@Test
    public void testKieSession() throws Exception {
        //StatelessKieSession ksession = (StatelessKieSession) context.getBean("ksession1");
        CDIExampleBean bean = wc.instance().select(CDIExampleBean.class).get();
        assertNotNull(bean);
        bean.executeRules();
        System.out.println("bean.getBeanManager() == " + bean.getBeanManager());

        Set<Bean<?>> beanSet = bean.getBeanManager().getBeans(KieContainerImpl.class);
        for (Bean<?> cdiBean : beanSet) {
            System.out.println(" cdiBean == " + cdiBean);
        }

        System.out.println("beanSet.size() == " + beanSet.size());
        assertNotNull(beanSet);
        assertNotNull(bean.getKieSession());
        System.out.println(bean.getKieSession());
    }

//    @Test
//    public void testKSessionExecution() throws Exception {
//        StatelessKieSession ksession = (StatelessKieSession) context.getBean("ksession1");
//        assertNotNull(ksession);
//
//        Person person = new Person("HAL", 42);
//        person.setHappy(false);
//        ksession.execute(person);
//        assertTrue(person.isHappy());
//    }

    //@AfterClass
    public static void tearDown() {
        //context.destroy();
        w.shutdown();
    }

}
