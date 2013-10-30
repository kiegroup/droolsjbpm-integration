package org.kie.example.api.kiemavenexample;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class KieMavenPluginExampleTest {

    @Test
    public void testGo() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new KieMavenPluginExample().go(ps);
        ps.close();

        String actual = new String(baos.toByteArray());
        String expected = "" +
                          "Dave: Hello, HAL. Do you read me, HAL?\n" +
                          "HAL: Dave. I read you.\n";
        assertEquals(expected, actual);
    }
}
