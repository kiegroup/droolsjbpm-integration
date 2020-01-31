package org.kie.maven.plugin;

import static java.util.Arrays.asList;

public enum ExecModelMode {

    YES,
    NO,
    YES_WITHDRL,
    WITHMVEL,
    WITHDRL_MVEL;

    public static boolean shouldGenerateModel(String s) {
        return asList(YES, YES_WITHDRL, WITHMVEL, WITHDRL_MVEL).contains(valueOf(s.toUpperCase()));
    }

    public static boolean shouldValidateMVEL(String s) {
        return asList(WITHMVEL, WITHDRL_MVEL).contains(valueOf(s.toUpperCase()));
    }

    public static boolean shouldDeleteFile(String s) {
        return asList(YES, WITHMVEL).contains(valueOf(s.toUpperCase()));
    }
}

