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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.kie.server.router.proxy.aggragate.JSONResponseAggregator;

public class JSONAggregatorTest extends AbstractAggregateTest {

    private static final Logger logger = Logger.getLogger(JSONAggregatorTest.class);

    @Test
    public void testAggregateProcessDefinitions() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(8, processDefs.length());
    }

    @Test
    public void testAggregateProcessDefinitionsTargetEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(5, processDefs.length());
    }

    @Test
    public void testAggregateProcessDefinitionsSourceEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(3, processDefs.length());
    }

    @Test
    public void testAggregateProcessDefinitionsEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(0, processDefs.length());
    }


    @Test
    public void testAggregateProcessInstances() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-instance-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-instance-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("process-instance");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(5, processDefs.length());
    }

    @Test
    public void testAggregateProcessInstancesTargetEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-instance-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-instance-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("process-instance");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(3, processDefs.length());
    }

    @Test
    public void testAggregateProcessInstancesSourceEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-instance-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-instance-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("process-instance");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(2, processDefs.length());
    }

    @Test
    public void testAggregateProcessInstancesEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-instance-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-instance-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("process-instance");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(0, processDefs.length());
    }

    @Test
    public void testAggregateTaskSummaries() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/task-summary-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/task-summary-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("task-summary");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(7, processDefs.length());
    }

    @Test
    public void testAggregateTaskSummariesTargetEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/task-summary-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/task-summary-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("task-summary");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(3, processDefs.length());
    }

    @Test
    public void testAggregateTaskSummariesSourceEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/task-summary-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/task-summary-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("task-summary");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(4, processDefs.length());
    }

    @Test
    public void testAggregateTaskSummariesEmpty() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/task-summary-empty.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/task-summary-empty.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        Object processes = aggregated.get("task-summary");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(0, processDefs.length());
    }

    @Test
    public void testSortProcessDefinitions() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String sorted = aggregate.aggregate(data, "ProcessId", true, 0, 2);
        logger.debug(sorted);

        JSONObject aggregated = new JSONObject(sorted);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(2, processDefs.length());
        // make sure it's properly sorted and paged
        String value1 = ((JSONObject)processDefs.get(0)).getString("process-id");
        assertEquals("1", value1);
        String value2 = ((JSONObject)processDefs.get(1)).getString("process-id");
        assertEquals("2", value2);
    }

    @Test
    public void testSortProcessDefinitionsDescending() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String sorted = aggregate.aggregate(data, "ProcessId", false, 0, 2);
        logger.debug(sorted);

        JSONObject aggregated = new JSONObject(sorted);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(2, processDefs.length());
        // make sure it's properly sorted and paged
        String value1 = ((JSONObject)processDefs.get(0)).getString("process-id");
        assertEquals("8", value1);
        String value2 = ((JSONObject)processDefs.get(1)).getString("process-id");
        assertEquals("7", value2);
    }

    @Test
    public void testSortProcessDefinitionsNextPage() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String sorted = aggregate.aggregate(data, "ProcessId", true, 1, 2);
        logger.debug(sorted);

        JSONObject aggregated = new JSONObject(sorted);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(2, processDefs.length());
        // make sure it's properly sorted and paged
        String value1 = ((JSONObject)processDefs.get(0)).getString("process-id");
        assertEquals("3", value1);
        String value2 = ((JSONObject)processDefs.get(1)).getString("process-id");
        assertEquals("4", value2);
    }

    @Test
    public void testSortProcessDefinitionsNextPageDescending() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String sorted = aggregate.aggregate(data, "ProcessId", false, 1, 2);
        logger.debug(sorted);

        JSONObject aggregated = new JSONObject(sorted);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(2, processDefs.length());
        // make sure it's properly sorted and paged
        String value1 = ((JSONObject)processDefs.get(0)).getString("process-id");
        assertEquals("6", value1);
        String value2 = ((JSONObject)processDefs.get(1)).getString("process-id");
        assertEquals("5", value2);
    }

    @Test
    public void testSortProcessDefinitionsOutOfPage() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/process-def-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/process-def-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String sorted = aggregate.aggregate(data, "ProcessId", true, 5, 2);
        logger.debug(sorted);

        JSONObject aggregated = new JSONObject(sorted);
        assertNotNull(aggregated);

        Object processes = aggregated.get("processes");
        assertNotNull(processes);
        assertTrue(processes instanceof JSONArray);

        JSONArray processDefs = (JSONArray) processes;
        assertEquals(0, processDefs.length());

    }

    @Test
    public void testAggregateContainers() throws Exception {
        String json1 = read(this.getClass().getResourceAsStream("/json/containers-1.json"));
        String json2 = read(this.getClass().getResourceAsStream("/json/containers-2.json"));

        JSONResponseAggregator aggregate = new JSONResponseAggregator();

        List<String> data = new ArrayList<>();
        data.add(json1);
        data.add(json2);

        String result = aggregate.aggregate(data);
        logger.debug(result);

        JSONObject aggregated = new JSONObject(result);
        assertNotNull(aggregated);

        JSONObject content = (JSONObject)((JSONObject) aggregated.get("result")).get("kie-containers");

        Object containers = content.get("kie-container");
        assertNotNull(containers);
        assertTrue(containers instanceof JSONArray);

        JSONArray processDefs = (JSONArray) containers;
        assertEquals(6, processDefs.length());
    }
}

