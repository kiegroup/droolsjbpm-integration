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

package org.kie.remote.services.rest.jaxb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.Test;

public class JavaCompilerTest {

    @Test
    public void testMoreCompliation() throws Exception {
        // compile class
        Class<?> cls = getClassFromSource("NewMyType.java", "MyType",
                                          "NewMyTypeChild.java", "MyTypeChild");

        // verify definition
        cls.getMethod("getNotText");

        // compile class with same name
        cls = getClassFromSource("MyType.java", "MyType",
                                 "MyTypeChild.java", "MyTypeChild");

        // verify definition
        try {
            cls.getMethod("getNotText");
            fail( "The getNotText method should NOT exist here!");
        } catch(Exception e ) {
            // ignore
        }
        cls.getMethod("getText");
    }

    /**
     * (runtime) Compilation of a source file (see the *.java files in src/test/resources)
     * @param fileAndTypeNames The names of the file to be compiled
     * @return A {@link Class} instance from the source in the file
     * @throws Exception
     */
    public static Class getClassFromSource(String... fileAndTypeNames) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File root = new File(tmpDir, "kie-services-remote-tests");
        String [] paths = new String[fileAndTypeNames.length/2];

        for( int i = 0;  i < fileAndTypeNames.length; i += 2 ) {
            String fileName = fileAndTypeNames[i];
            String typeName = fileAndTypeNames[i+1];

            String source = getSource(fileName);

            // Save source in .java file.
            File sourceFile = new File(root, "org/kie/remote/services/rest/jaxb/" + typeName + ".java");
            if( sourceFile.exists() ) {
                sourceFile.delete();
            }
            sourceFile.getParentFile().mkdirs();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(sourceFile);
                fileWriter.append(source);
            } finally {
                if( fileWriter != null ) {
                    fileWriter.close();
                }
            }
            paths[i/2] = sourceFile.getPath();
            sourceFile.deleteOnExit();
        }

        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, paths);

        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { root.toURI().toURL() });
        Class<?> cls = Class.forName("org.kie.remote.services.rest.jaxb.MyType", true, classLoader);

        // cleanup
        root.deleteOnExit();

        return cls;
    }

    /**
     * Read the file into a String
     * @param fileName The filename (in sr/test/resources)
     * @return The contents of the file
     * @throws Exception
     */
    private static String getSource(String fileName) throws Exception {
        URL fileUrl = JavaCompilerTest.class.getResource("/" + fileName);
        assertNotNull(fileUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStreamReader input = null;
        try {
            URI fileUri = fileUrl.toURI();
            input = new InputStreamReader(new FileInputStream(new File(fileUri)));

        OutputStreamWriter output = new OutputStreamWriter(baos);
        char[] buffer = new char[4096];
        int n = 0;
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        } finally {
            if( input != null ) {
                input.close();
            }
        }
        return baos.toString();
    }
}
