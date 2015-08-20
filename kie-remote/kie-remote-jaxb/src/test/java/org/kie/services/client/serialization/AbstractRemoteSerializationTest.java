/*
 * Copyright 2015 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.services.client.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.task.jaxb.ComparePair;
import org.junit.Assume;
import org.junit.Test;
import org.kie.api.definition.KieDefinition.KnowledgeType;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.internal.process.CorrelationProperty;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItemResponse;
import org.kie.services.client.serialization.jaxb.impl.runtime.JaxbCorrelationKey;
import org.kie.services.client.serialization.jaxb.impl.runtime.JaxbCorrelationProperty;
import org.kie.services.client.serialization.jaxb.impl.type.JaxbString;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRemoteSerializationTest {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRemoteSerializationTest.class);

    protected enum TestType {
        JAXB, JSON, YAML;
    }

    abstract public TestType getType();

    abstract public void addClassesToSerializationProvider( Class<?>... extraClass );

    public abstract <T> T testRoundTrip( T in ) throws Exception;

    private static Reflections reflections = new Reflections(ClasspathHelper.forPackage("org.kie.services.client"),
            ClasspathHelper.forPackage("org.kie.remote"), new TypeAnnotationsScanner(), new SubTypesScanner());

    // TESTS

    /*
     * Tests
     */

    @Test
    public void jaxbClassesTest() throws Exception {
        Assume.assumeFalse(TestType.YAML.equals(getType()));

        int i = 0;
        for( Class<?> jaxbClass : reflections.getTypesAnnotatedWith(XmlRootElement.class) ) {
            ++i;
            Constructor<?> construct = jaxbClass.getConstructor(new Class[] {});
            Object jaxbInst = construct.newInstance(new Object[] {});
            testRoundTrip(jaxbInst);
        }
        assertTrue(i > 20);
    }

    @Test
    public void genericResponseTest() throws Exception {
        JaxbGenericResponse resp = new JaxbGenericResponse();
        resp.setMessage("error");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");

        testRoundTrip(resp);
    }

    @Test
    public void exceptionTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        JaxbExceptionResponse resp = new JaxbExceptionResponse();
        resp.setMessage("error");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");

        RuntimeException re = new RuntimeException();
        resp.setCause(re);

        testRoundTrip(resp);
    }

    @Test
    public void variablesResponseTest() throws Exception {
        JaxbVariablesResponse resp = new JaxbVariablesResponse();

        testRoundTrip(resp);

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("one", "two");
        resp.setVariables(vars);

        testRoundTrip(resp);
    }

    @Test
    public void historyLogListTest() throws Exception {
        JaxbHistoryLogList resp = new JaxbHistoryLogList();

        testRoundTrip(resp);

        // vLog
        org.jbpm.process.audit.VariableInstanceLog vLog = new org.jbpm.process.audit.VariableInstanceLog(23, "process", "varInst",
                "var", "two", "one");
        vLog.setExternalId("domain");
        Field dateField = org.jbpm.process.audit.VariableInstanceLog.class.getDeclaredField("date");
        dateField.setAccessible(true);
        dateField.set(vLog, new Date());
        Field idField = org.jbpm.process.audit.VariableInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(vLog, 32l);
        resp.getHistoryLogList().add(new JaxbVariableInstanceLog(vLog));

        // pLog
        org.jbpm.process.audit.ProcessInstanceLog pLog = new org.jbpm.process.audit.ProcessInstanceLog(23, "process");
        pLog.setDuration(2000l);
        pLog.setEnd(new Date());
        pLog.setExternalId("domain");
        pLog.setIdentity("id");
        pLog.setOutcome("error");
        pLog.setParentProcessInstanceId(42);
        pLog.setProcessName("name");
        pLog.setProcessVersion("1-SNAP");
        pLog.setStatus(2);
        idField = org.jbpm.process.audit.ProcessInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(pLog, 32l);
        resp.getHistoryLogList().add(new JaxbProcessInstanceLog(pLog));

        // nLog
        org.jbpm.process.audit.NodeInstanceLog nLog = new org.jbpm.process.audit.NodeInstanceLog(0, 23, "process", "nodeInst",
                "node", "wally");
        idField = org.jbpm.process.audit.NodeInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(nLog, 32l);
        dateField = org.jbpm.process.audit.NodeInstanceLog.class.getDeclaredField("date");
        dateField.setAccessible(true);
        dateField.set(nLog, new Date());
        nLog.setNodeType("type");
        nLog.setWorkItemId(88l);
        nLog.setConnection("connex");
        nLog.setExternalId("domain");
        resp.getHistoryLogList().add(new JaxbNodeInstanceLog(nLog));

        testRoundTrip(resp);
    }

    @Test
    public void workItemObjectTest() throws Exception {
        // Don't run with YAML?
        Assume.assumeFalse(getType().equals(TestType.YAML));

        JaxbWorkItemResponse workitemObject = new JaxbWorkItemResponse();
        workitemObject.setId(35l);
        workitemObject.setName("Clau");
        workitemObject.setState(0);
        workitemObject.setProcessInstanceId(1l);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("test", "driving");
        workitemObject.setParameters(params);
        JaxbWorkItemResponse roundTripWorkItem = testRoundTrip(workitemObject);
        ComparePair.compareObjectsViaFields(workitemObject, roundTripWorkItem);
    }

    @Test
    // JBPM-4170
    public void nodeInstanceLogNpeTest() throws Exception {
        org.jbpm.process.audit.NodeInstanceLog nodeLog = new org.jbpm.process.audit.NodeInstanceLog();
        JaxbNodeInstanceLog jaxbNodeLog = new JaxbNodeInstanceLog(nodeLog);
        testRoundTrip(jaxbNodeLog);
    }

    @Test
    public void deploymentObjectsTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        // for test at end, fill during test
        JaxbDeploymentUnitList depUnitList = new JaxbDeploymentUnitList();

        // dep jobs
        JaxbDeploymentJobResult jaxbJob = new JaxbDeploymentJobResult();
        testRoundTrip(jaxbJob);

        // complex dep jobs
        KModuleDeploymentUnit kDepUnit = new KModuleDeploymentUnit("org", "jar", "1.0", "kbase", "ksession");
        kDepUnit.setStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);

        JaxbDeploymentUnit depUnit = new JaxbDeploymentUnit(kDepUnit.getGroupId(), kDepUnit.getArtifactId(),
                kDepUnit.getArtifactId());
        depUnit.setKbaseName(kDepUnit.getKbaseName());
        depUnit.setKsessionName(kDepUnit.getKsessionName());
        depUnit.setStrategy(kDepUnit.getStrategy());
        depUnit.setStatus(JaxbDeploymentStatus.NONEXISTENT);
        depUnitList.getDeploymentUnitList().add(depUnit);

        jaxbJob = new JaxbDeploymentJobResult(null, "test", depUnit, "deploy");
        jaxbJob.setIdentifier(23L);
        jaxbJob.setSuccess(false);
        JaxbDeploymentJobResult copyJaxbJob = testRoundTrip(jaxbJob);
        ComparePair.compareObjectsViaFields(jaxbJob, copyJaxbJob, "jobId", "identifier");

        depUnit = new JaxbDeploymentUnit("g", "a", "v");
        depUnit.setKbaseName("kbase");
        depUnit.setKsessionName("ksession");
        depUnit.setStatus(JaxbDeploymentStatus.DEPLOY_FAILED);
        depUnit.setStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
        depUnitList.getDeploymentUnitList().add(depUnit);

        JaxbDeploymentUnit copyDepUnit = testRoundTrip(depUnit);

        ComparePair.compareObjectsViaFields(depUnit, copyDepUnit, "identifier");

        JaxbDeploymentJobResult depJob = new JaxbDeploymentJobResult(null, "testing stuff", copyDepUnit, "test");
        depJob.setSuccess(true);
        JaxbDeploymentJobResult copyDepJob = testRoundTrip(depJob);

        ComparePair.compareObjectsViaFields(copyDepJob, depJob, "jobId", "identifier");

        JaxbDeploymentUnitList roundTripUnitList = testRoundTrip(depUnitList);
        ComparePair.compareObjectsViaFields(depUnitList.getDeploymentUnitList().get(0), roundTripUnitList.getDeploymentUnitList()
                .get(0), "jobId", "identifier");
    }

    @Test
    public void processInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        org.jbpm.process.audit.ProcessInstanceLog origLog = new org.jbpm.process.audit.ProcessInstanceLog(54,
                "org.hospital.patient.triage");
        origLog.setDuration(65l);
        origLog.setDuration(234l);
        origLog.setEnd(new Date((new Date()).getTime() + 1000));
        origLog.setExternalId("testDomainId");
        origLog.setIdentity("identityNotMemory");
        origLog.setProcessInstanceDescription("What a process, say!");

        // nullable
        origLog.setStatus(2);
        origLog.setOutcome("descriptiveErrorCodeOfAnError");
        origLog.setParentProcessInstanceId(65l);

        origLog.setProcessName("org.process.not.technical");
        origLog.setProcessVersion("v3.14");

        JaxbProcessInstanceLog xmlLog = new JaxbProcessInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        JaxbProcessInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareObjectsViaFields(xmlLog, newXmlLog, "id");

        ProcessInstanceLog newLog = newXmlLog.getResult();
        ProcessInstanceLog origCmpLog = (ProcessInstanceLog) origLog;
        assertEquals(origLog.getExternalId(), newLog.getExternalId());
        assertEquals(origCmpLog.getIdentity(), newLog.getIdentity());
        assertEquals(origCmpLog.getOutcome(), newLog.getOutcome());
        assertEquals(origCmpLog.getProcessId(), newLog.getProcessId());
        assertEquals(origCmpLog.getProcessName(), newLog.getProcessName());
        assertEquals(origCmpLog.getProcessVersion(), newLog.getProcessVersion());
        assertEquals(origCmpLog.getDuration(), newLog.getDuration());
        assertEquals(origCmpLog.getEnd(), newLog.getEnd());
        assertEquals(origCmpLog.getParentProcessInstanceId(), newLog.getParentProcessInstanceId());
        assertEquals(origCmpLog.getProcessInstanceId(), newLog.getProcessInstanceId());
        assertEquals(origCmpLog.getStart(), newLog.getStart());
        assertEquals(origCmpLog.getStatus(), newLog.getStatus());
    }

    @Test
    public void processInstanceLogNillable() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        org.jbpm.process.audit.ProcessInstanceLog origLog = new org.jbpm.process.audit.ProcessInstanceLog(54,
                "org.hospital.patient.triage");
        origLog.setDuration(65l);
        origLog.setEnd(new Date((new Date()).getTime() + 1000));
        origLog.setExternalId("testDomainId");
        origLog.setIdentity("identityNotMemory");

        // nullable/nillable
        // origLog.setStatus(2);
        // origLog.setOutcome("descriptiveErrorCodeOfAnError");
        // origLog.setParentProcessInstanceId(65l);

        origLog.setProcessName("org.process.not.technical");
        origLog.setProcessVersion("v3.14");

        JaxbProcessInstanceLog xmlLog = new JaxbProcessInstanceLog(origLog);
        JaxbProcessInstanceLog newXmlLog = testRoundTrip(xmlLog);

        assertEquals(xmlLog.getProcessInstanceId(), newXmlLog.getProcessInstanceId());
        assertEquals(xmlLog.getProcessId(), newXmlLog.getProcessId());

        assertEquals(xmlLog.getDuration(), newXmlLog.getDuration());
        assertEquals(xmlLog.getEnd(), newXmlLog.getEnd());
        assertEquals(xmlLog.getExternalId(), newXmlLog.getExternalId());
        assertEquals(xmlLog.getIdentity(), newXmlLog.getIdentity());

        assertEquals(xmlLog.getStatus(), newXmlLog.getStatus());
        assertEquals(xmlLog.getOutcome(), newXmlLog.getOutcome());
        assertEquals(xmlLog.getParentProcessInstanceId(), newXmlLog.getParentProcessInstanceId());

        assertEquals(xmlLog.getProcessName(), newXmlLog.getProcessName());
        assertEquals(xmlLog.getProcessVersion(), newXmlLog.getProcessVersion());
    }

    @Test
    public void nodeInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        int type = 0;
        long processInstanceId = 23;
        String processId = "org.hospital.doctor.review";
        String nodeInstanceId = "1-1";
        String nodeId = "1";
        String nodeName = "notification";

        org.jbpm.process.audit.NodeInstanceLog origLog = new org.jbpm.process.audit.NodeInstanceLog(type, processInstanceId,
                processId, nodeInstanceId, nodeId, nodeName);

        origLog.setWorkItemId(78l);
        origLog.setConnection("link");
        origLog.setExternalId("not-internal-num");
        origLog.setNodeType("the-sort-of-point");

        JaxbNodeInstanceLog xmlLog = new JaxbNodeInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        xmlLog.setId(2l);
        JaxbNodeInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareOrig(xmlLog, newXmlLog, JaxbNodeInstanceLog.class);

        NodeInstanceLog newLog = newXmlLog.getResult();
        ComparePair.compareOrig((NodeInstanceLog) origLog, newLog, NodeInstanceLog.class);
    }

    @Test
    public void variableInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));

        long processInstanceId = 23;
        String processId = "org.hospital.intern.rounds";
        String variableInstanceId = "patientNum-1";
        String variableId = "patientNum";
        String value = "33";
        String oldValue = "32";

        org.jbpm.process.audit.VariableInstanceLog origLog = new org.jbpm.process.audit.VariableInstanceLog(processInstanceId,
                processId, variableInstanceId, variableId, value, oldValue);

        origLog.setExternalId("outside-identity-representation");
        origLog.setOldValue("previous-data-that-this-variable-contains");
        origLog.setValue("the-new-data-that-has-been-put-in-this-variable");
        origLog.setVariableId("shortend-representation-of-this-representation");
        origLog.setVariableInstanceId("id-instance-variable");

        JaxbVariableInstanceLog xmlLog = new JaxbVariableInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        JaxbVariableInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareObjectsViaFields(xmlLog, newXmlLog, "id");

        VariableInstanceLog newLog = newXmlLog.getResult();
        ComparePair.compareOrig((VariableInstanceLog) origLog, newLog, VariableInstanceLog.class);
    }

    @Test
    public void processIdAndProcessDefinitionTest() throws Exception {
        // JaxbProcessDefinition
        ProcessAssetDesc assetDesc = new ProcessAssetDesc("org.test.proc.id", "The Name Of The Process", "1.999.23.Final",
                "org.test.proc", "RuleFlow", KnowledgeType.PROCESS.toString(), "org.test.proc", "org.test.proc:procs:1.999.Final");

        JaxbProcessDefinition jaxbProcDef = new JaxbProcessDefinition();
        jaxbProcDef.setDeploymentId(assetDesc.getDeploymentId());
        jaxbProcDef.setId(assetDesc.getId());
        jaxbProcDef.setName(assetDesc.getName());
        jaxbProcDef.setPackageName(assetDesc.getPackageName());
        jaxbProcDef.setVersion(assetDesc.getVersion());
        Map<String, String> forms = new HashMap<String, String>();
        forms.put("locationForm", "GPS: street: post code: city: state: land: planet: universe: ");
        jaxbProcDef.setForms(forms);

        JaxbProcessDefinition copyJaxbProcDef = testRoundTrip(jaxbProcDef);
        ComparePair.compareObjectsViaFields(jaxbProcDef, copyJaxbProcDef);
    }

    @Test
    public void deploymentDescriptorTest() throws Exception {
        JaxbDeploymentDescriptor depDescriptor = new JaxbDeploymentDescriptor();

        depDescriptor.setAuditMode(AuditMode.JMS);
        depDescriptor.setAuditPersistenceUnit("myDatabasePersistenceUnit");
        String[] classes = { "org.test.First", "org.more.test.Second" };
        depDescriptor.setClasses(Arrays.asList(classes));

        depDescriptor.setConfiguration(getNamedObjectModeList("conf"));
        depDescriptor.setEnvironmentEntries(getNamedObjectModeList("envEnt"));

    }

    private List<NamedObjectModel> getNamedObjectModeList( String type ) {
        type = "-" + type;
        List<NamedObjectModel> namedObjectModelList = new ArrayList<NamedObjectModel>();
        for( int i = 0; i < 2; ++i ) {
            NamedObjectModel nom = new NamedObjectModel();
            nom.setIdentifier("id-" + i + type);
            nom.setName("name-" + i + type);
            String[] params = { UUID.randomUUID().toString(), UUID.randomUUID().toString() };
            List<Object> paramList = new ArrayList<Object>();
            paramList.addAll(Arrays.asList(params));
            nom.setParameters(paramList);
            nom.setResolver(i + "-resolver" + type);
            namedObjectModelList.add(nom);
        }
        return namedObjectModelList;
    }

    @Test
    public void funnyCharactersTest() throws Exception {
        String testStr = "test &<>\"\' test";
        JaxbString jaxbStr = new JaxbString(testStr);

        JaxbString copy = testRoundTrip(jaxbStr);
        assertEquals("Funny characters not correctly encoded", testStr, copy.getValue());
    }

    @Test
    public void correlationKeyTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        JaxbCorrelationKey corrKey = new JaxbCorrelationKey();
        corrKey.setName("anton");
        List<JaxbCorrelationProperty> properties = new ArrayList<JaxbCorrelationProperty>(3);
        corrKey.setJaxbProperties(properties);
        properties.add(new JaxbCorrelationProperty("name", "value"));
        properties.add(new JaxbCorrelationProperty("only-a-value"));
        properties.add(new JaxbCorrelationProperty("ngalan", "bili"));

        JaxbCorrelationKey copyCorrKey = testRoundTrip(corrKey);
        
        assertEquals("name", corrKey.getName(), copyCorrKey.getName());
        assertEquals("prop list size", corrKey.getProperties().size(), copyCorrKey.getProperties().size());
        List<CorrelationProperty<?>> propList = corrKey.getProperties();
        List<CorrelationProperty<?>> copyPropList = copyCorrKey.getProperties();
        for( int i = 0; i < propList.size(); ++i ) {
            CorrelationProperty<?> prop = propList.get(i);
            CorrelationProperty<?> copyProp = copyPropList.get(i);
            assertEquals(i + ": name", prop.getName(), copyProp.getName());
            assertEquals(i + ": type", prop.getType(), copyProp.getType());
            assertEquals(i + ": value", prop.getValue(), copyProp.getValue());
        }
    }
}
