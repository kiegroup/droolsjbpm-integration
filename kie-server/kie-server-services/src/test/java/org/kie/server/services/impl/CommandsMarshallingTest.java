package org.kie.server.services.impl;

import com.thoughtworks.xstream.XStream;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.AgendaGroupSetFocusCommand;
import org.drools.core.command.runtime.rule.DeleteCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.GetObjectsCommand;
import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.ModifyCommand;
import org.drools.core.command.runtime.rule.QueryCommand;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandsMarshallingTest {
    private XStream marshaller = XStreamXml.newXStreamMarshaller(Thread.currentThread().getContextClassLoader());

    @Test
    public void testMarshallInsertObjectCommand() {
        String xmlCommand = "<insert>\n" +
                "  <string>String value</string>\n" +
                "</insert>";
        InsertObjectCommand command = (InsertObjectCommand) marshaller.fromXML(xmlCommand);
        assertEquals("String value", command.getObject().toString());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallRetractCommand() {
        String xmlCommand = "<retract fact-handle=\"0:234:345:456:567:789\"/>";
        DeleteCommand command = (DeleteCommand) marshaller.fromXML(xmlCommand);
        assertEquals("0:234:345:456:567:789:NON_TRAIT", command.getFactHandle().toExternalForm());

        assertEquals("<retract fact-handle=\"0:234:345:456:567:789:NON_TRAIT\"/>", marshaller.toXML(command));
    }

    @Test
    public void testMarshallModifyCommand() {
        String xmlCommand = "<modify fact-handle=\"0:234:345:456:567:789\">\n" +
                "  <set accessor=\"age\" value=\"30\"/>\n" +
                "</modify>";
        ModifyCommand command = (ModifyCommand) marshaller.fromXML(xmlCommand);
        assertEquals(1, command.getSetters().size());

        assertEquals("<modify fact-handle=\"0:234:345:456:567:789:NON_TRAIT\">\n" +
                "  <set accessor=\"age\" value=\"30\"/>\n" +
                "</modify>", marshaller.toXML(command));
    }

    @Test
    public void testMarshallGetObjectCommand() {
        String xmlCommand = "<get-object fact-handle=\"0:234:345:456:567:789\" out-identifier=\"test\"/>";
        GetObjectCommand command = (GetObjectCommand) marshaller.fromXML(xmlCommand);
        assertEquals("test", command.getOutIdentifier());

        assertEquals("<get-object fact-handle=\"0:234:345:456:567:789:NON_TRAIT\" out-identifier=\"test\"/>", marshaller.toXML(command));
    }

    @Test
    public void testMarshallInsertElementsCommand() {
        String xmlCommand = "<insert-elements>\n" +
                "  <string>test1</string>\n" +
                "  <string>test2</string>\n" +
                "</insert-elements>";
        InsertElementsCommand command = (InsertElementsCommand) marshaller.fromXML(xmlCommand);
        assertEquals(2, command.getObjects().size());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallFireAllRulesCommand() {
        String xmlCommand = "<fire-all-rules max=\"10\" out-identifier=\"result\"/>";
        FireAllRulesCommand command = (FireAllRulesCommand) marshaller.fromXML(xmlCommand);
        assertEquals(10, command.getMax());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallStartProcessCommand() {
        String xmlCommand = "<start-process processId=\"org.drools.task.processOne\" out-identifier=\"id\"/>";
        StartProcessCommand command = (StartProcessCommand) marshaller.fromXML(xmlCommand);
        assertEquals("org.drools.task.processOne", command.getProcessId());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallQueryCommand() {
        String xmlCommand = "<query out-identifier=\"persons-out\" name=\"persons\"/>";
        QueryCommand command = (QueryCommand) marshaller.fromXML(xmlCommand);
        assertEquals("persons", command.getName());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallSetGlobalCommand() {
        String xmlCommand = "<set-global identifier=\"helper\" out-identifier=\"output\">\n" +
                "  <list/>\n" +
                "</set-global>";
        SetGlobalCommand command = (SetGlobalCommand) marshaller.fromXML(xmlCommand);
        assertEquals("helper", command.getIdentifier());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallGetGlobalCommand() {
        String xmlCommand = "<get-global identifier=\"helper\" out-identifier=\"helperOutput\"/>";
        GetGlobalCommand command = (GetGlobalCommand) marshaller.fromXML(xmlCommand);
        assertEquals("helper", command.getIdentifier());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    public void testMarshallGetObjectsCommand() {
        String xmlCommand = "<get-objects out-identifier=\"objects\"/>";
        GetObjectsCommand command = (GetObjectsCommand) marshaller.fromXML(xmlCommand);
        assertEquals("objects", command.getOutIdentifier());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    @Test
    @Ignore("Set focus command not yet supported")
    public void testMarshallAgendaGroupSetFocusCommand() {
        String xmlCommand = "<agenda-group-set-focus name=\"my-agenda-group\"/>";
        AgendaGroupSetFocusCommand command = (AgendaGroupSetFocusCommand) marshaller.fromXML(xmlCommand);
        assertEquals("my-agenda-group", command.getName());

        assertEquals(xmlCommand, marshaller.toXML(command));
    }

    // TODO determine what other commands are supported and add tests for them

}
