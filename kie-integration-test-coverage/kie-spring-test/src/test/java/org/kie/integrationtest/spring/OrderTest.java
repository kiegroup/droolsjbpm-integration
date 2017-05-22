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

package org.kie.integrationtest.spring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(locations = "classpath:springContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class OrderTest {
    @KSession("orderKnowledgeSession")
    private StatelessKieSession orderRulesProcessor;

    private final Party buyer  = new Party();
    private final Party seller = new Party();

    @Test
    public void testConsignmentOrder() {
        testOrder(new Order(seller, buyer, OrderType.CONSIGNMENT), 0);

        testOrder(new Order(buyer, seller, OrderType.CONSIGNMENT), 0);

        testOrder(new Order(buyer, buyer, OrderType.CONSIGNMENT), 1);

        testOrder(new Order(seller, seller, OrderType.CONSIGNMENT), 1);
    }

    @Test
    public void testSalesOrder() {
        testOrder(new Order(seller, buyer, OrderType.SALE), 0);

        testOrder(new Order(buyer, seller, OrderType.SALE), 0);

        testOrder(new Order(buyer, buyer, OrderType.SALE), 1);

        testOrder(new Order(seller, seller, OrderType.SALE), 1);
    }

    @Test
    public void testTransferOrder() {
        testOrder(new Order(buyer, buyer, OrderType.TRANSFER), 0);

        testOrder(new Order(seller, seller, OrderType.TRANSFER), 0);

        testOrder(new Order(buyer, seller, OrderType.TRANSFER), 1);

        testOrder(new Order(seller, buyer, OrderType.TRANSFER), 1);
    }

    private void testOrder(final Order order, final int errorCount) {
        final Set<String> errors = new HashSet<>();

        orderRulesProcessor.execute(Arrays.asList(order, errors));

        assertEquals(errorCount, errors.size());
    }
}
