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

package org.kie.server.controller.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.builder.ContainerBuilder;
import org.kie.server.controller.log.LogHelper;
import org.kie.server.controller.validate.NotDuplicateValidStrings;
import org.kie.server.controller.validate.ViolationHelper;

/**
 * Create new server template
 */
@Mojo( name = "create-template", defaultPhase = LifecyclePhase.DEPLOY, threadSafe = true, requiresProject = false)
public class CreateTemplateMojo extends KieControllerMojo {

    public final static String[] TEMPLATE_CAPABILITIES = { "RULE", "PROCESS", "PLANNING" };

    @Parameter(property = "kie-ctrl.template-id", required = true)
    private String templateId;

    @Parameter(property = "kie-ctrl.template-name")
    private String templateName;

    @Parameter(property = "kie-ctrl.capabilities")
    @NotDuplicateValidStrings(admittedValues = { "RULE", "PROCESS", "PLANNING" }, message = "Capabilities not valid")
    private List<String> capabilities;

    @Parameter(property = "kie-ctrl.containers")
    @Valid
    private List<ContainerBuilder> containers = new ArrayList<>();

    @Override
    public void executeCommand() throws MojoExecutionException, MojoFailureException {

        if (templateName == null || templateName.isEmpty()) {
            templateName = templateId;
        }

        if (capabilities == null) {
            capabilities = Arrays.asList(TEMPLATE_CAPABILITIES);
        }

        getLog().info("Server Template Id: " + templateId);
        getLog().info("Server Template Name: " + templateName);
        getLog().info("Capabilities: " + capabilities);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CreateTemplateMojo>> violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new MojoExecutionException(ViolationHelper.printViolations(violations));
        }

        ServerTemplate serverTemplate = new ServerTemplate();
        serverTemplate.setId(templateId);
        serverTemplate.setName(templateName);
        serverTemplate.setCapabilities(capabilities);

        List<ContainerSpec> containerSpecs = containers.stream()
                .map(containerBuilder -> containerBuilder.build(templateId ,templateName))
                .collect(Collectors.toList());

        LogHelper.logContainers(getLog(), containerSpecs);

        serverTemplate.setContainersSpec(containerSpecs);
        kieControllerGateway.createServerTemplate(serverTemplate);

        getLog().info("Server template " + templateId + " CREATED");

    }

}
