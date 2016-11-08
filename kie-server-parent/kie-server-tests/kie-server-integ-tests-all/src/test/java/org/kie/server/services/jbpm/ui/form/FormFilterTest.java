/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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


package org.kie.server.services.jbpm.ui.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.junit.Test;

public class FormFilterTest {


    @Test
    public void testSubForm() throws Exception {

        FormManagerService formManagerService = new FormManagerServiceImpl();

        String createOrderFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/CreateOrder.form"));
        String headerFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/PurchaseHeader.form"));
        String orderLineFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/PurchaseLine.form"));

        formManagerService.registerForm("test", "CreateOrder.form", createOrderFormContent);
        formManagerService.registerForm("test", "PurchaseHeader.form", headerFormContent);
        formManagerService.registerForm("test", "PurchaseLine.form", orderLineFormContent);

        RemoteFormModellerFormProvider formProvider = new RemoteFormModellerFormProvider();
        formProvider.configure(formManagerService);

        // setup some test data
        Map<String, Object> inputs = new HashMap<String, Object>();

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setDescription("test description");
        purchaseOrder.setRequiresCFOApproval(true);
        purchaseOrder.setTotal(100.0);
        // header
        PurchaseOrderHeader header = new PurchaseOrderHeader();
        header.setCreationDate(new Date());
        header.setCustomer("john");
        header.setProject("test project");

        purchaseOrder.setHeader(header);
        // order lines
        List<PurchaseOrderLine> orderLines = new ArrayList<PurchaseOrderLine>();

        PurchaseOrderLine orderLine1 = new PurchaseOrderLine();
        orderLine1.setDescription("first line");
        orderLine1.setTotal(10.0);
        orderLine1.setAmount(2.0);
        orderLine1.setUnitPrice(25.0);

        orderLines.add(orderLine1);

        PurchaseOrderLine orderLine2 = new PurchaseOrderLine();
        orderLine2.setDescription("second line");
        orderLine2.setTotal(20.0);
        orderLine2.setAmount(4.0);
        orderLine2.setUnitPrice(25.0);

        orderLines.add(orderLine2);

        purchaseOrder.setLines(orderLines);

        inputs.put("po_in", purchaseOrder);

        // outputs
        Map<String, Object> outputs = new HashMap<String, Object>();
        PurchaseOrder purchaseOrderOut = new PurchaseOrder();
        purchaseOrderOut.setDescription("output description");
        purchaseOrderOut.setRequiresCFOApproval(true);
        purchaseOrderOut.setTotal(100.0);
        // header
        PurchaseOrderHeader headerOut = new PurchaseOrderHeader();
        headerOut.setCreationDate(new Date());
        headerOut.setCustomer("john output");
        headerOut.setProject("output project");

        purchaseOrderOut.setHeader(headerOut);
        // order lines
        List<PurchaseOrderLine> orderLinesOut = new ArrayList<PurchaseOrderLine>();

        PurchaseOrderLine orderLineOut = new PurchaseOrderLine();
        orderLineOut.setDescription("output line");
        orderLineOut.setTotal(10.0);
        orderLineOut.setAmount(2.0);
        orderLineOut.setUnitPrice(25.0);

        orderLinesOut.add(orderLineOut);

        purchaseOrderOut.setLines(orderLinesOut);

        outputs.put("po_out", purchaseOrderOut);

        String filtered = formProvider.filterXML(createOrderFormContent, "en", "test", inputs, outputs);

        System.out.println(filtered);
    }
}
