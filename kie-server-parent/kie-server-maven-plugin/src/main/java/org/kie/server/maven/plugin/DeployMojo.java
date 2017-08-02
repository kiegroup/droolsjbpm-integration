/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kie.internal.runtime.conf.RuntimeStrategy;

/**
 * Deploy the k-jar artifact
 */
@Mojo( name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = true)
public class DeployMojo extends KieServerAbstactMojo {

    @Parameter(property = "deploy.runtime-strategy", readonly = true)
    private String runtimeStrategy;

    @Override
    public void excecuteCommand() throws MojoExecutionException, MojoFailureException {

        // using k-jar kie-deployment-descriptor runtime strategy
        if (runtimeStrategy == null) {

            getLog().info("Using deployment descriptor runtime strategy");
            kieServerGateway.deploy(project, container, null);
            return;

        }

        // override runtime strategy
        RuntimeStrategy strategy;

        try {
            strategy = RuntimeStrategy.valueOf(runtimeStrategy);
        } catch (IllegalArgumentException ex) {
            // if runtime strategy value is not valid
            throw new MojoFailureException("Runtime Strategy Not Found: " + runtimeStrategy);
        }

        getLog().info("Override Runtime Strategy: " + runtimeStrategy);
        kieServerGateway.deploy(project, container, strategy);

    }

}
