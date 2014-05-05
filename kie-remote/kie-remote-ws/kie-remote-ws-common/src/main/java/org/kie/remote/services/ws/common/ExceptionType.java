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
