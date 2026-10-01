/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel.embedded.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.apache.camel.Exchange;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.spi.DataFormat;

/**
 * Custom Camel {@link DataFormatDefinition} + {@link DataFormat} that performs XStream-based
 * XML/JSON marshalling for Drools command objects.  This replaces the {@code camel-xstream}
 * artifact which was dropped in Apache Camel 4.x.
 *
 * <p>The {@link XStream} instance is provided via {@link #setXstream(XStream)} after being
 * built by {@link XStreamXml#newXStreamMarshaller} or {@link XStreamJson#newJSonMarshaller}
 * (which both delegate to {@code BatchExecutionHelper}). This avoids having to reflectively
 * instantiate the Drools inner converter classes, which all require an {@link XStream}
 * constructor argument.</p>
 *
 * <p>Additional user-supplied custom converters (e.g. test-only {@code PersonConverter}) that
 * <em>do</em> have a no-arg constructor can be appended via {@link #addCustomConverter(String)};
 * they are registered on top of the pre-built XStream when {@link #getXstream()} is first
 * called.</p>
 */
public class DroolsXStreamDataFormat extends DataFormatDefinition implements DataFormat {

    /**
     * The fully-configured XStream instance (built by BatchExecutionHelper).
     * Null until {@link #setXstream(XStream)} is called, at which point any
     * pending custom converters are also applied.
     */
    private XStream xstream;

    /** True when this format was configured for JSON (set by {@link XStreamJson}). */
    private boolean json;

    /**
     * Class names of additional converters that have a no-arg constructor and should be
     * registered on top of the pre-built XStream.  Populated before {@link #setXstream} is
     * called; flushed during {@link #setXstream}.
     */
    private List<String> pendingCustomConverters;

    public DroolsXStreamDataFormat() {
        super("drools-xstream");
        // Register this instance as the runtime DataFormat so Camel's route engine
        // resolves getDataFormat() to this object without needing a separate factory.
        setDataFormat(this);
    }

    // -------------------------------------------------------------------------
    // DataFormatDefinition — wire this instance as its own runtime DataFormat
    // -------------------------------------------------------------------------

    @Override
    public DataFormatDefinition copyDefinition() {
        return shallowCopy();
    }

    // -------------------------------------------------------------------------
    // Service lifecycle (required by DataFormat extends Service)
    // -------------------------------------------------------------------------

    @Override
    public void start() { }

    @Override
    public void stop() { }

    // -------------------------------------------------------------------------
    // DataFormat — marshal / unmarshal
    // -------------------------------------------------------------------------

    @Override
    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        XStream xs = getXstream();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        ClassLoader prev = xs.getClassLoader();
        if (ccl != null && ccl != prev) {
            xs.setClassLoader(ccl);
        }
        try {
            xs.toXML(graph, stream);
        } finally {
            if (ccl != null && ccl != prev) {
                xs.setClassLoader(prev);
            }
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        XStream xs = getXstream();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        ClassLoader prev = xs.getClassLoader();
        if (ccl != null && ccl != prev) {
            xs.setClassLoader(ccl);
        }
        try {
            return xs.fromXML(stream);
        } finally {
            if (ccl != null && ccl != prev) {
                xs.setClassLoader(prev);
            }
        }
    }

    // -------------------------------------------------------------------------
    // XStream access
    // -------------------------------------------------------------------------

    /**
     * Returns the configured {@link XStream} instance.  Throws if {@link #setXstream(XStream)}
     * has not been called yet.
     */
    public XStream getXstream() {
        if (xstream == null) {
            throw new IllegalStateException(
                    "DroolsXStreamDataFormat: XStream instance has not been set. "
                    + "Call XStreamXml.newXStreamMarshaller() or XStreamJson.newJSonMarshaller() first.");
        }
        return xstream;
    }

    /**
     * Sets the pre-built {@link XStream} instance and flushes any pending custom converters
     * that were registered via {@link #addCustomConverter(String)}.
     */
    public void setXstream(XStream xstream) {
        xstream.addPermission(AnyTypePermission.ANY);
        this.xstream = xstream;

        if (pendingCustomConverters != null) {
            for (String className : pendingCustomConverters) {
                registerCustomConverter(xstream, className);
            }
            pendingCustomConverters = null;
        }
    }

    // -------------------------------------------------------------------------
    // Custom converter registration (no-arg ctor converters only)
    // -------------------------------------------------------------------------

    /**
     * Appends a converter (identified by its fully-qualified class name) that has a
     * <em>no-arg constructor</em>.  If {@link #setXstream(XStream)} has already been called
     * the converter is registered immediately; otherwise it is queued.
     */
    public void addCustomConverter(String converterClassName) {
        if (xstream != null) {
            registerCustomConverter(xstream, converterClassName);
        } else {
            if (pendingCustomConverters == null) {
                pendingCustomConverters = new ArrayList<>();
            }
            pendingCustomConverters.add(converterClassName);
        }
    }

    /**
     * Convenience method that accepts a {@link List} of class names (mirrors the old
     * {@code XStreamDataFormat.setConverters()} API used in tests).
     */
    public void setConverters(List<String> converterClassNames) {
        for (String name : converterClassNames) {
            addCustomConverter(name);
        }
    }

    private static void registerCustomConverter(XStream xstream, String className) {
        try {
            Class<?> cls = Class.forName(className, false,
                    Thread.currentThread().getContextClassLoader());
            Converter converter = (Converter) cls.getDeclaredConstructor().newInstance();
            xstream.registerConverter(converter);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "DroolsXStreamDataFormat: cannot instantiate custom converter '" + className + "'", e);
        }
    }

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean json) {
        this.json = json;
    }

    // -------------------------------------------------------------------------
    // Cloning support (used by KiePolicy)
    // -------------------------------------------------------------------------

    /**
     * Returns a shallow copy that shares the same underlying {@link XStream} instance
     * (XStream itself is thread-safe for reading).
     */
    public DroolsXStreamDataFormat shallowCopy() {
        DroolsXStreamDataFormat copy = new DroolsXStreamDataFormat();
        copy.xstream = this.xstream;
        copy.json = this.json;
        copy.pendingCustomConverters = this.pendingCustomConverters != null
                ? new ArrayList<>(this.pendingCustomConverters) : null;
        copy.setDataFormat(copy);
        return copy;
    }
}
