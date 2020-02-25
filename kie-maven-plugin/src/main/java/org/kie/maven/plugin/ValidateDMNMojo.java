/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.maven.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.core.assembler.DMNAssemblerService;
import org.kie.dmn.core.compiler.DMNProfile;
import org.kie.dmn.feel.util.ClassLoaderUtil;
import org.kie.dmn.model.api.DMNModelInstrumentedBase;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.validation.DMNValidator;
import org.kie.dmn.validation.DMNValidator.Validation;
import org.kie.dmn.validation.DMNValidatorFactory;
import org.kie.internal.utils.ChainedProperties;

@Mojo(name = "validateDMN",
      requiresDependencyResolution = ResolutionScope.NONE,
      requiresProject = true,
      defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ValidateDMNMojo extends AbstractKieMojo {

    @Parameter(required = true, defaultValue = "${project.build.resources}")
    private List<Resource> resources;

    @Parameter
    private Map<String, String> properties;

    @Parameter(required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(property = "validateDMN", defaultValue = "VALIDATE_SCHEMA,VALIDATE_MODEL")
    private String validateDMN;
    private List<Validation> actualFlags = new ArrayList<>();

    private List<Path> resourcesPaths;
    private DMNValidator validator;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        actualFlags.addAll(computeFlagsFromCSVString(validateDMN));
        // for this phase, keep only the following flags (the rest requires the BuildMojo).
        actualFlags.retainAll(Arrays.asList(Validation.VALIDATE_SCHEMA, Validation.VALIDATE_MODEL));
        if (actualFlags.isEmpty()) {
            getLog().info("No VALIDATE_SCHEMA or VALIDATE_MODEL flags set, skipping.");
            return;
        }

        resourcesPaths = resources.stream().map(r -> new File(r.getDirectory()).toPath()).collect(Collectors.toList());
        if (getLog().isDebugEnabled()) {
            getLog().debug("resourcesPaths: " + resourcesPaths.stream().map(Path::toString).collect(Collectors.joining(",\n")));
        }

        List<Path> dmnModelPaths = computeDmnModelPaths();
        if (dmnModelPaths.isEmpty()) {
            getLog().info("No DMN Models found.");
            return;
        }

        getLog().info("Initializing DMNValidator...");
        initializeDMNValidator();
        getLog().info("DMNValidator initialized.");

        dmnModelPaths.forEach(x -> getLog().info("Will validate DMN model: " + x.toString()));
        List<DMNMessage> validation = validator.validateUsing(actualFlags.toArray(new Validation[]{}))
                                               .theseModels(dmnModelPaths.stream().map(Path::toFile).collect(Collectors.toList()).toArray(new File[]{}));
        logValidationMessages(validation);
        if (validation.stream().anyMatch(m -> m.getLevel() == Level.ERROR)) {
            throw new MojoFailureException("There are DMN Validation Error(s).");
        }
    }

    private void logValidationMessages(List<DMNMessage> validation) {
        for (DMNMessage msg : validation) {
            Consumer<CharSequence> logFn = null;
            switch (msg.getLevel()) {
                case ERROR:
                    logFn = getLog()::error;
                    break;
                case WARNING:
                    logFn = getLog()::warn;
                    break;
                case INFO:
                default:
                    logFn = getLog()::info;
                    break;
            }
            StringBuilder sb = new StringBuilder();
            if (msg.getSourceReference() instanceof DMNModelInstrumentedBase) {
                DMNModelInstrumentedBase ib = (DMNModelInstrumentedBase) msg.getSourceReference();
                while (ib.getParent() != null) {
                    ib = ib.getParent();
                }
                if (ib instanceof Definitions) {
                    sb.append(((Definitions) ib).getName() + ": ");
                }
            }
            sb.append(msg.getText());
            logFn.accept(sb.toString());
        }
    }

    public List<Validation> computeFlagsFromCSVString(String csvString) {
        List<Validation> flags = new ArrayList<>();
        boolean resetFlag = false;
        for (String p : csvString.split(",")) {
            try {
                flags.add(Validation.valueOf(p));
            } catch (IllegalArgumentException e) {
                getLog().info("validateDMN configured with flag: '" + p + "' determines this Mojo will not be executed (reset all flags).");
                resetFlag = true;
            }
        }
        if (resetFlag) {
            flags.clear();
        }
        return flags;
    }

    private List<Path> computeDmnModelPaths() throws MojoExecutionException {
        List<Path> dmnModelPaths = new ArrayList<>();
        for (Path p : resourcesPaths) {
            getLog().info("Looking for DMN models in path: " + p);
            try (Stream<Path> walk = Files.walk(p)) {
                walk.filter(f -> f.toString().endsWith(".dmn"))
                    .forEach(dmnModelPaths::add);
            } catch (Exception e) {
                throw new MojoExecutionException("Failed executing ValidateDMNMojo", e);
            }
        }
        return dmnModelPaths;
    }

    private void initializeDMNValidator() throws MojoExecutionException {
        ClassLoader classLoader = ClassLoaderUtil.findDefaultClassLoader();
        ChainedProperties chainedProperties = ChainedProperties.getChainedProperties(classLoader);
        List<KieModuleModel> kieModules = new ArrayList<>();
        for (Path p : resourcesPaths) {
            try (Stream<Path> walk = Files.walk(p)) {
                List<Path> collect = walk.filter(f -> f.toString().endsWith("kmodule.xml")).collect(Collectors.toList());
                for (Path k : collect) {
                    kieModules.add(KieModuleModelImpl.fromXML(k.toFile()));
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Failed executing ValidateDMNMojo", e);
            }
        }
        for (KieModuleModel kmm : kieModules) {
            Properties ps = new Properties();
            ps.putAll(kmm.getConfigurationProperties());
            chainedProperties.addProperties(ps);
        }
        List<DMNProfile> dmnProfiles = new ArrayList<>();
        dmnProfiles.addAll(DMNAssemblerService.getDefaultDMNProfiles(chainedProperties));
        try {
            Map<String, String> dmnProfileProperties = new HashMap<>();
            chainedProperties.mapStartsWith(dmnProfileProperties, DMNAssemblerService.DMN_PROFILE_PREFIX, false);
            for (Map.Entry<String, String> dmnProfileProperty : dmnProfileProperties.entrySet()) {
                DMNProfile dmnProfile = (DMNProfile) classLoader.loadClass(dmnProfileProperty.getValue()).newInstance();
                dmnProfiles.add(dmnProfile);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MojoExecutionException("Failed DMNValidator initialization.", e);
        }
        validator = DMNValidatorFactory.newValidator(dmnProfiles);
    }
}

