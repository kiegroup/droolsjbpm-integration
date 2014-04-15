package org.kie.spring;

import java.util.Map;

import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.ObjectModelResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringObjectModelResolver implements ObjectModelResolver, ApplicationContextAware {

    private static final String ID = "spring";

    private ApplicationContext applicationContext;

    @Override
    public Object getInstance(ObjectModel model, ClassLoader cl, Map<String, Object> contextParams) {
        if (applicationContext == null) {
            throw new IllegalStateException("No spring application context provided");
        }
        return applicationContext.getBean(model.getIdentifier());
    }

    @Override
    public boolean accept(String resolverId) {
        if (ID.equals(resolverId)) {
            return true;
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (this.applicationContext == null) {
            this.applicationContext = applicationContext;
        }
    }
}
