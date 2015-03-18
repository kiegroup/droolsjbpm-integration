/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class EAPFileUtils {

    public static void writeToFile(InputStream inputStream, FileOutputStream outputStream) throws IOException {
        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void writeToFile(InputStream inputStream, String fileNem) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(fileNem);
        writeToFile(inputStream, outputStream);
    }

    public static void writeToFile(String str, File f) throws IOException {
        writeToFile(new ByteArrayInputStream(str.getBytes("UTF-8")), f);
    }

    public static void writeToFile(String str, String fileName) throws IOException {
        writeToFile(new ByteArrayInputStream(str.getBytes("UTF-8")), fileName);
    }

    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        writeToFile(inputStream, outputStream);
    }

    public static File writeFile(File path, String fileName, String fileContent) throws IOException {
        if (fileContent != null && fileContent.trim().length() > 0) {
            path.mkdirs();
            File out = new File(path, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));
            writer.write (fileContent);

            //Close writer
            writer.close();
            return out;
        }
        return null;
    }

    public static void removeFirstLineIfDuplicated(File path) throws IOException {
        Scanner scanner = new Scanner(path);
        ArrayList<String> coll = new ArrayList<String>();
        String firstLine = scanner.nextLine();
        if (scanner.hasNextLine()) {
            String secondLine = scanner.nextLine();
            if (!firstLine.equalsIgnoreCase(secondLine)) coll.add(firstLine);
            coll.add(secondLine);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            coll.add(line);
        }

        scanner.close();

        FileWriter writer = new FileWriter(path);
        for (String line : coll) {
            writer.write(line);
        }

        writer.close();
    }

    public static String extractFileName(String path) {
        if (path == null || path.trim().length() == 0) return null;

        int fileNameIndex = path.lastIndexOf(File.separator);
        int fileNameExtIndex = path.lastIndexOf(".");

        return path.substring(fileNameIndex + 1, fileNameExtIndex);
    }

    public static String getStringFromInputStream(InputStream is) throws Exception {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return sb.toString();

    }

    /**
     * Creates a JAR file and add the file <code>fileToAdd</code> into the JAR.
     * @param outPath The output directory for the JAR file.
     * @param fileName The JAR file name.
     * @param fileToAdd The file to add into the JAR.
     * @param entryName The JAR entry name of the file to add into the JAR. 
     * @return The generated JAR file.
     * @throws IOException
     */
    public static File createJarFile(String outPath, String fileName, File fileToAdd, String entryName) throws IOException {
        if (outPath == null || fileToAdd == null) return null;
        
        File outFile = new File(outPath, fileName);

        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(outFile));
        JarOutputStream jo = new JarOutputStream(bo);

        BufferedInputStream bi = new BufferedInputStream(new FileInputStream(fileToAdd));

        JarEntry je = new JarEntry(entryName);
        jo.putNextEntry(je);

        byte[] buf = new byte[1024];
        int anz;

        while ((anz = bi.read(buf)) != -1) {
            jo.write(buf, 0, anz);
        }

        bi.close();
        jo.close();
        bo.close();
        
        return outFile;
    }
}
