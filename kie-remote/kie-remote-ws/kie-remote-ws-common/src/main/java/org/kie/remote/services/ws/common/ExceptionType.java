/*
 * Copyright 2015 JBoss Inc
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


import javax.xml.bind.annotation.XmlType;

/**
 * All of the possible types of Service exceptions.
 */
@XmlType
public enum ExceptionType {

    // Generic type for internal system faults (default value)
	SYSTEM, 
    // Improper configuration to handle request: correct configuration and restart instance
	CONFIGURATION, 
	// Problem with input parameters: correct the input in the webservice request
	VALIDATION, 
	// Problem with authorisation: try again with other credentials
	PERMISSION, 
	// Request conflicts with an Problem with connection to a backend component, try again later.
	CONFLICT

}
