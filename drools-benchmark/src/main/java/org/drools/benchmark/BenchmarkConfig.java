/*
 * Copyright 2011 JBoss Inc
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

package org.drools.benchmark;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class BenchmarkConfig implements Iterable<BenchmarkDefinition> {

    private List<BenchmarkDefinition> benchmarks;

    private int delay = 10;

    public BenchmarkConfig(String configFile) {
        benchmarks = parse(configFile);
    }

    public Iterator<BenchmarkDefinition> iterator() {
        return benchmarks.iterator();
    }

    public int getDelay() {
        return delay;
    }

    private List<BenchmarkDefinition> parse(String configFile) {
        try {
            InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(configFile);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
            Element root = doc.getDocumentElement();
            root.normalize();
            parseRootAttrs(root);
            return parseBenchmarks(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void parseRootAttrs(Element root) throws Exception {
        delay = getAttributeValueAsInt(root, "delay", 10);
    }

    private List<BenchmarkDefinition> parseBenchmarks(Element root) throws Exception {
        NodeList nodes = root.getElementsByTagName("benchmark");
        List<BenchmarkDefinition> bs = new ArrayList(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) bs.add(parse((Element)nodes.item(i)));
        return bs;
    }

    private BenchmarkDefinition parse(Element element) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String className = element.getAttribute("classname");
        List<String> args = getTagValues(element, "arg");
        Constructor<?> constructor = getConstructorForArgs(className, args);

        String description = element.getAttribute("description");
        int repetitions = getAttributeValueAsInt(element, "repetitions", 1);
        String en = element.getAttribute("enabled");
        boolean enabled = en.isEmpty() || !en.trim().toLowerCase().equals("false");

        return new BenchmarkDefinition(constructor, toArgs(constructor.getParameterTypes(), args))
                .setDescription(description)
                .setRepetitions(repetitions)
                .setEnabled(enabled);
    }

    private Constructor<?> getConstructorForArgs(String className, List<String> args) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        for (Constructor<?> c : clazz.getConstructors()) {
            if (c.getParameterTypes().length == args.size()) {
                return c;
            }
        }
        throw new RuntimeException("Unable to find a constructor for class " + className + " with the given arguments: " + args);
    }

    private Object[] toArgs(Class<?>[] types, List<String> args) {
        Object[] objs = new Object[types.length];
        for (int i = 0; i < types.length; i++) objs[i] = toArg(types[i], args.get(i));
        return objs;
    }

    private Object toArg(Class<?> type, String arg) {
        if (type == Integer.class || type == Integer.TYPE) return Integer.parseInt(arg);
        return arg;
    }

    private List<String> getTagValues(Element element, String name) {
        List values = new LinkedList<String>();
        NodeList children = element.getElementsByTagName(name);
        for (int i = 0; i < children.getLength(); i++) {
            values.add(children.item(i).getFirstChild().getNodeValue());
        }
        return values;
    }

    private int getAttributeValueAsInt(Element elem, String name, int def) {
        String value = elem.getAttribute(name);
        return value.isEmpty() ? def : Integer.parseInt(value);
    }
}
