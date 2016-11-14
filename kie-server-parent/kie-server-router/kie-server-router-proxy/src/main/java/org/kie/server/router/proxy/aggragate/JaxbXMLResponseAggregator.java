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

package org.kie.server.router.proxy.aggragate;

import static org.kie.server.router.utils.Helper.readProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class JaxbXMLResponseAggregator extends XMLResponseAggregator {

    private static final String XML_TYPE = "application/xml";

    private static final Properties sortByMapping = readProperties(JaxbXMLResponseAggregator.class.getResourceAsStream("/sort-jaxb.mapping"));

    protected List<String> nodes = Arrays.asList(
            "processes",
            "fragments",
            "comments",
            "definitions",
            "instances",
            "milestones",
            "role-assignments",
            "stages",
            "migration-report-instance",
            "process-node",
            "task-notification",
            "task-reassignment",
            "timer-instance",
            "queries",
            "task",
            "document-instances",
            "error-info-instance",
            "node-instance",
            "process-instance",
            "request-info-instance",
            "solver",
            "task-attachment",
            "task-comment",
            "task-event-instance",
            "task-instance",
            "task-summary",
            "variable-instance",
            "work-item-instance",
            "kie-container");

    public JaxbXMLResponseAggregator() {
        super();
    }

    @Override
    public boolean supports(Object... acceptTypes) {

        return supports(XML_TYPE, acceptTypes);
    }

    @Override
    protected void copyNodes(NodeList children, Document target, Node targetNode) {

        for (int j = 0; j < children.getLength(); j++) {
            Node existing = children.item(j);
            if (nodes.contains(existing.getNodeName()) && existing.hasChildNodes()) {
                Node imported = target.importNode(existing, true);
                targetNode.appendChild(imported);
            }
        }
    }

    @Override
    protected List<String> knownNames() {
        return nodes;
    }

    @Override
    protected String getElementLevel() {

        return "1";
    }

    @Override
    protected String getRootNode(String rootNode) {
        return "/" + rootNode;
    }

    @Override
    protected String sortBy(String fieldName) {

        return sortByMapping.getProperty(fieldName, fieldName);
    }
}

