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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class FormFilterTest {

    public static final int EXPECTED_FORMS = 3;
    public static final int PURCHASE_ORDER_FIELDS = 5;

    private FormManagerService formManagerService;

    private InMemoryFormProvider formProvider;

    private String createOrderFormContent;
    private String headerFormContent;
    private String orderLineFormContent;

    @Before
    public void setup() throws IOException {
        formManagerService = new FormManagerServiceImpl();

        createOrderFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/CreateOrder.form"),
                                                  "UTF-8");
        headerFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/PurchaseHeader.form"),
                                             "UTF-8");
        orderLineFormContent = IOUtils.toString(this.getClass().getResourceAsStream("/test-forms/PurchaseLine.form"),
                                                "UTF-8");

        formManagerService.registerForm("test",
                                        "CreateOrder.form",
                                        createOrderFormContent);
        formManagerService.registerForm("test",
                                        "PurchaseHeader.form",
                                        headerFormContent);
        formManagerService.registerForm("test",
                                        "PurchaseLine.form",
                                        orderLineFormContent);

        formProvider = new InMemoryFormProvider();
        formProvider.configure(formManagerService);
    }

    @Test
    public void testFilterSubForm() throws Exception {

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

        inputs.put("po_in",
                   purchaseOrder);

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

        outputs.put("po_out",
                    purchaseOrderOut);

        String filteredForm = formProvider.filterXML(createOrderFormContent,
                                                     "en",
                                                     "test",
                                                     inputs,
                                                     outputs);

        validateFormXML(filteredForm);
    }

    @Test
    public void testAddSubForm() throws Exception {
        String allForms = formProvider.attachSubForms(createOrderFormContent,
                                                      "test");

        validateFormXML(allForms);
    }

    private void validateFormXML(String formXML) throws Exception {
        assertNotNull(formXML);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new ByteArrayInputStream(formXML.getBytes()));

        assertNotNull(doc);

        NodeList allForms = doc.getElementsByTagName(InMemoryFormProvider.NODE_FORM);

        assertEquals(EXPECTED_FORMS,
                     allForms.getLength());

        Node nodeForm = allForms.item(0);

        NodeList childNodes = nodeForm.getChildNodes();

        int childCount = 0;

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals(InMemoryFormProvider.NODE_FIELD)) {
                childCount++;
            }
        }

        assertEquals(PURCHASE_ORDER_FIELDS,
                     childCount);
    }
}
