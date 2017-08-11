/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.gateway;

import java.io.IOException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class ErrorResponseFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

        Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());

        // default NotFoundException doesn't contain original server response
        // this filter creates a not empty Not Found exception
        // useful for client
        if (NOT_FOUND.equals(status) && responseContext.hasEntity()) {

            String errorMessage = IOUtils.toString(responseContext.getEntityStream(), "UTF-8");
            throw new NotFoundException(errorMessage);

        } else if (INTERNAL_SERVER_ERROR.equals(status) && requestContext.hasEntity()) {

            String errorMessage = IOUtils.toString(responseContext.getEntityStream(), "UTF-8");
            throw new ServerErrorException(errorMessage, INTERNAL_SERVER_ERROR);

        } else if (responseContext.getStatus() < 200 || responseContext.getStatus() >= 300) {

            if (requestContext.hasEntity()) {

                String errorMessage = IOUtils.toString(responseContext.getEntityStream(), "UTF-8");
                throw new WebApplicationException(errorMessage, status);

            }

            throw new WebApplicationException(status);

        }

    }

}
