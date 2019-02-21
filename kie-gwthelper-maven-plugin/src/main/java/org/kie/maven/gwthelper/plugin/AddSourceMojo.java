/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.maven.gwthelper.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Add more source directories to the POM.
 */
@Mojo(name = "add-source", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class AddSourceMojo  extends AbstractMojo {

    private final static String SRC_MAIN_JAVA = "/src/main/java".replace("/", System.getProperty("file.separator"));
    private final static String SRC_MAIN_RESOURCES = "/src/main/resources".replace("/", System.getProperty("file.separator"));

    /**
     * Comma-separated additional source directories.
     * @since 0.1
     */
    @Parameter(required = true)
    private String rootDirectories;

    /**
     * Comma-separated pattern to match for including modules.
     * Does not use regex, but simple string
     * @since 0.1
     */
    @Parameter(required = false)
    private String includes;

    /**
     * Comma-separated pattern to match for excluding modules.
     * Does not use regex, but simple string
     * @since 0.1
     */
    @Parameter(required = false)
    private String excludes;

    /**
     * @since 0.1
     */
    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        if (StringUtils.isNotEmpty(includes) && StringUtils.isNotEmpty(excludes)) {
            throw new MojoExecutionException("Only one of 'includes' or 'excludes' can be provided");
        }
        String[] rootDirectoryPaths = rootDirectories.split(",");
        for (String rootDirectoryPath : rootDirectoryPaths) {
            File rootDirectory = new File(rootDirectoryPath);
            loopForMavenModules(rootDirectory);
        }
    }

    /**
     * Method to recursively <i>filter</i> only <b>maven</b> directories, i.e. directories containing a <b>pom.xml</b> file
     * @param source
     * @throws MojoExecutionException
     */
    private void loopForMavenModules(File source) throws MojoExecutionException {
        checkReadableDirectory(source);
        if (isMavenModule(source)) {
            getLog().debug("mavenModule " + source.getAbsolutePath());
            loopForGwtModule(source);
            List<File> sources = Arrays.asList(source.listFiles());
            for (File file : sources) {
                if (file.isDirectory()) {
                    loopForMavenModules(file);
                }
            }
        }
    }

    /**
     * Method to recursively <i>filter</i> only <b>gwt</b> directories, i.e. directories containing a <b>/src/main/resources/(..)/(..).gwt.xml</b> file
     * @param source
     * @throws MojoExecutionException
     */
    private void loopForGwtModule(File source) throws MojoExecutionException {
        getLog().debug("loopForGwtModule " + source.getAbsolutePath());
        if (isValidGwtModule(source)) {
            File sources = new File(source.getAbsolutePath() + SRC_MAIN_JAVA);
            checkReadableDirectory(sources);
            this.project.addCompileSourceRoot(sources.getAbsolutePath());
            if (getLog().isInfoEnabled()) {
                getLog().info("Source directory: " + sources + " added.");
            }
            File resources = new File(source.getAbsolutePath() + SRC_MAIN_RESOURCES);
            this.project.addCompileSourceRoot(resources.getAbsolutePath());
            if (getLog().isInfoEnabled()) {
                getLog().info("Source directory: " + resources + " added.");
            }
        }
    }

    /**
     * Method to check if in the given directory is a <i>valid</i> <b>Gwt</b> module, i.e. it contains a "src/main/resources/./.gwt.xml" file
     * eventually matching the <b>includes/excludes</b> patterns
     * @param toCheck
     * @return
     * @throws MojoExecutionException
     */
    private boolean isValidGwtModule(File toCheck) throws MojoExecutionException {
        getLog().debug("isValidGwtModule " + toCheck.getAbsolutePath());
        File resources = new File(toCheck.getAbsolutePath() + SRC_MAIN_RESOURCES);
        if (!resources.exists()) {
            return false;
        }
        boolean toReturn = false;
        try {
            final Optional<Path> first = Files.walk(Paths.get(resources.getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".gwt.xml")).findFirst();
            if (first.isPresent()) {
                String fileName = first.get().getFileName().toString();
                if (StringUtils.isNotEmpty(includes)) {
                    toReturn = matchPattern(fileName, includes);
                } else if (StringUtils.isNotEmpty(excludes)) {
                    toReturn = !matchPattern(fileName, excludes);
                } else {
                    toReturn = true;
                }
            }
        } catch (IOException e) {
            String errorMessage = StringUtils.isEmpty(e.getMessage()) ? e.getClass().getName() : e.getMessage();
            errorMessage += " while analyzing " + toCheck.getAbsolutePath();
            throw new MojoExecutionException(errorMessage);
        }
        return toReturn;
    }

    /**
     * Method to check if the given directory is a <b>Maven</b> module (it contains <b>pom.xml</b>)
     * @param toCheck
     * @return
     */
    private boolean isMavenModule(File toCheck) {
        return toCheck.isDirectory() && toCheck.list() != null && Arrays.asList(toCheck.list()).contains("pom.xml");
    }

    /**
     * Method to check if the given String contains one of the comma-separated pattern.
     * Matching is done with String.contains()
     * @param toCheck
     * @param pattern
     * @return
     */
    private boolean matchPattern(String toCheck, String pattern) {
        return Arrays.stream(pattern.split(",")).anyMatch(toCheck::contains);
    }

    /**
     * Method to check if the given file is an <b>existing, readable, directory</b>
     * @param toCheck
     * @throws MojoExecutionException if check fails
     */
    private void checkReadableDirectory(File toCheck) throws MojoExecutionException {
        if (!toCheck.exists() || !toCheck.canRead() || !toCheck.isDirectory()) {
            throw new MojoExecutionException("Directory " + toCheck.getAbsolutePath() + " is not a readable directory");
        }
    }
}
