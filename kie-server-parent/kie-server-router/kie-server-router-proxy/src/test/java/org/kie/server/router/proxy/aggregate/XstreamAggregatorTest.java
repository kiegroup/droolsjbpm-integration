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

package org.kie.server.router.proxy.aggregate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.kie.server.router.proxy.aggragate.XstreamXMLResponseAggregator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XstreamAggregatorTest extends AbstractAggregateTest {

    private static final Logger logger = Logger.getLogger(XstreamAggregatorTest.class);

    @Test
    public void testAggregateProcessDefinitions() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(5, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(3, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(0, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessInstances() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-instance-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-instance-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstanceList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList instances = xml.getElementsByTagName("processInstances");
        assertNotNull(instances);
        assertEquals(1, instances.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstance");
        assertNotNull(processInstances);
        assertEquals(3, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-instance-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-instance-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstanceList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList instances = xml.getElementsByTagName("processInstances");
        assertNotNull(instances);
        assertEquals(1, instances.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstance");
        assertNotNull(processInstances);
        assertEquals(1, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-instance-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-instance-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstanceList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList instances = xml.getElementsByTagName("processInstances");
        assertNotNull(instances);
        assertEquals(1, instances.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstance");
        assertNotNull(processInstances);
        assertEquals(2, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-instance-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-instance-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstanceList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList instances = xml.getElementsByTagName("processInstances");
        assertNotNull(instances);
        assertEquals(1, instances.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.ProcessInstance");
        assertNotNull(processInstances);
        assertEquals(0, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummaries() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/task-summary-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/task-summary-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummaryList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList tasks = xml.getElementsByTagName("tasks");
        assertNotNull(tasks);
        assertEquals(1, tasks.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummary");
        assertNotNull(processInstances);
        assertEquals(5, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/task-summary-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/task-summary-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummaryList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList tasks = xml.getElementsByTagName("tasks");
        assertNotNull(tasks);
        assertEquals(1, tasks.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummary");
        assertNotNull(processInstances);
        assertEquals(3, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/task-summary-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/task-summary-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummaryList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList tasks = xml.getElementsByTagName("tasks");
        assertNotNull(tasks);
        assertEquals(1, tasks.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummary");
        assertNotNull(processInstances);
        assertEquals(2, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/task-summary-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/task-summary-empty.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummaryList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList tasks = xml.getElementsByTagName("tasks");
        assertNotNull(tasks);
        assertEquals(1, tasks.getLength());

        NodeList processInstances = xml.getElementsByTagName("org.kie.server.api.model.instance.TaskSummary");
        assertNotNull(processInstances);
        assertEquals(0, processInstances.getLength());
    }

    @Test
    public void testSortProcessDefinitions() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 0, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("id");
        assertNotNull(processDefIds);
        assertEquals(2, processDefIds.getLength());
        // make sure it's properly sorted and paged
        String value1 = processDefIds.item(0).getFirstChild().getNodeValue();
        assertEquals("1", value1);
        String value2 = processDefIds.item(1).getFirstChild().getNodeValue();
        assertEquals("2", value2);
    }

    @Test
    public void testSortProcessDefinitionsDescending() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", false, 0, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("id");
        assertNotNull(processDefIds);
        assertEquals(2, processDefIds.getLength());
        // make sure it's properly sorted and paged
        String value1 = processDefIds.item(0).getFirstChild().getNodeValue();
        assertEquals("5", value1);
        String value2 = processDefIds.item(1).getFirstChild().getNodeValue();
        assertEquals("4", value2);
    }

    @Test
    public void testSortProcessDefinitionsNextPage() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 1, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("id");
        assertNotNull(processDefIds);
        assertEquals(2, processDefIds.getLength());
        // make sure it's properly sorted and paged
        String value1 = processDefIds.item(0).getFirstChild().getNodeValue();
        assertEquals("3", value1);
        String value2 = processDefIds.item(1).getFirstChild().getNodeValue();
        assertEquals("4", value2);
    }

    @Test
    public void testSortProcessDefinitionsNextPageDescending() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", false, 1, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("id");
        assertNotNull(processDefIds);
        assertEquals(2, processDefIds.getLength());
        // make sure it's properly sorted and paged
        String value1 = processDefIds.item(0).getFirstChild().getNodeValue();
        assertEquals("3", value1);
        String value2 = processDefIds.item(1).getFirstChild().getNodeValue();
        assertEquals("2", value2);
    }

    @Test
    public void testSortProcessDefinitionsOutOfPage() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/process-def-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 5, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinitionList");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("processes");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("org.kie.server.api.model.definition.ProcessDefinition");
        assertNotNull(processDefs);
        assertEquals(0, processDefs.getLength());

    }

    @Test
    public void testAggregateContainers() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/containers-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/containers-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("org.kie.server.api.model.ServiceResponse");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList defs = xml.getElementsByTagName("result");
        assertNotNull(defs);
        assertEquals(1, defs.getLength());

        NodeList processDefs = xml.getElementsByTagName("kie-container");
        assertNotNull(processDefs);
        assertEquals(6, processDefs.getLength());
    }

    @Test
    public void testAggregateRawList() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/raw-list-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/raw-list-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("sql-timestamp");
        assertNotNull(processes);
        assertEquals(5, processes.getLength());
    }

    @Test
    public void testAggregateRawListWithPaging() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/xstream/raw-list-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/xstream/raw-list-2.xml"));
        XstreamXMLResponseAggregator aggregate = new XstreamXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, null, true, 1, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("sql-timestamp");
        assertNotNull(processes);
        assertEquals(2, processes.getLength());
    }
}

