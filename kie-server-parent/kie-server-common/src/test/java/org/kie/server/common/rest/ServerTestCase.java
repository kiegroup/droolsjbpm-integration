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
/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.kie.server.common.rest;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.kie.server.common.rest.KieServerHttpRequest.CHARSET_UTF8;

/**
 * Base test case that provides a running HTTP server
 * </p>
 * Copied (stolen!) from Kevin Sawicki's brilliant http-request project: https://github.com/kevinsawicki/http-request
 */
public class ServerTestCase {

    /**
     * Simplified handler
     */
    protected static abstract class RequestHandler extends Handler.Abstract {

        private Request request;

        private Response response;

        /**
         * Handle request
         *
         * @param request
         * @param response
         */
        public abstract void handle(Request request, Response response) throws Exception;

        /**
         * Set instance fields and delegate to {@link #handle(Request, Response)}.
         * Use this instead of calling {@link #handle(Request, Response)} directly so that
         * {@link #read()} and {@link #write(String)} can access the current request/response.
         */
        protected final void invokeWith(Request request, Response response) throws Exception {
            this.request = request;
            this.response = response;
            handle(request, response);
        }

        /**
         * Read content
         *
         * @return content
         */
        protected byte[] read() {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8196];
            int read;
            try {
                InputStream input = Content.Source.asInputStream(request);
                while ((read = input.read(buffer)) != -1)
                    content.write(buffer, 0, read);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return content.toByteArray();
        }

        /**
         * Write value
         *
         * @param value
         */
        protected void write(String value) {
            try {
                byte[] bytes = value.getBytes(CHARSET_UTF8);
                response.getHeaders().put(HttpHeader.CONTENT_LENGTH, bytes.length);
                response.write(true, java.nio.ByteBuffer.wrap(bytes), Callback.NOOP);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Write line
         *
         * @param value
         */
        protected void writeln(String value) {
            write(value + System.lineSeparator());
        }

        @Override
        public boolean handle(final Request request,
                              final Response response,
                              final Callback callback) throws Exception {
            invokeWith(request, response);
            callback.succeeded();
            return true;
        }

    }

    /**
     * Server
     */
    protected static Server server;
    protected static Server proxy;
    protected static int proxyPort;
    protected static final AtomicInteger proxyHitCount = new AtomicInteger(0);
    protected static final AtomicReference<String> proxyUser = new AtomicReference<String>();
    protected static final AtomicReference<String> proxyPassword = new AtomicReference<String>();

    /**
     * Set up server with handler
     *
     * @param handler
     * @return port
     * @throws Exception
     */
    public static String setUp(final Handler handler) throws Exception {
        server = new Server();
        if (handler != null)
            server.setHandler(handler);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.setConnectors(new Connector[]{connector});
        server.start();

        setupProxy();

        return "http://localhost:" + connector.getLocalPort();
    }

    public static void setupProxy() throws Exception {
        proxy = new Server();
        ServerConnector proxyConnector = new ServerConnector(proxy);
        proxyConnector.setPort(0);
        proxy.setConnectors(new Connector[]{proxyConnector});

        Handler.Sequence handlerList = new Handler.Sequence(
                new RequestHandler() {
                    @Override
                    public void handle(Request request, Response response) {
                        proxyHitCount.incrementAndGet();
                        String auth = request.getHeaders().get("Proxy-Authorization");
                        if (auth != null) {
                            auth = auth.substring(auth.indexOf(' ') + 1);
                            try {
                                auth = new String(Base64.getDecoder().decode(auth), CHARSET_UTF8);
                                int colon = auth.indexOf(':');
                                proxyUser.set(auth.substring(0, colon));
                                proxyPassword.set(auth.substring(colon + 1));
                            } catch (UnsupportedEncodingException e) {
                                response.setStatus(HTTP_BAD_REQUEST);
                            }
                        }
                    }
                },
                new org.eclipse.jetty.proxy.ProxyHandler.Reverse(
                        request -> org.eclipse.jetty.http.HttpURI.build(request.getHttpURI())
                                .scheme("http")
                                .host("localhost")
                                .port(server.getURI().getPort())));
        proxy.setHandler(handlerList);

        proxy.start();
        proxyPort = proxyConnector.getLocalPort();
    }

    @Before
    public void clearProxyHitCount() {
        proxyHitCount.set(0);
    }

    /**
     * Tear down server if created
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        if (server != null)
            server.stop();
        if (proxy != null)
            proxy.stop();
    }
}
