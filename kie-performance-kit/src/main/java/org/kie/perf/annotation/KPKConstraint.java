package org.kie.perf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface KPKConstraint {

    /**
     * List of constraints in the form propertyName=expectedValue. E.g.
     * @KPKConstraint(["jbpm.runtimeManagerStrategy=PerProcessInstance"])
     */
    String[] value();

}
