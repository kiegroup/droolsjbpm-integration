package org.kie.aries.blueprint.helpers;

import org.kie.api.runtime.Environment;

public class JPAPlaceholderResolverStrategyHelper {
    Environment environment;

    public JPAPlaceholderResolverStrategyHelper(Environment environment) {
        this.environment = environment;
    }

    public JPAPlaceholderResolverStrategyHelper() {
    }

    public Environment getEnvironment() {
        return environment;
    }
}
