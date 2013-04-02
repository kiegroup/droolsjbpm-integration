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

import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleAvailable;

import org.osgi.framework.Version;

public class DroolsBundlesAssertions {

	public static void assertDroolsBundles() {
		assertBundleAvailable("org.apache.servicemix.bundles.xstream", new Version(
				"1.4.4_1"));
		assertBundleAvailable("org.apache.servicemix.bundles.woodstox", new Version(
				"3.2.9_3"));
		assertBundleAvailable("org.apache.servicemix.bundles.xmlpull", new Version(
				"1.1.3.1_2"));
		assertBundleAvailable("org.apache.servicemix.bundles.xpp3", new Version(
				"1.1.4c_6"));
		assertBundleAvailable("org.kie.internal", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("org.kie.api", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("org.drools.core", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("org.drools.templates", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("org.drools.decisiontables", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("org.drools.persistence.jpa", new Version(
				"6.0.0-SNAPSHOT"));
		assertBundleAvailable("com.google.protobuf", new Version(
				"2.5.0"));
		assertBundleAvailable("org.apache.servicemix.specs.jaxb-api-2.2", new Version(
				"2.2.0"));
		assertBundleAvailable("org.apache.servicemix.specs.activation-api-1.1", new Version(
				"2.2.0"));
		assertBundleAvailable("org.apache.servicemix.specs.stax-api-1.2", new Version(
				"2.2.0"));
		assertBundleAvailable("com.google.protobuf", new Version(
				"2.5.0"));
		assertBundleAvailable("org.apache.servicemix.bundles.jaxb-impl", new Version(
				"2.2.1.1_2"));
		assertBundleAvailable("org.apache.servicemix.bundles.jaxb-xjc", new Version(
				"2.2.1.1_2"));

		assertBundleAvailable("org.apache.servicemix.bundles.poi", new Version(
				"3.9_1"));
		assertBundleAvailable("org.apache.commons.codec", new Version(
				"1.5"));
		
		assertBundleAvailable("org.mvel2", new Version(
				"2.1.4.Final3"));
		
	}
}
