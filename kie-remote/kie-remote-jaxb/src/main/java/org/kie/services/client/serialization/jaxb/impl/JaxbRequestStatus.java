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

package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum JaxbRequestStatus {
    SUCCESS,
    // Technical failure on the server side
    FAILURE,
    // Syntax exception or command not accepted
    BAD_REQUEST,
    // not an allowed command
    FORBIDDEN,
    // task service permissions
    PERMISSIONS_CONFLICT,
    // instance does not exist
    NOT_FOUND;
}
