/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.casemgmt;

import java.text.MessageFormat;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.casemgmt.api.CaseActiveException;
import org.jbpm.casemgmt.api.CaseNotFoundException;
import org.jbpm.services.api.DeploymentNotFoundException;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.remote.rest.casemgmt.Messages.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;

public abstract class AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCaseResource.class);

    protected CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase;
    protected KieServerRegistry context;

    public AbstractCaseResource() {
    }

    public AbstractCaseResource(
            final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
            final KieServerRegistry context) {
        this.caseManagementRuntimeDataServiceBase = caseManagementRuntimeDataServiceBase;
        this.context = context;
    }

    protected Response invokeCaseOperation(HttpHeaders headers, String containerId, String caseId, CaseOperation operation) {
        Variant v = getVariant(headers);
        String type = getContentType(headers);
        Header conversationIdHeader = buildConversationIdHeader(containerId, context, headers);
        try {
            return operation.invoke(v, type, conversationIdHeader);
        } catch (CaseActiveException e) {
            return alreadyExists(
                    MessageFormat.format(CASE_INSTANCE_ACTIVE, caseId), v, conversationIdHeader);
        } catch (CaseNotFoundException e) {
            return notFound(
                    MessageFormat.format(CASE_INSTANCE_NOT_FOUND, caseId), v, conversationIdHeader);
        } catch (DeploymentNotFoundException e) {
            return notFound(
                    MessageFormat.format(CONTAINER_NOT_FOUND, containerId), v, conversationIdHeader);
        } catch (IllegalArgumentException e) {
           return badRequest(
                   e.getMessage(), v, conversationIdHeader);
        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(
                    MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v, conversationIdHeader);
        }
    }

}