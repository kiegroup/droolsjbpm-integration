/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.ws.common;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * This is used to return the version of a service.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VersionResponse", propOrder = {
    "_version"
})
public class VersionResponse extends SerializableServiceObject {

	/** Serial Version UID. */
    private static final long serialVersionUID = -374376680962126054L;
    
    /** The version. */
	private String _version;
	
	/**
	 * Constructor.
	 */
	public VersionResponse() {
		this("unknown");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param version The version.
	 */
	public VersionResponse(String version) {
		_version = version;
	}

	/**
	 * @return The version.
	 */
	public String getVersion() {
		return _version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		_version = version;
	}
	
}
