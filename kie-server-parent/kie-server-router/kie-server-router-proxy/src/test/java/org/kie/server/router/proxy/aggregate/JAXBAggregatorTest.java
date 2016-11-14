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
import org.kie.server.router.proxy.aggragate.JaxbXMLResponseAggregator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class JAXBAggregatorTest extends AbstractAggregateTest {

    private static final Logger logger = Logger.getLogger(JAXBAggregatorTest.class);

    @Test
    public void testAggregateProcessDefinitions() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(5, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(3, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessDefinitionsEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(0, processDefs.getLength());
    }

    @Test
    public void testAggregateProcessInstances() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-instance-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("process-instance");
        assertNotNull(processInstances);
        assertEquals(4, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-instance-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("process-instance");
        assertNotNull(processInstances);
        assertEquals(1, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-instance-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("process-instance");
        assertNotNull(processInstances);
        assertEquals(3, processInstances.getLength());
    }

    @Test
    public void testAggregateProcessInstancesEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-instance-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-instance-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("process-instance");
        assertNotNull(processInstances);
        assertEquals(0, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummaries() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("task-summary-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("task-summary");
        assertNotNull(processInstances);
        assertEquals(5, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesTargetEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("task-summary-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("task-summary");
        assertNotNull(processInstances);
        assertEquals(3, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesSourceEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("task-summary-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("task-summary");
        assertNotNull(processInstances);
        assertEquals(2, processInstances.getLength());
    }

    @Test
    public void testAggregateTaskSummariesEmpty() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-empty.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/task-summary-empty.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("task-summary-list");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processInstances = xml.getElementsByTagName("task-summary");
        assertNotNull(processInstances);
        assertEquals(0, processInstances.getLength());
    }

    @Test
    public void testSortProcessDefinitions() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 0, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("process-id");
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
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", false, 0, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("process-id");
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
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 1, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("process-id");
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
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", false, 1, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(2, processDefs.getLength());

        NodeList processDefIds = xml.getElementsByTagName("process-id");
        assertNotNull(processDefIds);
        assertEquals(2, processDefIds.getLength());
        // make sure it's properly sorted and paged
        String value1 = processDefIds.item(0).getFirstChild().getNodeValue();
        assertEquals("3", value1);
        String value2 = processDefIds.item(1).getFirstChild().getNodeValue();
        assertEquals("2", value2);
    }

    @Test
    public void testSortProcessDefinitionsOutOPage() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/process-def-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/process-def-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data, "ProcessId", true, 5, 2);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("process-definitions");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList processDefs = xml.getElementsByTagName("processes");
        assertNotNull(processDefs);
        assertEquals(0, processDefs.getLength());
    }

    @Test
    public void testAggregateContainers() throws Exception {
        String xml1 = read(this.getClass().getResourceAsStream("/jaxb/containers-1.xml"));
        String xml2 = read(this.getClass().getResourceAsStream("/jaxb/containers-2.xml"));
        JaxbXMLResponseAggregator aggregate = new JaxbXMLResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(xml1);
        data.add(xml2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        Document xml = toXml(result);
        assertNotNull(xml);

        NodeList processes = xml.getElementsByTagName("response");
        assertNotNull(processes);
        assertEquals(1, processes.getLength());

        NodeList wrapper = xml.getElementsByTagName("kie-containers");
        assertNotNull(wrapper);
        assertEquals(1, wrapper.getLength());

        NodeList containers = xml.getElementsByTagName("kie-container");
        assertNotNull(containers);
        assertEquals(6, containers.getLength());
    }
}

