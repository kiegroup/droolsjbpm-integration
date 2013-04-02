/*
 * Copyright 2013 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drools.osgi.tests.common;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import org.ops4j.pax.exam.Option;

public class CommonPaxExamConfiguration {

	public static Option baseDroolsConfiguration() {
		return composite(
				junitBundles(),
				// cleanCaches(false),

				// list of bundles that should be installed
				mavenBundle().groupId("org.osgi")
						.artifactId("org.osgi.compendium").version("4.3.0"),
				mavenBundle().groupId("org.knowhowlab.osgi")
						.artifactId("org.knowhowlab.osgi.testing.utils")
						.version("1.2.0"),
				mavenBundle().groupId("org.knowhowlab.osgi")
						.artifactId("org.knowhowlab.osgi.testing.assertions")
						.version("1.2.0"),
				mavenBundle().groupId("org.slf4j").artifactId("slf4j-api")
						.version("1.7.2"),
				mavenBundle().groupId("ch.qos.logback")
						.artifactId("logback-core").version("1.0.9"),
				mavenBundle().groupId("ch.qos.logback")
						.artifactId("logback-classic").version("1.0.9"),
				mavenBundle().groupId("org.drools")
						.artifactId("drools-osgi-enviroment-tests-common")
						.version("6.0.0-SNAPSHOT"),
				kieConfiguration(),
				droolsCompileConfiguration(),
				droolsCoreConfiguration(),
				droolsDecisionTemplateConfiguration(),
				droolsDecisionTableConfiguration(),
				systemProperty("project.version").value(
						System.getProperty("project.version")));
	}

	protected static Option droolsDecisionTemplateConfiguration() {
		return composite(mavenBundle().groupId("org.drools")
				.artifactId("drools-templates").version("6.0.0-SNAPSHOT"));
	}

	protected static Option kieConfiguration() {
		return composite(mavenBundle().groupId("org.kie").artifactId("kie-api")
				.version("6.0.0-SNAPSHOT"), mavenBundle().groupId("org.kie")
				.artifactId("kie-internal").version("6.0.0-SNAPSHOT"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.xstream")
						.version("1.4.4_1"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.woodstox")
						.version("3.2.9_3"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.xmlpull")
						.version("1.1.3.1_2"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.xpp3")
						.version("1.1.4c_6"));
	}

	protected static Option droolsCoreConfiguration() {
		return composite(
				mavenBundle().groupId("org.drools").artifactId("drools-core")
						.version("6.0.0-SNAPSHOT"),
				mavenBundle().groupId("com.google.protobuf")
						.artifactId("protobuf-java").version("2.5.0"),
				mavenBundle().groupId("org.apache.servicemix.specs")
						.artifactId("org.apache.servicemix.specs.jaxb-api-2.2")
						.version("2.2.0"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.jaxb-impl")
						.version("2.2.1.1_2"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.jaxb-xjc")
						.version("2.2.1.1_2"),
				mavenBundle()
						.groupId("org.apache.servicemix.specs")
						.artifactId(
								"org.apache.servicemix.specs.activation-api-1.1")
						.version("2.2.0"),
				mavenBundle().groupId("org.apache.servicemix.specs")
						.artifactId("org.apache.servicemix.specs.stax-api-1.2")
						.version("2.2.0"),

				mavenBundle().groupId("org.mvel").artifactId("mvel2")
						.version("2.1.4.Final"));
	}

	protected static Option droolsCompileConfiguration() {
		return composite(mavenBundle().groupId("org.drools")
				.artifactId("drools-compiler").version("6.0.0-SNAPSHOT"));
	}

	protected static Option droolsDecisionTableConfiguration() {
		return composite(
				mavenBundle().groupId("org.drools")
						.artifactId("drools-decisiontables")
						.version("6.0.0-SNAPSHOT"),
				mavenBundle().groupId("org.apache.servicemix.bundles")
						.artifactId("org.apache.servicemix.bundles.poi")
						.version("3.9_1"),
				mavenBundle().groupId("commons-codec")
						.artifactId("commons-codec").version("1.5"));
	}
}
