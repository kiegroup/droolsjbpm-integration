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
 * Copyright (c) 2014 Kevin Sawicki <kevinsawicki@gmail.com>
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



import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.HttpHeaders.IF_NONE_MATCH;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.server.common.rest.KieServerHttpRequest.CHARSET_UTF8;
import static org.kie.server.common.rest.KieServerHttpRequest.appendQueryParameters;
import static org.kie.server.common.rest.KieServerHttpRequest.deleteRequest;
import static org.kie.server.common.rest.KieServerHttpRequest.getRequest;
import static org.kie.server.common.rest.KieServerHttpRequest.newRequest;
import static org.kie.server.common.rest.KieServerHttpRequest.postRequest;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.B64Code;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests of {@link KieServerHttpRequest}
 */
public class KieServerHttpRequestTest extends ServerTestCase {

    private static String url;

    private static RequestHandler handler;

    /**
     * Set up server
     *
     * @throws Exception
     */
    @BeforeClass
    public static void startServer() throws Exception {
        url = setUp(new RequestHandler() {

            @Override
            public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
                    throws IOException, ServletException {
                if( handler != null )
                    handler.handle(target, baseRequest, request, response);
            }

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                if( handler != null )
                    handler.handle(request, response);
            }
        });
    }

    /**
     * Clear handler
     */
    @After
    public void clearHandler() {
        handler = null;
    }

    /**
     * Create request with malformed URL
     */
    @Test(expected = KieServerHttpRequestException.class)
    public void malformedStringUrlTest() {
        KieServerHttpRequest.newRequest("\\m/");
    }

    /**
     * Create request with malformed URL
     */
    @Test
    public void malformedStringUrlCauseTest() {
        try {
            KieServerHttpRequest.newRequest("\\m/");
            fail("Exception not thrown");
        } catch( KieServerHttpRequestException e ) {
            assertNotNull(e.getCause());
        }
    }

    /**
     * Set request buffer size to negative value
     */
    @Test(expected = IllegalArgumentException.class)
    public void negativeBufferSize() {
        KieServerHttpRequest.newRequest("http://localhost").bufferSize(-1);
    }

    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getEmptyTest() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        
        KieServerHttpRequest request = getRequest(new URL(url));
        assertNotNull(request.getConnection());
        assertEquals(30000, request.timeout(30000).getConnection().getReadTimeout());
        assertEquals(2500, request.bufferSize(2500).bufferSize());
        assertFalse(request.ignoreCloseExceptions(false).ignoreCloseExceptions());
        int code = request.get().response().code();
        assertEquals(200, code);
        assertEquals("GET", method.get());
        assertEquals("OK", request.response().message());
        assertEquals(HTTP_OK, code);
        assertEquals("", request.response().body());
        assertNotNull(request.toString());
        assertFalse(request.toString().length() == 0);
        assertEquals(request, request.disconnect());
        assertTrue(request.response().contentLength() == 0);
        assertEquals(request.getUrl().toString(), url);
        assertEquals("GET", request.getMethod());
    }

    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getUrlEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = getRequest(new URL(url));
        assertNotNull(request.getConnection());
        int code = request.response().code();
        assertEquals(200, code);
        assertEquals("GET", method.get());
        assertEquals("OK", request.response().message());
        assertEquals(HTTP_OK, code);
        assertEquals("", request.response().body());
    }

    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getNoContent() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_NO_CONTENT);
            }
        };
        KieServerHttpRequest request = getRequest(new URL(url));
        assertNotNull(request.getConnection());
        int code = request.response().code();
        assertEquals(HTTP_NO_CONTENT, code);
        assertEquals("GET", method.get());
        assertEquals("No Content", request.response().message());
        assertEquals(HTTP_NO_CONTENT, code);
        assertEquals("", request.response().body());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithSpace() throws Exception {
        String unencoded = "/a resource";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url + unencoded);
        assertEquals(200, request.get().response().code());
        assertEquals(unencoded, path.get());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithUnicode() throws Exception {
        String unencoded = "/\u00DF";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url + unencoded);
        assertEquals(200, request.get().response().code());
        assertEquals(unencoded, path.get());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithPercent() throws Exception {
        String unencoded = "/%";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url + unencoded);
        assertEquals(200, request.get().response().code());
        assertEquals(unencoded, path.get());
    }

    /**
     * Make a DELETE request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void deleteEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = deleteRequest(new URL(url));
        assertNotNull(request.getConnection());
        assertEquals(200, request.delete().response().code());
        assertEquals("DELETE", method.get());
        assertEquals("", request.response().body());
        assertEquals("DELETE", request.getMethod());
    }

    /**
     * Make a DELETE request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void deleteUrlEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = deleteRequest(new URL(url));
        assertNotNull(request.getConnection());
        assertEquals(200, request.response().code());
        assertEquals("DELETE", method.get());
        assertEquals("", request.response().body());
    }

    /**
     * Make a POST request with an empty request body
     *
     * @throws Exception
     */
    @Test
    public void postEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_CREATED);
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_CREATED, request.post().response().code());
        assertEquals("POST", method.get());
    }

    /**
     * Make a POST request with an empty request body
     *
     * @throws Exception
     */
    @Test
    public void postUrlEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                response.setStatus(HTTP_CREATED);
            }
        };
        KieServerHttpRequest request = getRequest(new URL(url));
        assertEquals(HTTP_CREATED, request.post().response().code());
        assertEquals("POST", method.get());
    }

    /**
     * Make a POST request with a non-empty request body
     *
     * @throws Exception
     */
    @Test
    public void postNonEmptyString() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        int code = postRequest(new URL(url)).body("hello").response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("hello", body.get());
    }

    /**
     * Make a post with an explicit set of the content length
     *
     * @throws Exception
     */
    @Test
    public void postWithLength() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        final AtomicReference<Integer> length = new AtomicReference<Integer>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                length.set(request.getContentLength());
                response.setStatus(HTTP_OK);
            }
        };
        String data = "hello";
        int sent = data.getBytes().length;
        int code = newRequest(new URL(url)).body(data).post().response().code();
        assertEquals(HTTP_OK, code);
        assertEquals(sent, length.get().intValue());
        assertEquals(data, body.get());
    }

    /**
     * Make a post of form data
     *
     * @throws Exception
     */
    @Test
    public void postForm() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        final AtomicReference<String> contentType = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                contentType.set(request.getContentType());
                response.setStatus(HTTP_OK);
            }
        };
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("name", "user");
        data.put("number", "100");
        int code = postRequest(new URL(url)).form(data).form("zip", "12345").response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("name=user&number=100&zip=12345", body.get());
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", contentType.get());
    }

    /**
     * Make a post of form data
     *
     * @throws Exception
     */
    @Test
    public void postFormWithNoCharset() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        final AtomicReference<String> contentType = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                contentType.set(request.getContentType());
                response.setStatus(HTTP_OK);
            }
        };
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("name", "user");
        data.put("number", "100");
        int code = postRequest(new URL(url)).form(data, null).form("zip", "12345").response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("name=user&number=100&zip=12345", body.get());
        assertEquals("application/x-www-form-urlencoded", contentType.get());
    }

    /**
     * Make a post with an empty form data map
     *
     * @throws Exception
     */
    @Test
    public void postEmptyForm() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        int code = postRequest(new URL(url)).form(new HashMap<String, String>()).response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("", body.get());
    }

    /**
     * Make a GET request for a non-empty response body
     *
     * @throws Exception
     */
    @Test
    public void getNonEmptyString() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                write("hello");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("hello", request.response().body());
        assertEquals("hello".getBytes().length, request.response().contentLength());
        assertFalse(request.response().contentLength() == 0);
    }

    /**
     * Make a GET request with a response that includes a charset parameter
     *
     * @throws Exception
     */
    @Test
    public void getWithResponseCharset() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setContentType("text/html; charset=UTF-8");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals(CHARSET_UTF8, request.response().charset());
    }

    /**
     * Make a GET request with a response that includes a charset parameter
     *
     * @throws Exception
     */
    @Test
    public void getWithResponseCharsetAsSecondParam() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setContentType("text/html; param1=val1; charset=UTF-8");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals(CHARSET_UTF8, request.response().charset());
    }

    /**
     * Make a GET request with basic authentication specified
     *
     * @throws Exception
     */
    @Test
    public void basicAuthentication() throws Exception {
        final AtomicReference<String> user = new AtomicReference<String>();
        final AtomicReference<String> password = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                String auth = request.getHeader("Authorization");
                auth = auth.substring(auth.indexOf(' ') + 1);
                auth = B64Code.decode(auth, CHARSET_UTF8);
                int colon = auth.indexOf(':');
                user.set(auth.substring(0, colon));
                password.set(auth.substring(colon + 1));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).basicAuthorization("user", "p4ssw0rd");
        assertEquals(200, request.post().response().code());
        assertEquals("user", user.get());
        assertEquals("p4ssw0rd", password.get());
    }

    /**
     * Make a GET request with basic proxy authentication specified
     *
     * @throws Exception
     */
    @Test
    @Ignore // add proxy functionality..?
    public void basicProxyAuthentication() throws Exception {
        final AtomicBoolean finalHostReached = new AtomicBoolean(false);
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                finalHostReached.set(true);
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).useProxy("localhost", proxyPort).proxyBasic("user", "p4ssw0rd");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("user", proxyUser.get());
        assertEquals("p4ssw0rd", proxyPassword.get());
        assertEquals(true, finalHostReached.get());
        assertEquals(1, proxyHitCount.get());
    }

    /**
     * Make a GET and get response body as byte array
     *
     * @throws Exception
     */
    @Test
    public void getBytes() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                write("hello");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertTrue(Arrays.equals("hello".getBytes(), request.response().bytes()));
    }

    /**
     * Make a GET request that returns an error string
     *
     * @throws Exception
     */
    @Test
    public void getError() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                write("error");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, request.get().response().code());
        assertEquals("error", request.response().body());
    }

    /**
     * Make a GET request that returns an empty error string
     *
     * @throws Exception
     */
    @Test
    public void noError() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("", request.response().body());
    }

    /**
     * Verify 'Content-Encoding' header
     *
     * @throws Exception
     */
    @Test
    public void contentEncodingHeader() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("Content-Encoding", "gzip");
            }
        };
        assertEquals("gzip", newRequest(url).get().response().contentEncoding());
    }

    /**
     * Verify 'Content-Type' header
     *
     * @throws Exception
     */
    @Test
    public void contentTypeHeader() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("Content-Type", "text/html");
            }
        };
        assertEquals("text/html", newRequest(url).get().response().contentType());
    }

    /**
     * Verify 'Content-Type' header
     *
     * @throws Exception
     */
    @Test
    public void requestContentTypeTest() throws Exception {
        final AtomicReference<String> contentType = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                contentType.set(request.getContentType());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).contentType("text/html", "UTF-8");
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("text/html; charset=UTF-8", contentType.get());
    }

    /**
     * Verify 'Content-Type' header
     *
     * @throws Exception
     */
    @Test
    public void requestContentTypeNullCharsetTest() throws Exception {
        final AtomicReference<String> contentType = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                contentType.set(request.getContentType());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).contentType("text/html", null);
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("text/html", contentType.get());
    }

    /**
     * Verify 'Content-Type' header
     *
     * @throws Exception
     */
    @Test
    public void requestContentTypeEmptyCharsetTest() throws Exception {
        final AtomicReference<String> contentType = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                contentType.set(request.getContentType());
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).contentType("text/html", "");
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("text/html", contentType.get());
    }

    /**
     * Verify setting headers
     *
     * @throws Exception
     */
    @Test
    public void headers() throws Exception {
        final AtomicReference<String> h1 = new AtomicReference<String>();
        final AtomicReference<String> h2 = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                h1.set(request.getHeader("h1"));
                h2.set(request.getHeader("h2"));
            }
        };
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("h1", "v1");
        headers.put("h2", "v2");
        KieServerHttpRequest request = newRequest(url).headers(headers);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("v1", h1.get());
        assertEquals("v2", h2.get());
    }

    /**
     * Verify setting headers
     *
     * @throws Exception
     */
    @Test
    public void emptyHeaders() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).headers(Collections.<String, String> emptyMap());
        assertEquals(HTTP_OK, request.get().response().code());
    }

    /**
     * Verify getting all headers
     *
     * @throws Exception
     */
    @Test
    public void getAllHeaders() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "a");
                response.setHeader("b", "b");
                response.addHeader("a", "another");
            }
        };
        Map<String, List<String>> headers = newRequest(url).get().response().headers();
        assertEquals(headers.size(), 6);
        assertEquals(headers.get("a").size(), 2);
        assertTrue(headers.get("b").get(0).equals("b"));
    }

    /**
     * Verify setting number header
     *
     * @throws Exception
     */
    @Test
    public void numberHeader() throws Exception {
        final AtomicReference<String> h1 = new AtomicReference<String>();
        final AtomicReference<String> h2 = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                h1.set(request.getHeader("h1"));
                h2.set(request.getHeader("h2"));
            }
        };
        KieServerHttpRequest request = newRequest(url).header("h1", 5).header("h2", (Number) null);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("5", h1.get());
        assertEquals(null, h2.get());
    }

    /**
     * Verify 'User-Agent' request header
     *
     * @throws Exception
     */
    @Test
    public void userAgentHeader() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("User-Agent"));
            }
        };
        KieServerHttpRequest request = newRequest(url).header(USER_AGENT, "browser 1.0");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("browser 1.0", header.get());
    }

    /**
     * Verify 'Accept' request header
     *
     * @throws Exception
     */
    @Test
    public void acceptHeader() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("Accept"));
            }
        };
        KieServerHttpRequest request = newRequest(url).accept("application/json");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("application/json", header.get());
    }

    /**
     * Verify 'Accept' request header when calling {@link KieServerHttpRequest#acceptJson()}
     *
     * @throws Exception
     */
    @Test
    public void acceptJson() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("Accept"));
            }
        };
        KieServerHttpRequest request = newRequest(url).accept(APPLICATION_JSON);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("application/json", header.get());
    }

    /**
     * Verify 'If-None-Match' request header
     *
     * @throws Exception
     */
    @Test
    public void ifNoneMatchHeader() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("If-None-Match"));
            }
        };
        KieServerHttpRequest request = newRequest(url).header(IF_NONE_MATCH, "eid");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("eid", header.get());
    }

    /**
     * Verify 'Accept-Charset' request header
     *
     * @throws Exception
     */
    @Test
    public void acceptCharsetHeader() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("Accept-Charset"));
            }
        };
        KieServerHttpRequest request = newRequest(url).acceptCharset(CHARSET_UTF8);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals(CHARSET_UTF8, header.get());
    }

    /**
     * Verify 'Accept-Encoding' request header
     *
     * @throws Exception
     */
    @Test
    public void acceptEncodingHeader() throws Exception {
        final AtomicReference<String> header = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                header.set(request.getHeader("Accept-Encoding"));
            }
        };
        KieServerHttpRequest request = newRequest(url).acceptEncoding("compress");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("compress", header.get());
    }

    /**
     * Verify certificate and host helpers on HTTPS connection
     *
     * @throws Exception
     */
    @Test
    public void httpsTrust() throws Exception {
        assertNotNull(getRequest("https://localhost").trustAllCerts().trustAllHosts());
    }

    /**
     * Verify certificate and host helpers ignore non-HTTPS connection
     *
     * @throws Exception
     */
    @Test
    public void httpTrust() throws Exception {
        assertNotNull(getRequest("http://localhost").trustAllCerts().trustAllHosts());
    }

    /**
     * Verify hostname verifier is set and accepts all
     */
    @Test
    public void verifierAccepts() {
        KieServerHttpRequest request = getRequest("https://localhost");
        HttpsURLConnection connection = (HttpsURLConnection) request.getConnection();
        request.trustAllHosts();
        assertNotNull(connection.getHostnameVerifier());
        assertTrue(connection.getHostnameVerifier().verify(null, null));
    }

    /**
     * Verify single hostname verifier is created across all calls
     */
    @Test
    public void singleVerifier() {
        KieServerHttpRequest request1 = getRequest("https://localhost").trustAllHosts();
        KieServerHttpRequest request2 = getRequest("https://localhost").trustAllHosts();
        assertNotNull(((HttpsURLConnection) request1.getConnection()).getHostnameVerifier());
        assertNotNull(((HttpsURLConnection) request2.getConnection()).getHostnameVerifier());
        assertEquals(((HttpsURLConnection) request1.getConnection()).getHostnameVerifier(),
                ((HttpsURLConnection) request2.getConnection()).getHostnameVerifier());
    }

    /**
     * Verify single SSL socket factory is created across all calls
     */
    @Test
    public void singleSslSocketFactory() {
        KieServerHttpRequest request1 = getRequest("https://localhost").trustAllCerts();
        KieServerHttpRequest request2 = getRequest("https://localhost").trustAllCerts();
        assertNotNull(((HttpsURLConnection) request1.getConnection()).getSSLSocketFactory());
        assertNotNull(((HttpsURLConnection) request2.getConnection()).getSSLSocketFactory());
        assertEquals(((HttpsURLConnection) request1.getConnection()).getSSLSocketFactory(),
                ((HttpsURLConnection) request2.getConnection()).getSSLSocketFactory());
    }

    /**
     * Make a GET request that should be compressed
     *
     * @throws Exception
     */
    @Test
    public void getGzipped() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                if( !"gzip".equals(request.getHeader("Accept-Encoding")) )
                    return;

                response.setHeader("Content-Encoding", "gzip");
                GZIPOutputStream output;
                try {
                    output = new GZIPOutputStream(response.getOutputStream());
                } catch( IOException e ) {
                    throw new RuntimeException(e);
                }
                try {
                    output.write("hello compressed".getBytes(CHARSET_UTF8));
                } catch( IOException e ) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        output.close();
                    } catch( IOException ignored ) {
                        // Ignored
                    }
                }
            }
        };
        KieServerHttpRequest request = getRequest(url).acceptEncoding("gzip").setUncompress(true);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("hello compressed", request.response().body());
    }

    /**
     * Make a GET request that should be compressed but isn't
     *
     * @throws Exception
     */
    @Test
    public void getNonGzippedWithUncompressEnabled() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                if( !"gzip".equals(request.getHeader("Accept-Encoding")) )
                    return;

                write("hello not compressed");
            }
        };
        KieServerHttpRequest request = getRequest(url).acceptEncoding("gzip").setUncompress(true);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("hello not compressed", request.response().body());
    }

    /**
     * Get header with multiple response values
     *
     * @throws Exception
     */
    @Test
    public void getHeaders() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.addHeader("a", "1");
                response.addHeader("a", "2");
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        String[] values = request.response().headers("a");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertTrue(Arrays.asList(values).contains("1"));
        assertTrue(Arrays.asList(values).contains("2"));
    }

    /**
     * Get header values when not set in response
     *
     * @throws Exception
     */
    @Test
    public void getEmptyHeaders() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        String[] values = request.response().headers("a");
        assertNotNull(values);
        assertEquals(0, values.length);
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getSingleParameterTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=d");
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("d", request.response().headerParameter("a", "c"));
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getMultipleParametersTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=d;e=f");
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("d", request.response().headerParameter("a", "c"));
        assertEquals("f", request.response().headerParameter("a", "e"));
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getSingleParameterQuotedTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=\"d\"");
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("d", request.response().headerParameter("a", "c"));
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getMultipleParametersQuotedTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=\"d\";e=\"f\"");
            }
        };
        KieServerHttpRequest request = getRequest(url);
        assertEquals(HTTP_OK, request.response().code());
        assertEquals("d", request.response().headerParameter("a", "c"));
        assertEquals("f", request.response().headerParameter("a", "e"));
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getMissingParameter() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=d");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertNull(request.response().headerParameter("a", "e"));
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getParameterFromMissingHeaderTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=d");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertNull(request.response().headerParameter("b", "c"));
        assertTrue(request.response().headerParameters("b").isEmpty());
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getEmptyParameterTest() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;c=");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertNull(request.response().headerParameter("a", "c"));
        assertTrue(request.response().headerParameters("a").isEmpty());
    }

    /**
     * Get header parameter value
     *
     * @throws Exception
     */
    @Test
    public void getEmptyParameters() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "b;");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        assertNull(request.response().headerParameter("a", "c"));
        assertTrue(request.response().headerParameters("a").isEmpty());
    }

    /**
     * Get header parameter values
     *
     * @throws Exception
     */
    @Test
    public void getParameters() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "value;b=c;d=e");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        Map<String, String> params = request.response().headerParameters("a");
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals("c", params.get("b"));
        assertEquals("e", params.get("d"));
    }

    /**
     * Get header parameter values
     *
     * @throws Exception
     */
    @Test
    public void getQuotedParameters() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "value;b=\"c\";d=\"e\"");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        Map<String, String> params = request.response().headerParameters("a");
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals("c", params.get("b"));
        assertEquals("e", params.get("d"));
    }

    /**
     * Get header parameter values
     *
     * @throws Exception
     */
    @Test
    public void getMixQuotedParameters() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_OK);
                response.setHeader("a", "value; b=c; d=\"e\"");
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertEquals(HTTP_OK, request.get().response().code());
        Map<String, String> params = request.response().headerParameters("a");
        assertNotNull(params);
        assertEquals(2, params.size());
        assertEquals("c", params.get("b"));
        assertEquals("e", params.get("d"));
    }

    /**
     * Verify sending form data as a sequence of {@link Entry} objects
     *
     * @throws Exception
     */
    @Test
    public void postFormAsEntries() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("name", "user");
        data.put("number", "100");
        KieServerHttpRequest request = newRequest(url).form(data);
        int code = request.post().response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("name=user&number=100", body.get());
    }

    /**
     * Verify sending form data where entry value is null
     *
     * @throws Exception
     */
    @Test
    public void postFormEntryWithNullValue() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        Map<String, String> data = new LinkedHashMap<String, String>();
        data.put("name", null);
        KieServerHttpRequest request = newRequest(url).form(data);
        int code = request.post().response().code();
        assertEquals(HTTP_OK, code);
        assertEquals("name=", body.get());
    }

    /**
     * Verify POST with query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "user");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify POST with query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithVaragsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "user").query("number", "100");
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify POST with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithEscapedMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "us er");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify POST with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithEscapedVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "us er").query("number", "100");
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify POST with numeric query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithNumericQueryParams() throws Exception {
        Map<Object, Object> inputParams = new HashMap<Object, Object>();
        inputParams.put(1, 2);
        inputParams.put(3, 4);
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("1", request.getParameter("1"));
                outputParams.put("3", request.getParameter("3"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("2", outputParams.get("1"));
        assertEquals("4", outputParams.get("3"));
    }

    /**
     * Verify GET with query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "user");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("GET", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "user").query("number", "100");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("GET", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithEscapedMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "us er");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("GET", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithEscapedVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "us er").query("number", "100");
        assertEquals(HTTP_OK, request.get().response().code());
        assertEquals("GET", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "user");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.delete().response().code());
        assertEquals("DELETE", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "user").query("number", "100");
        assertEquals(HTTP_OK, request.delete().response().code());
        assertEquals("DELETE", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithEscapedMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "us er");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query(inputParams);
        assertEquals(HTTP_OK, request.delete().response().code());
        assertEquals("DELETE", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithEscapedVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "us er").query("number", "100");
        assertEquals(HTTP_OK, request.delete().response().code());
        assertEquals("DELETE", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify POST with query parameters
     *
     * @throws Exception
     */
    @Test
    public void postWithVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        KieServerHttpRequest request = newRequest(url).query("name", "user").query("number", "100");
        assertEquals(HTTP_OK, request.post().response().code());
        assertEquals("POST", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Append with base URL with no path
     *
     * @throws Exception
     */
    @Test
    public void appendMappedQueryParamsWithNoPath() throws Exception {
        assertEquals("http://test.com/?a=b", appendQueryParameters("http://test.com", Collections.singletonMap("a", "b")));
    }

    /**
     * Append with base URL with no path
     *
     * @throws Exception
     */
    @Test
    public void appendVarargsQueryParmasWithNoPath() throws Exception {
        assertEquals("http://test.com/?a=b", appendQueryParameters("http://test.com", "a", "b"));
    }

    /**
     * Append with base URL with path
     *
     * @throws Exception
     */
    @Test
    public void appendMappedQueryParamsWithPath() throws Exception {
        assertEquals("http://test.com/segment1?a=b",
                appendQueryParameters("http://test.com/segment1", Collections.singletonMap("a", "b")));
        assertEquals("http://test.com/?a=b", appendQueryParameters("http://test.com/", Collections.singletonMap("a", "b")));
    }

    /**
     * Append with base URL with path
     *
     * @throws Exception
     */
    @Test
    public void appendVarargsQueryParamsWithPath() throws Exception {
        assertEquals("http://test.com/segment1?a=b", appendQueryParameters("http://test.com/segment1", "a", "b"));
        assertEquals("http://test.com/?a=b", appendQueryParameters("http://test.com/", "a", "b"));
    }

    /**
     * Append multiple params
     *
     * @throws Exception
     */
    @Test
    public void appendMultipleMappedQueryParams() throws Exception {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("a", "b");
        params.put("c", "d");
        assertEquals("http://test.com/1?a=b&c=d", appendQueryParameters("http://test.com/1", params));
    }

    /**
     * Append multiple params
     *
     * @throws Exception
     */
    @Test
    public void appendMultipleVarargsQueryParams() throws Exception {
        assertEquals("http://test.com/1?a=b&c=d", appendQueryParameters("http://test.com/1", "a", "b", "c", "d"));
    }

    /**
     * Append null params
     *
     * @throws Exception
     */
    @Test
    public void appendNullMappedQueryParams() throws Exception {
        assertEquals("http://test.com/1", appendQueryParameters("http://test.com/1", (Map<?, ?>) null));
    }

    /**
     * Append null params
     *
     * @throws Exception
     */
    @Test
    public void appendNullVaragsQueryParams() throws Exception {
        assertEquals("http://test.com/1", appendQueryParameters("http://test.com/1", (Object[]) null));
    }

    /**
     * Append empty params
     *
     * @throws Exception
     */
    @Test
    public void appendEmptyMappedQueryParams() throws Exception {
        assertEquals("http://test.com/1", appendQueryParameters("http://test.com/1", Collections.<String, String> emptyMap()));
    }

    /**
     * Append empty params
     *
     * @throws Exception
     */
    @Test
    public void appendEmptyVarargsQueryParams() throws Exception {
        assertEquals("http://test.com/1", appendQueryParameters("http://test.com/1", new Object[0]));
    }

    /**
     * Append params with null values
     *
     * @throws Exception
     */
    @Test
    public void appendWithNullMappedQueryParamValues() throws Exception {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("a", null);
        params.put("b", null);
        assertEquals("http://test.com/1?a=&b=", appendQueryParameters("http://test.com/1", params));
    }

    /**
     * Append params with null values
     *
     * @throws Exception
     */
    @Test
    public void appendWithNullVaragsQueryParamValues() throws Exception {
        assertEquals("http://test.com/1?a=&b=", appendQueryParameters("http://test.com/1", "a", null, "b", null));
    }

    /**
     * Try to append with wrong number of arguments
     */
    @Test(expected = IllegalArgumentException.class)
    public void appendOddNumberOfParams() {
        appendQueryParameters("http://test.com", "1");
    }

    /**
     * Append with base URL already containing a '?'
     */
    @Test
    public void appendMappedQueryParamsWithExistingQueryStart() {
        assertEquals("http://test.com/1?a=b", appendQueryParameters("http://test.com/1?", Collections.singletonMap("a", "b")));
    }

    /**
     * Append with base URL already containing a '?'
     */
    @Test
    public void appendVarargsQueryParamsWithExistingQueryStart() {
        assertEquals("http://test.com/1?a=b", appendQueryParameters("http://test.com/1?", "a", "b"));
    }

    /**
     * Append with base URL already containing a '?'
     */
    @Test
    public void appendMappedQueryParamsWithExistingParams() {
        assertEquals("http://test.com/1?a=b&c=d",
                appendQueryParameters("http://test.com/1?a=b", Collections.singletonMap("c", "d")));
        assertEquals("http://test.com/1?a=b&c=d",
                appendQueryParameters("http://test.com/1?a=b&", Collections.singletonMap("c", "d")));

    }

    /**
     * Append with base URL already containing a '?'
     */
    @Test
    public void appendWithVarargsQueryParamsWithExistingParams() {
        assertEquals("http://test.com/1?a=b&c=d", appendQueryParameters("http://test.com/1?a=b", "c", "d"));
        assertEquals("http://test.com/1?a=b&c=d", appendQueryParameters("http://test.com/1?a=b&", "c", "d"));
    }

    /**
     * Get a 400
     *
     * @throws Exception
     */
    @Test
    public void badRequestCode() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_BAD_REQUEST);
            }
        };
        KieServerHttpRequest request = newRequest(url);
        assertNotNull(request);
        assertEquals(HTTP_BAD_REQUEST, request.get().response().code());
    }

    /**
     * Verify data is sent when receiving response without first calling {@link KieServerHttpRequest#response().Code()}
     *
     * @throws Exception
     */
    @Test
    public void sendReceiveWithoutcode() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                try {
                    response.getWriter().write("world");
                } catch( IOException ignored ) {
                    // Ignored
                }
                response.setStatus(HTTP_OK);
            }
        };

        KieServerHttpRequest request = newRequest(new URL(url)).ignoreCloseExceptions(false);
        assertEquals("world", request.body("hello").post().response().body());
        assertEquals("hello", body.get());
    }

    /**
     * Verify data is send when receiving response headers without first calling {@link KieServerHttpRequest#response().code()}
     *
     * @throws Exception
     */
    @Test
    public void sendHeadersWithoutCode() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setHeader("h1", "v1");
                response.setHeader("h2", "v2");
                response.setStatus(HTTP_OK);
            }
        };

        KieServerHttpRequest request = newRequest(new URL(url)).ignoreCloseExceptions(false);
        Map<String, List<String>> headers = request.body("hello").post().response().headers();
        assertEquals("v1", headers.get("h1").get(0));
        assertEquals("v2", headers.get("h2").get(0));
        assertEquals("hello", body.get());
    }

    /**
     * Verify data is send when receiving response integer header without first
     * calling {@link KieServerHttpRequest#response().Code()}
     *
     * @throws Exception
     */
    @Test
    public void sendIntHeaderWithoutCode() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                body.set(new String(read()));
                response.setIntHeader("Width", 9876);
                response.setStatus(HTTP_OK);
            }
        };

        KieServerHttpRequest request = newRequest(new URL(url)).ignoreCloseExceptions(false);
        assertEquals(9876, request.body("hello").post().response().intHeader("Width"));
        assertEquals("hello", body.get());
    }

    /**
     * Verify reading response body for empty 200
     *
     * @throws Exception
     */
    @Test
    public void streamOfEmptyOkResponse() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(200);
            }
        };
        assertEquals("", newRequest(url).get().response().body());
    }

    /**
     * Verify reading response body for empty 400
     *
     * @throws Exception
     */
    @Test
    public void bodyOfEmptyErrorResponse() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_BAD_REQUEST);
            }
        };
        assertEquals("", newRequest(url).get().response().body());
    }

    /**
     * Verify reading response body for non-empty 400
     *
     * @throws Exception
     */
    @Test
    public void bodyOfNonEmptyErrorResponse() throws Exception {
        handler = new RequestHandler() {

            @Override
            public void handle( Request request, HttpServletResponse response ) {
                response.setStatus(HTTP_BAD_REQUEST);
                try {
                    response.getWriter().write("error");
                } catch( IOException ignored ) {
                    // Ignored
                }
            }
        };
        assertEquals("error", newRequest(url).get().response().body());
    }

}
