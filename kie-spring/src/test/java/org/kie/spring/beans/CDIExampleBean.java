package org.kie.spring.beans;

import org.kie.api.cdi.KSession;
import org.kie.api.runtime.StatelessKieSession;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CDIExampleBean {

    @Inject
    @KSession("ksession1")
    StatelessKieSession kieSession;

    @Inject
    BeanManager beanManager;

    public void executeRules() {
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
        } catch (NamingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println(ctx);
    }

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public StatelessKieSession getKieSession() {
        return kieSession;
    }

    public void setKieSession(StatelessKieSession kieSession) {
        this.kieSession = kieSession;
    }
}
