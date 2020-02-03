package org.kie.maven.plugin;

import org.apache.maven.plugin.logging.Log;

public class DroolsExecModelConsistency {

    public static boolean shouldGenerateModel(boolean modelParameterEnabled, boolean modelCompilerInClassPath, Log log) {

        boolean cannotEnableExecutableModel = modelParameterEnabled && !modelCompilerInClassPath;

        if (modelParameterEnabled && cannotEnableExecutableModel) {
            log.warn("You're trying to use Drools with the executable model without providing it on the classpath");
            log.warn("This will fail at compile time, therefore the Executable Model generation is disabled");
            log.warn("Add the drools-model-compiler artifact to the pom.xml to enable the executable model");
        }

        return modelParameterEnabled && modelCompilerInClassPath;
    }
}
