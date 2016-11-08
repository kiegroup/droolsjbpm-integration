/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.casemgmt;

public class Messages {

    public static final String CASE_DEFINITION_NOT_FOUND = "Could not find case definition \"{0}\" in container \"{1}\"";
    public static final String CONTAINER_NOT_FOUND = "Container not found with id \"{0}\"";

    public static final String CASE_INSTANCE_NOT_FOUND = "Could not find case instance \"{0}\"";

    public static final String CASE_INSTANCE_ACTIVE = "Case with id \"{0}\" is already started/reopened";

    public static final String CASE_COMMENT_NOT_FOUND = "Could not find case comment with id \"{0}\" in case \"{1}\"";

    public static final String UNEXPECTED_ERROR = "Unexpected error during processing request \"{0}\"";
}
