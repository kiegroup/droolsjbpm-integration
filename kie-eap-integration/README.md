JBoss EAP/AS layer distributions builder
========================================

This project allows to create custom distributions for JBoss EAP/AS based on JBoss static modules.

The idea is to deploy some of the common resources that a single or several applications require in a static layer in JBoss EAP/AS container, then generate lightweight skinny WARs that depends on this layer.

For more information about JBoss EAP/AS modules refer to [JBoss community documentation](https://docs.jboss.org/author/display/MODULES/Home)

Table of contents
------------------

* **[Introduction](#introduction)**
* **[Strategy](#strategy)**
* **[Project Modules](#project-modules)**
* **[EAP Modules Plugin](#eap-modules-plugin)**
* **[Usage](#usage)**
* **[Patches](#patches)**
* **[Limitations](#limitations)**

Introduction
============

In general you can create a JBoss static module manually by basically defining its resources and its dependencies to other modules in a XML file.

But in large projects, where there is a lot of people continuously working on, it can be difficult to maintain the dependencies between the modules manually, so this plugin provides a way where the dependencies are almost automatically resolved using Maven definitions for the modules to generate.

This project basically provides all the module definitions and a Maven 3.0 plugin that allows to create the modules that will be contained in a JBoss EAP/AS static layer and the web applications that use them.

The use of this plugin has several advantages:

* Full Maven integration
* Extensible
    * Some services like module scanners or dependency/graph builder are Plexus services.
    * Support for implementing different types of graph (flat, tree, etc)
* It makes life easier to the developer
    * The developer only has to define which modules to create, the metadata for them and its resources, not its dependencies.
    * Allows the developer not to worry about module dependencies, they are resolved by the maven definitions and added automatically in the module descriptor generated.
    * Allows the developer not to worry about module dependencies to base JBoss EAP/AS base modules too, as the plugin resolves them.
* Build time error detection
    * Any change in project artifacts that can produce a missing module dependency is detected in the build (package) time, not in runtime as if you create the modules manually. The build fails if any dependence is not satisfied.
    * Detects versions mismatched for module artifacts
    * Detects project unresolved resources (defined as module resources)
    * Detect resource duplications in module definitions (including EAP base ones)
* Customization
    * Support for static (not automatically resolved) dependencies.
    * Support for static dependencies only when is building a given distribution.
    * Easy to add new distributions, just creating a pom file for each module

Strategy
========

In order to delegate to maven the modules dependency graph, the static modules to generate must be defined in a Maven format.

The way that each static module is defined is by a Maven POM file:

* Each pom artifact defines a JBoss static module
* JBoss static module meta-data (name, slot, etc) are Maven properties in the module pom file.
* JBoss static module resources are Maven dependencies in the module pom file.

Using this approach, the plugin can delegate to Maven the dependency graph generation and then, generate the modules layer dependencies.

* JBoss EAP/AS base layer support
    * Consider that a default JBoss EAP/AS installation already contains a static modules layer named <code>base</code>, which contains the container **shared** libraries.
    * The plugin resolves them too.
    * Each JBoss EAP/AS version has to be defined as a base version in the plugin, by generating a pom for each module.
    * In order to generate these EAP/AS module pom files, the plugin has a specific goal that read the module definitions and each resource in a jboss installation in order to generate the module pom files automatically.

Project Modules
===============

This project contains four modules:

* <code>kie-eap-modules</code> contains all module definitions.
    * <code>kie-eap-static-modules</code> contains the drools/jBPM static modules definitions for the static layer to generate.
    * <code>kie-jboss-eap-base-modules</code> contains the JBoss EAP/AS base modules definitions (pre-installed ones) for a given EAP/AS version.
    * <code>kie-eap-dynamic-modules</code> contains the dynamic modules. In this case, the drools/jBPM BPMS and BRMS webapp distributions.
* <code>kie-jboss-modules-plugin</code> contains the plugin sources.
* <code>kie-eap-distributions</code> contains the distributions to generate. There are two types of distributions:

    * Static layer distributions -> Are a set of static modules (AKA <code>layer</code>)

    * Webapp distributions -> Are web applications ready to deploy over a static layer distribution.

EAP Modules Plugin
==================

Introduction
------------
This section contains information about the <code>org.kie:jboss-modules</code> plugin.

The goal for this plugin is to generate static layer or webapp distributions by delegating to Maven all the dependency resolutions.

To generate a distribution, the plugin requires some inputs:

* *The distribution name*
    The distribution name is the name for the layer to generate.
* *The base modules - Maven module*
    All the JBoss EAP/AS base module definitions must be contained in a global maven module.
    The plugin uses this maven module to scan all the base module definitions (metadata, resources, etc).
    For each JBoss EAP/AS version, the base modules can differ, so exist a maven module that contains all base module definitions for each container version.
    Currently, the versions supported are <code>6.1.1</code> and <code>6.3.0</code>.            
* *The static modules*
    All the static module definitions that the generated layer will contain must be specified as project dependencies.
    All dependency artifacts of type <code>pom</code> will be considered static modules to add in the layer.
    The plugin uses this artifacts to scan all the static module definitions (metadata, resources, etc).

Using these inputs, the plugin:

1. *Scans for static module definitions*
    Read module names, slots, resources and other meta-information.

2. *Scans for base module definitions*
    Read module names, slots, resources and other meta-information.

3. *Generates the dependency graph for the modules*

4. *Resolve the module that contains each node in the dependency graph, then adds a dependency to this module*

5. *Generates the static modules graph with all resolutions performed*

6. *Generates the assembly descriptors for the layer to generate, based on the previous static module graph resolved*

Goals
-----

These are the plugin goals:

* **generate-eap-base-module-descriptors**   
    - Description
        - When a new JBoss EAP/AS version is released, this goal is used to generate the module definitions for all its base layer modules.
        - It takes the <code>EAP_ROOT</code> path as argument and scans for all <code>module.xml </code>inside <code>modules/system/layers/base</code> directory.   
        - For each resource defined in the <code>module.xml</code>, if the resource is type jar, scan the jar file in oder to look up for a <code>pom.xml</code> maven resource descriptor.    
            - If the resource contains a <code>pom.xml</code>, its parsed and used to generate the module definition.
            - If the resource does not contains a <code>pom.xml</code>, its filename parsed and used to generate the module definition.    
    - Configuration parameters
        <table border="1">
            <tr>
                <th>Parameter</th>
                <th>Allowed values</th>
                <th>Default value</th>
                <th>Required</th>
                <th>Description</th>
            </tr>
            <tr>
                <td>eapRootPath</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The root path for the JBoss EAP/AS installation to scan</td>
            </tr>
            <tr>
                <td>outputFilePath</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The full path for the generated output resources file</td>
            </tr>
            <tr>
                <td>eapName</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The name of the JBoss application server</td>
            </tr>
            <tr>
                <td>tempPath</td>
                <td>Any string</td>
                <td>java.io.tmpdir</td>
                <td>false</td>
                <td>The full path for the temporary files. If not set, the java.io.tmpdir will be used as default</td>
            </tr>
            <tr>
                <td>mavenModulesVersion</td>
                <td>Any string</td>
                <td>The current project version</td>
                <td>false</td>
                <td>The version for the maven modules to generate</td>
            </tr>
        </table>
    - Result   
        - A multi-module maven structure will be generated in directory as specified by <code>outputFilePath</code> property.

* **static-layer-graph**   
    - Description
        - This goal is used to generate and print the distribution graph for the static module layer specified.
        - All static module definitions (pom type artifacts) to add in the generated layer graph must be added as current project dependencies.   
        - This distribution graph is generated for a given JBoss EAP/AS version, so the base module descriptors for the target version must be specified using plugin configuration parameter.      
    - Configuration parameters
        <table border="1">
            <tr>
                <th>Parameter</th>
                <th>Allowed values</th>
                <th>Default value</th>
                <th>Required</th>
                <th>Description</th>
            </tr>
            <tr>
                <td>distributionName</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The name of the JBoss EAP layer distrubtion to geneate</td>
            </tr>
            <tr>
                <td>baseModule</td>
                <td>A base module dependency</td>
                <td></td>
                <td>true</td>
                <td>The maven module that contains all base EAP/AS modules for a given version. It can contain exclusions</td>
            </tr>
            <tr>
                <td>staticDependencies</td>
                <td>A collection of static dependencies only for this distribution</td>
                <td></td>
                <td>false</td>
                <td>The static dependencies for this distribution modules</td>
            </tr>
            <tr>
                <td>graphOutputFile</td>
                <td>Any string</td>
                <td></td>
                <td>false</td>
                <td>The file to print the generated distribution graph</td>
            </tr>
            <tr>
                <td>includeOptionalDependencies</td>
                <td>Boolean value</td>
                <td>false</td>
                <td>false</td>
                <td>The flag that indicates if the optional dependencies must be scanned in the current project dependency tree</td>
            </tr>
            <tr>
                <td>failOnMissingDependency</td>
                <td>Boolean value</td>
                <td>true</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a dependency to a module resource is not satisfied</td>
            </tr>
            <tr>
                <td>failOnUnresolvableResource</td>
                <td>Boolean value</td>
                <td>true</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a module resource cannot be resolved in current project dependency tree</td>
            </tr>
            <tr>
                <td>failOnVersionMismatchedResource</td>
                <td>Boolean value</td>
                <td>false</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a module version for a resource is not resolvable in current project dependencies</td>
            </tr>
        </table>
    - Result   
        - The distribution graph for the static module layer specified is printed in system output and in the file specified in property <code>graphOutputFile</code>, if any.   

* **build-static-layer**   
    - Description
        - This goal is used to generate the distribution ZIP file for the static module layer specified.
        - All static module definitions (pom type artifacts) to add in the generated layer must be added as current project dependencies.   
        - This distribution is generated for a given JBoss EAP/AS version, so the base module descriptors for the target version must be specified using plugin configuration parameter.      
    - Configuration parameters
        <table border="1">
            <tr>
                <th>Parameter</th>
                <th>Allowed values</th>
                <th>Default value</th>
                <th>Required</th>
                <th>Description</th>
            </tr>
            <tr>
                <td>distributionName</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The name of the JBoss EAP layer distrubtion to geneate</td>
            </tr>
            <tr>
                <td>baseModule</td>
                <td>A base module dependency</td>
                <td></td>
                <td>true</td>
                <td>The maven module that contains all base EAP/AS modules for a given version. It can contain exclusions</td>
            </tr>
            <tr>
                <td>staticDependencies</td>
                <td>A collection of static dependencies only for this distribution</td>
                <td></td>
                <td>false</td>
                <td>The static dependencies for this distribution modules</td>
            </tr>
            <tr>
                <td>graphOutputFile</td>
                <td>Any string</td>
                <td></td>
                <td>false</td>
                <td>The file to print the generated distribution graph</td>
            </tr>
            <tr>
                <td>includeOptionalDependencies</td>
                <td>Boolean value</td>
                <td>false</td>
                <td>false</td>
                <td>The flag that indicates if the optional dependencies must be scanned in the current project dependency tree</td>
            </tr>
            <tr>
                <td>failOnMissingDependency</td>
                <td>Boolean value</td>
                <td>true</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a dependency to a module resource is not satisfied</td>
            </tr>
            <tr>
                <td>failOnUnresolvableResource</td>
                <td>Boolean value</td>
                <td>true</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a module resource cannot be resolved in current project dependency tree</td>
            </tr>
            <tr>
                <td>failOnVersionMismatchedResource</td>
                <td>Boolean value</td>
                <td>false</td>
                <td>false</td>
                <td>The flag that indicates if the build must fail when a module version for a resource is not resolvable in current project dependencies</td>
            </tr>
            <tr>
                <td>outputPath</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The output path for the genrated module descriptor and assembly files. The resulting assembly descriptor file will be created in this path</td>
            </tr>
            <tr>
                <td>assemblyFormats</td>
                <td>Comma separated assembly formats</td>
                <td>dir,zip</td>
                <td>false</td>
                <td>The output formats for assembly descriptor. Use comma-separated values</td>
            </tr>
        </table>
    - Result   
        - The distribution graph for the static module layer specified is printed in system output and in the file specified in property <code>graphOutputFile</code>, if any.
        - The resulting distribution files and resources, such as <code>module.xml</code> descriptors or module jars, are generated in the directory specified in property <code>outputPath</code>.
        - Inside this directory, the assembly descriptor for this distribution is generated in the following file path: <code>static-modules/${distributionName}/${distributionName}-assembly.xml</code>    

* **build-dynamic-modules**   
    - Description
        - This goal is used to generate the skinny WAR files for a given static layer distribution. 
        - All the static module resources that the web application depends are excluded from the skinny generated artifact and the modules added as dependencies in the <code>jboss-deployment-descriptor.xml</code> file.      
        - If the web application depends on another web application, the <code>jboss-all.xml</code> is added into the generated skinny artifact.      
    - Configuration parameters
        <table border="1">
            <tr>
                <th>Parameter</th>
                <th>Allowed values</th>
                <th>Default value</th>
                <th>Required</th>
                <th>Description</th>
            </tr>
            <tr>
                <td>distributionName</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The name of the dynamic distrubtion to geneate</td>
            </tr>
            <tr>
                <td>outputPath</td>
                <td>Any string</td>
                <td></td>
                <td>true</td>
                <td>The output path for the generated artifacts and assembly files. The resulting assembly.xml file will be created inside this path</td>
            </tr>
            <tr>
                <td>staticLayerArtifact</td>
                <td>The static layer JAR artifact</td>
                <td></td>
                <td>true</td>
                <td>The static layer artifact than contains all the modules and properties for the layer</td>
            </tr>
            <tr>
                <td>assemblyFormats</td>
                <td>Comma separated assembly formats</td>
                <td>dir,war</td>
                <td>false</td>
                <td>The output formats for assembly descriptor. Use comma-separated values</td>
            </tr>
        </table>
    - Result   
        - The resulting web applications are generated in the directory specified in property <code>outputPath</code>.
        - Inside this directory, the assembly descriptor for each web application is generated in the following file path: <code>dynamic-modules/${distributionName}/${dynamnic-module-name}-assembly.xml</code>

Usage
=====

This section contains information about:

* Profiles
* Distributions generation
* Adding static module dependencies
* Adding static dependencies only in a certain distribution
* Adding new JBoss EAP/AS versions to support
* Adding other static layer definitions
* How to change the target JBoss EAP/AS version

Profiles
--------
These are the available profiles:   

* <code>bpms-layer</code>: Used to generate the BPMS layer distribution. It results in the BPMS static layer generated in a ZIP file.   
* <code>bpms-webapp</code>: Used to generate the BPMS webapp skinny WAR that works using the BPMS layer.   
* <code>brms-layer</code>: Used to generate the BRMS layer distribution. It results in the BRMS static layer generated in a ZIP file.   
* <code>brms-webapp</code>: Used to generate the BRMS webapp skinny WAR that works using the BRMS layer.   

Distributions generation
------------------------
This section describes how to generate the distributions.   

* **Generate ALL distributions** (No profile)   
If no profile enabled via profile identifier or via property, ALL modules and distributions are generated.   
Run <code>mvn clean install</code>   

* **Generate BPMS Layer**
Generates the BPMS layer ZIP   
Run <code>mvn clean install -Dbpms-layer</code>   

* **Generate BPMS Layer and BPMS webapp**
Generates the BPMS layer ZIP and the skinny WAR files for kie-wb-webapp and jbpm-dashbuilder web applications.   
Run <code>mvn clean install -Dbpms-layer -Dbpms-webapp</code>   

* **Generate BRMS Layer**
Generates the BRMS layer ZIP   
Run <code>mvn clean install -Dbrms-layer</code>   

* **Generate BRMS Layer and BRMS webapp**
Generates the BPMS layer ZIP and the skinny WAR file for kie-drools-wb-webapp web application.   
Run <code>mvn clean install -Dbrms-layer -Dbrms-webapp</code>   

* **Generate the Base JBoss EAP/AS module descriptors**
Generates the base module definitions for all JBoss EAP/AS versions included.    
Run <code>mvn clean install -Deap-base-modules</code>   

* NOTE: All these commands must be run from <code>kie-eap-integration</code> root directory.   
* NOTE: This <code>kie-eap-integration</code> module is not build by default for <code>kie-wb-distributions</code> module build. To enable it, add the property <code>full</code>.   

How to add static dependencies
------------------------------

You can add custom module dependencies for a given static or dynamic module if Maven do not resolve them for any reason.

**Adding a dependency from a static module to another static module**   
This example adds a static dependency from module <code>org.jbpm</code> to module <code>org.drools</code>:   

1. Edit the pom file for the <code>org.jbpm</code> module, located at <code>kie-eap-modules/kie-eap-static-modules/org-jbpm</code>   
2. Add a new maven property:    
    - Named <code>module.dependencies</code>    
    - The value is a comma separated names of modules to depend on, in format <code>module:slot</code>. You can use maven properties as <code>${project.version}</code>    
    In this example: <code>&lt;module.dependencies&gt;org.drools:${project.version}&lt;/module.dependencies&gt;</code>    
3. Build and install the <code>org.jbpm</code> maven module.    
4. Build and install the static layer distribution and the web application distribution, if necessary.    

**Adding a dependency from a dynamic module to another dynamic module**    
Dynamic modules (EAR/WAR) can have dependencies to another dynamic modules too, by adding a <code>jboss-all.xml</code> descriptor file.        

This example adds a dependency from dynamic module <code>org.jbpm.dashbuilder</code> to module <code>org-kie-wb-webapp</code>:    

1. Edit the pom file for the <code>org.jbpm.dashbuilder</code> module, located at <code>kie-eap-modules/kie-eap-dynamic-modules/org-jbpm-dashbuilder-webapp</code>    
2. Add a new maven property:    
    - Named <code>module.add-jboss-all</code>    
    - Use <code>true</code> as value if you want to add the <code>jboss-all.xml</code> file in the generated webapp.   
    In this example: <code>&lt;module.add-jboss-all&gt;true&lt;/module.add-jboss-all&gt;</code>    
3. In the project that build these dynamic modules, add a maven property named <code>jboss-all-<dynamic_module_name></code>. The value for this property must be the name for the final assembled dependant webapp. In this example:        
    - Consider the final assembled skinny kie-wb-webapp will be named <code>kie-wb-webapp-modules.war</code>,   
    - Then, you must add a maven property as: <code>&lt;jboss-all-org.jbpm.dashbuilder&gt;kie-wb-webapp-modules.war&lt;/jboss-all-org.jbpm.dashbuilder&gt;</code>   
3. Build and install the <code>org.jbpm.dashbuilder</code> maven module.    
4. Build and install the dynamic layer distribution of this web application distribution.   

How to add static dependencies only in a certain distribution
-------------------------------------------------------------

If you define some static module dependencies in a module descriptor pom file, these static dependencies will be added for the module when building any distribution.    

There are some special cases where the user does not want to define the static dependency for the module descriptor itself, only when the module is added for a specific distribution.    

For example: imagine that module <code>com.opensymphony.quartz</code> must depend on <code>javax.api</code> (for any distribution) and on <code>org.jbpm</code> and <code>org.drools</code> (only when building the BPMS distribution).    

In this example, the user should add a static module dependency on <code>javax.api</code> in the <code>com.opensymphony.quartz</code> module descriptor pom file, and <code>org.jbpm</code> and <code>org.drools</code> static dependencies in the BPMS distribution pom file.    

In order to add static dependencies only for a given distribution:   
1. Edit the pom file for the distribution to build.    
2. Add a <code>staticDependency</code> tag for each module dependency you wanna add in the kie-jboss-modules plugin configuration section.   
   NOTE: If you want to add a dependency for ALL the modules in the distribuion, you can use the special keyword <code>ALL</code> for both <code>name</code> and <code>slot</code> tag values.   

Example:    
&lt;staticDependencies&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;Add&nbsp;a&nbsp;dependency&nbsp;to&nbsp;javax.api:main&nbsp;module&nbsp;for&nbsp;all&nbsp;static&nbsp;modules&nbsp;in&nbsp;this&nbsp;layer.&nbsp;--&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;staticDependency&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;ALL&lt;/name&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;slot&gt;ALL&lt;/slot&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;dependencies&gt;javax.api:main&lt;/dependencies&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/staticDependency&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;!--&nbsp;Add&nbsp;a&nbsp;dependency&nbsp;to&nbsp;org.jbpm:main,org.drools:main&nbsp;modules&nbsp;for&nbsp;com.opensymphony.quartz&nbsp;static&nbsp;module&nbsp;in&nbsp;this&nbsp;layer.&nbsp;--&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;staticDependency&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;com.opensymphony.quartz&lt;/name&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;slot&gt;main&lt;/slot&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;dependencies&gt;org.jbpm:main,org.drools:main&lt;/dependencies&gt;   
&nbsp;&nbsp;&nbsp;&nbsp;&lt;/staticDependency&gt;   
&lt;staticDependencies&gt;   

How to create a static module definition
----------------------------------------

If you want to add a new module, you have to create a new maven module in the <code>kie-eap-modules</code>:

* The module must be packaged as a pom artifact.
* The module dependencies represents the resources (JARs, etc) that will contain the generated JBoss static module.
* You must define some properties in the module pom that represents the static module meta-data.
* Add this new maven module in the parent <code>modules</code> section.

How to add JBoss EAP/AS base modules for a given version
--------------------------------------------------------

The plugin has a specific goal that scans a filesystem installation of a JBoss EAP/AS container:
* Look for all module descriptors in the <code>modules</code> directory for the JBoss container.
* For each descriptor, scan its resources. If the resource is a JAR, the JAR is scanned looking for maven metadata of this artifact.
* Generates all the maven modules that represent the JBoss base modules.

Then you can quickly add a new container version support.

In addition, once a new maven modules for a specific version are generated, you can add custom mappings by editing the generated pom files.

How to use another another static modules layer definition
----------------------------------------------------------

TODO

How to change the target JBoss EAP/AS version
----------------------------------------------

TODO

Patches
=======

Introduction
------------
* Patches are considered independent extensions for the jboss-modules plugin that allow to perform additional operations when creating the modules distribution.

* Patches are coded as Java files.

* Patches have a unique identifier.

* Patches are defined for:
  - A given JBoss EAP version.
  - A given static/dynamic module.

* When building the modules distribution, always an EAP version is indicated to the build process, so a patch is executed if:
 - The EAP version descriptor has the patch enabled for this version (patches are executed for a given EAP version, might not apply to all versions)
 - The static/dynamic module that is currently building have the patch definition entry in the module definition file.

* Patches have their own lifecycle, allowing the user to create new patches quickly and intercept in the build generation process.

Available patches
-----------------

These are the current coded and tested available patches.

**Servlet spec 3.0 - Webfragments**   
* Patch identifier: <code>dynamic.webfragment</code>      
* Is known that on both EAP 6.1.0. and 6.1.1 webfragment descriptors located inside custom static modules are not loaded.    
* If a dynamic module is build over a static layer, which has single or multiple modules that contain resource/s with webfragment descriptors, a new generated JAR file is added into the resulting webapp for each webfragment descriptor found.  
* So it consists on creating a new jar on runtime with a web-fragment descriptor. For each web-fragment descriptor a new jar is created and added into WEB-INF/lib of the webapp.   
* This method allows to not modify the original deployment descriptor (web.xml) of the webapp and use always the latest <code>webfragment.xml</code> file from the JAR, if the artifact is a snapshot.    

Other workarounds
-----------------

These ones are not patches themselves, they do not have any patch build file and modules are not referencing they as are not patches.  
But for some EAP unknown issues yet, some artifacts must be placed in different locations than expected, and these workarounds have been applied to the BPMS/BRMS distribution.  

**Seam transactions**    
Seam consists of two artifacts:      
* seam-transaction-api-3.X.jar  
* seam-transaction-3.X.jar  

The jBPM core static module for EAP depends on seam transaction api. So, this jars should be placed in another static module, not in the webapp.  
But for a unknown reason yet, when putting seam-transaction-3.X.jar outside the webapp, the transactions are not running.  
The reason seems to be that the transaction interceptor defined in <code>beans.xml</code> located inside webapp, is not registered if seam-transaction-3.X.jar (impl classes) is outside webapp lib.  
This interceptor is:  
 <code>
 &lt;interceptors&gt;
      &lt;class&gt;org.jboss.seam.transaction.TransactionInterceptor&lt;/class&gt;
  &lt;/interceptors&gt;
 </code>  
This behaviour should be analyzed with EAP team.  

**REST services**   
As seam transactions, if the jar containing kie remote REST services <code>kie-common-services-6-X</code> is located outside webapp lib, for example inside a EAP static module, the services are not running.     
This behaviour should be analyzed with EAP team.  

Limitations
===========
* The plugin only supports the generation of a single static layer.
* Maven versions major than > 3.1.X - Aether API from Maven 3.0.X to Maven 3.1.X has been changed. This plugin version only supports Maven versions major than 3.1.X
* The current plugin graph implementation type (currently only FLAT) generates the static modules by NOT exporting the dependencies (see <code>export</code> attribute for JBoss module descriptors).
