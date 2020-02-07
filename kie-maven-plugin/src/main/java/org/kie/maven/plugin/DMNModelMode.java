package org.kie.maven.plugin;

import static java.util.Arrays.asList;

public enum DMNModelMode {

    YES,
    NO;

    public static boolean modelParameterEnabled(String s) {
        return asList(YES).contains(valueOf(s.toUpperCase()));
    }
}

