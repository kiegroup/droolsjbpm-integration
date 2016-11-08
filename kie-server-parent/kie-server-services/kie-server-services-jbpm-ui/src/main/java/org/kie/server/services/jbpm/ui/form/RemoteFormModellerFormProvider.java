/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringEscapeUtils;
import org.drools.core.util.MVELSafeHelper;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.form.provider.AbstractFormProvider;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.kie.server.services.jbpm.ui.FormServiceBase;
import org.kie.server.services.jbpm.ui.api.UIFormProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RemoteFormModellerFormProvider extends AbstractFormProvider implements UIFormProvider {

    public static final String NODE_FORM = "form";
    public static final String NODE_FIELD = "field";
    public static final String NODE_PROPERTY = "property";
    public static final String NODE_DATA_HOLDER = "dataHolder";

    public static final String ATTR_NAME = "name";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_TYPE = "type";
    public static final List<String> ATTR_LANG_NAMES = Arrays.asList("label", "errorMessage", "title");

    public static final String SUB_FORM_TYPE = "Subform";
    public static final String MULTI_SUB_FORM_TYPE = "MultipleSubform";

    public RemoteFormModellerFormProvider() {
    }

    @Override
    public void configure(FormManagerService formManagerService) {
        setFormManagerService(formManagerService);
    }

    @Override
    public String render(String name, ProcessDefinition process, Map<String, Object> renderContext) {
        if (!(process instanceof ProcessAssetDesc)) {
            return null;
        }

        String templateString = formManagerService.getFormByKey(process.getDeploymentId(), process.getId());
        if (templateString == null) {
            templateString = formManagerService.getFormByKey(process.getDeploymentId(), process.getId() + getFormSuffix());
        }

        if (templateString == null || templateString.isEmpty()) {
            return null;
        } else {
            String lang = (String) renderContext.get("lang");
            Boolean filterContent = (Boolean) renderContext.get("filterForm");

            if (filterContent == null || Boolean.TRUE.equals(filterContent)) {
                templateString = filterXML(templateString, lang, process.getDeploymentId(), null, null);
            }
            return templateString;
        }
    }

    @Override
    public String render(String name, Task task, ProcessDefinition process, Map<String, Object> renderContext) {
        if (task == null) return null;

        String lookupName = getTaskFormName( task );

        if ( lookupName == null || lookupName.isEmpty()) return null;

        String templateString = formManagerService.getFormByKey(task.getTaskData().getDeploymentId(), lookupName);

        if (templateString == null || templateString.isEmpty()) {
            return null;
        } else {
            Map inputs = new HashMap();
            Map m = (Map) renderContext.get("inputs");
            if (m != null) {
                inputs.putAll(m);
            }

            Map outputs = new HashMap();
            Map mOut = (Map) renderContext.get("outputs");
            if (mOut != null) {
                outputs.putAll(mOut);
            }

            String lang = (String) renderContext.get("lang");
            Boolean filterContent = (Boolean) renderContext.get("filterForm");

            if (filterContent == null || Boolean.TRUE.equals(filterContent)) {
                templateString = filterXML(templateString, lang, task.getTaskData().getDeploymentId(), inputs, outputs);
            }
            return templateString;
        }
    }

    protected String filterXML(String document, String lang, String deploymentId, Map inputs, Map outputs) {
        try {
            if (inputs == null) {
                inputs = Collections.emptyMap();
            }
            if (outputs == null) {
                outputs = Collections.emptyMap();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new ByteArrayInputStream(document.getBytes()));
            NodeList nodes = doc.getElementsByTagName(NODE_FORM);
            Node nodeForm = nodes.item(0);
            NodeList childNodes = nodeForm.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeName().equals(NODE_FIELD)) {

                    String fieldType = node.getAttributes().getNamedItem(ATTR_TYPE).getNodeValue();
                    if (SUB_FORM_TYPE.equals(fieldType)) {

                        String defaultSubForm = findPropertyValue(node, "defaultSubform");
                        if (defaultSubForm != null) {

                            String subFormContent = formManagerService.getFormByKey(deploymentId, defaultSubForm);

                            if (subFormContent != null) {
                                // read once to find out input binding name
                                Document tmpSubForm = builder.parse(new ByteArrayInputStream(subFormContent.getBytes()));

                                Node firstFieldNode = tmpSubForm.getElementsByTagName(NODE_FIELD).item(0);
                                // inputs - current node
                                String currentNodeInputBinding = findPropertyValue(node, "inputBinding");
                                currentNodeInputBinding = currentNodeInputBinding.replaceAll("/", ".");

                                // outputs current node
                                String currentNodeOutputBinding = findPropertyValue(node, "outputBinding");
                                currentNodeOutputBinding = currentNodeOutputBinding.replaceAll("/", ".");

                                // inputs sub form
                                String inputBindingSubForm = findPropertyValue(firstFieldNode, "inputBinding");
                                inputBindingSubForm = inputBindingSubForm.split("/")[0];

                                // outputs sub form
                                String outputBindingSubForm = findPropertyValue(firstFieldNode, "outputBinding");
                                outputBindingSubForm = outputBindingSubForm.split("/")[0];

                                Map<String, Object> subFormInputs = new HashMap<String, Object>(inputs);
                                try {
                                    subFormInputs.put(inputBindingSubForm, MVELSafeHelper.getEvaluator().eval(currentNodeInputBinding, inputs));
                                } catch (Exception e) {

                                }

                                Map<String, Object> subFormOutputs = new HashMap<String, Object>(outputs);
                                try {
                                    subFormOutputs.put(outputBindingSubForm, MVELSafeHelper.getEvaluator().eval(currentNodeOutputBinding, outputs));
                                } catch (Exception e) {

                                }

                                // run the transformation
                                String filtered = filterXML(subFormContent, lang, deploymentId, subFormInputs, subFormOutputs);

                                Document docSubForm = builder.parse(new ByteArrayInputStream(filtered.getBytes()));
                                NodeList nodesSubForm = docSubForm.getElementsByTagName(NODE_FORM);
                                Node nodeFormSubForm = nodesSubForm.item(0);

                                Node imported = doc.importNode(nodeFormSubForm, true);

                                node.getParentNode().replaceChild(imported, node);
                            }
                        }
                    } else if (MULTI_SUB_FORM_TYPE.equals(fieldType)) {

                        String defaultSubForm = findPropertyValue(node, "defaultSubform");
                        if (defaultSubForm != null) {

                            String subFormContent = formManagerService.getFormByKey(deploymentId, defaultSubForm);
                            if (subFormContent != null) {

                                String inputBinding = findPropertyValue(node, "inputBinding");
                                inputBinding = inputBinding.replaceAll("/", ".");

                                String outputBinding = findPropertyValue(node, "outputBinding");
                                outputBinding = outputBinding.replaceAll("/", ".");

                                Collection<Object> list = new ArrayList<Object>();
                                Collection<Object> listOut = new ArrayList<Object>();
                                Map<String, Object> subFormInputs = new HashMap<String, Object>(inputs);
                                Map<String, Object> subFormOutputs = new HashMap<String, Object>(outputs);
                                try {
                                    list = (Collection<Object>) MVELSafeHelper.getEvaluator().eval(inputBinding, inputs);
                                } catch (Exception e) {
                                    // no elements found add simple object to generate single line
                                    list.add(new Object());
                                }
                                try {
                                    listOut = (Collection<Object>) MVELSafeHelper.getEvaluator().eval(outputBinding, outputs);
                                } catch (Exception e) {
                                    // no elements found add simple object to generate single line
                                    list.add(new Object());
                                }

                                // read once to find out input binding name
                                Document tmpSubForm = builder.parse(new ByteArrayInputStream(subFormContent.getBytes()));

                                Node firstFieldNode = tmpSubForm.getElementsByTagName(NODE_FIELD).item(0);

                                String inputBindingSubForm = findPropertyValue(firstFieldNode, "inputBinding");
                                inputBindingSubForm = inputBindingSubForm.split("/")[0];

                                String outputBindingSubForm = findPropertyValue(firstFieldNode, "outputBinding");
                                outputBindingSubForm = outputBindingSubForm.split("/")[0];
                                // inputs
                                for (Object element : list) {
                                    subFormInputs.put(inputBindingSubForm, element);

                                    String filtered = filterXML(subFormContent, lang, deploymentId, subFormInputs, subFormOutputs);

                                    Document docSubForm = builder.parse(new ByteArrayInputStream(filtered.getBytes()));
                                    NodeList nodesSubForm = docSubForm.getElementsByTagName(NODE_FORM);
                                    Node nodeFormSubForm = nodesSubForm.item(0);

                                    Node imported = doc.importNode(nodeFormSubForm, true);

                                    node.getParentNode().appendChild(imported);
                                }
                                // outputs
                                for (Object element : listOut) {
                                    subFormOutputs.put(outputBindingSubForm, element);

                                    String filtered = filterXML(subFormContent, lang, deploymentId, Collections.emptyMap(), subFormOutputs);

                                    Document docSubForm = builder.parse(new ByteArrayInputStream(filtered.getBytes()));
                                    NodeList nodesSubForm = docSubForm.getElementsByTagName(NODE_FORM);
                                    Node nodeFormSubForm = nodesSubForm.item(0);

                                    Node imported = doc.importNode(nodeFormSubForm, true);

                                    node.getParentNode().appendChild(imported);
                                }

                                node.getParentNode().removeChild(node);
                            }

                        }

                    } else {

                        NodeList fieldPropsNodes = node.getChildNodes();
                        for (int j = 0; j < fieldPropsNodes.getLength(); j++) {
                            Node nodeFieldProp = fieldPropsNodes.item(j);
                            if (nodeFieldProp.getNodeName().equals(NODE_PROPERTY)) {
                                String propName = nodeFieldProp.getAttributes().getNamedItem(ATTR_NAME).getNodeValue();
                                String value = StringEscapeUtils.unescapeXml(nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).getNodeValue());
                                if (inputs != null && propName != null && value != null && "inputBinding".equals(propName)) {
                                    if (!value.isEmpty()) {
                                        value = value.replaceAll("/", ".");
                                        try {
                                            Object actualValue = MVELSafeHelper.getEvaluator().eval(value, inputs);

                                            nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).setNodeValue(String.valueOf(actualValue));
                                        } catch (Exception e) {
                                            // no elements found add simple object to generate single line

                                        }
                                    }
                                } else if (outputs != null && propName != null && value != null && "outputBinding".equals(propName)) {
                                    if (!value.isEmpty()) {
                                        value = value.replaceAll("/", ".");
                                        try {
                                            Object actualValue = MVELSafeHelper.getEvaluator().eval(value, outputs);

                                            nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).setNodeValue(String.valueOf(actualValue));
                                        } catch (Exception e) {
                                            // no elements found add simple object to generate single line

                                        }
                                    }
                                } else if (propName != null && value != null && ATTR_LANG_NAMES.contains(propName)) {
                                    filterProperty(nodeFieldProp, lang, value);
                                }
                            }
                        }
                    }
                }
            }

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            document = writer.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return document;
    }

    private void filterProperty(Node property, String lang, String value) {
        String label = getLabel(lang, value);
        property.getAttributes().getNamedItem(ATTR_VALUE).setNodeValue(label);
    }

    private static String getLabel(String lang, String value) {
        // logic based on form modeler way of taking values - applies to .form files
        String[] values = value.split("quot;");
        Map<String,String> langWord = new HashMap<String,String>();

        for (int i = 0; i < values.length;i=i+4) {
            String key = values[i + 1];
            String valueTmp="";
            if( i+3 < values.length){
                valueTmp = values[i + 3];
            }
            if(key.length()==2){
                langWord.put(key, valueTmp);
            }

        }
        // end of logic based on form modeler
        String response = langWord.get(lang);
        if (response == null || response.isEmpty()) {
            response = langWord.get("en");
        }
        return response;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    protected String getFormExtension() {
        return ".form";
    }

    protected String findPropertyValue(Node node, String propertyName) {
        NodeList fieldPropsNodes = node.getChildNodes();
        for (int j = 0; j < fieldPropsNodes.getLength(); j++) {
            Node nodeFieldProp = fieldPropsNodes.item(j);
            if (nodeFieldProp.getNodeName().equals(NODE_PROPERTY)) {
                String propName = nodeFieldProp.getAttributes().getNamedItem(ATTR_NAME).getNodeValue();
                String value = StringEscapeUtils.unescapeXml(nodeFieldProp.getAttributes().getNamedItem(ATTR_VALUE).getNodeValue());
                if (propertyName.equals(propName)) {
                    return value;
                }
            }
        }

        return null;
    }

    @Override
    public String getType() {
        return FormServiceBase.FormType.FORM_MODELLER_TYPE.getName();
    }
}
