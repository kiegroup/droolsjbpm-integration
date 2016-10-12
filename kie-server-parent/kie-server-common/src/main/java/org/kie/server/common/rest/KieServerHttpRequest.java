/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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
import static java.net.Proxy.Type.HTTP;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

/**
 * This class is only meant to be used internally by the kie-server code! For interacting with the
 * REST API, please use a proper REST framework such as RestEasy or Apache CXF.
 * </p>
 * <b>Using this class to interact with the REST API <i>will not be supported</i> and any issues or problems
 * that arise from such use will be dismissed with a referral to this exact text!</b>
 * </p>
 * <rr>
 * A fluid interface for making HTTP requests using an underlying {@link HttpURLConnection} (or sub-class).
 * <p>
 * Each instance supports making a single request and cannot be reused for further requests.
 *
 * This code was originally copied from Kevin Sawicki's
 * <a href="https://github.com/kevinsawicki/http-request">HttpRequest project</a> * project.
 * </p>
 * However, it has been extensively modified and rewritten to fit the use case in this code.
 */
public class KieServerHttpRequest {

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_SERVER = "Server";
    public static final String PARAM_CHARSET = "charset";
    private static final String[] EMPTY_STRINGS = new String[0];

    // Request information

    private static final int DEFAULT_TIMEOUT_SECS = 5;

    private RequestInfo requestInfo;

    private int bufferSize = 8192;
    private boolean ignoreCloseExceptions = true;
    boolean uncompress = false;

    private HttpURLConnection connection = null;
    private RequestOutputStream output;

    boolean followRedirects = false;
    String httpProxyHost;
    int httpProxyPort;


    private KieServerHttpResponse response = null;

    private static class RequestInfo {
        URL baseUrl;
        URL requestUrl;

        String user;
        String password;

        Integer timeoutInMilliSecs = DEFAULT_TIMEOUT_SECS * 1000;

        String requestMethod;

        Map<String, List<String>> headers;
        Map<String, List<String>> queryParameters;
        Map<String, List<String>> formParameters;
        boolean form = false;
        String charset;

        StringBuilder body;
        MediaType bodyContentType;

        public URL getRequestUrl() {
            if( requestUrl == null ) {
                requestUrl = baseUrl;
            }
            return requestUrl;
        }

        public void setRequestUrl( String urlString ) {
            requestUrl = convertStringToUrl(urlString);
        }

        public List<String> getHeader( String name ) {
            if( headers == null ) {
                headers = new LinkedHashMap<String, List<String>>();
            }
            if( headers.get(name) == null ) {
                return Collections.EMPTY_LIST;
            } else {
                return headers.get(name);
            }
        }

        public void setHeader( String name, Object value ) {
            if( this.headers == null ) {
                this.headers = new LinkedHashMap<String, List<String>>();
            }
            if( this.headers.get(name) == null ) {
                this.headers.put(name, new ArrayList<String>());
            }
            this.headers.get(name).add(value == null ? null : value.toString());
        }

        public void setQueryParameter( String name, Object value ) {
            if( this.queryParameters == null ) {
                this.queryParameters = new LinkedHashMap<String, List<String>>();
            }
            if( this.queryParameters.get(name) == null ) {
                this.queryParameters.put(name, new ArrayList<String>());
            }
            this.queryParameters.get(name).add(value == null ? null : value.toString());
        }

        public void setFormParameter( String name, Object value ) {
            if( this.formParameters == null ) {
                this.formParameters = new LinkedHashMap<String, List<String>>();
            }
            if( this.formParameters.get(name) == null ) {
                this.formParameters.put(name, new ArrayList<String>());
            }
            this.formParameters.get(name).add(value == null ? null : value.toString());
        }

        public void addToBody( CharSequence addToBody ) {
            if( this.body == null ) {
                this.body = new StringBuilder();
            }
            this.body.append(addToBody);
        }

        @Override
        public RequestInfo clone() {
            RequestInfo clone = new RequestInfo();
            clone.baseUrl = baseUrl;
            clone.body = body;
            clone.bodyContentType = bodyContentType;
            clone.charset = charset;
            clone.form = form;
            clone.formParameters = formParameters;
            clone.headers = headers;
            clone.password = password;
            clone.queryParameters = queryParameters;
            clone.requestMethod = requestMethod;
            clone.requestUrl = requestUrl;
            clone.timeoutInMilliSecs = timeoutInMilliSecs;
            clone.user = user;
            return clone;
        }
    }

    private RequestInfo getRequestInfo() {
        if( requestInfo == null ) {
            requestInfo = new RequestInfo();
        }
        return requestInfo;
    }

    /**
     * Creates {@link HttpURLConnection HTTP connections} for {@link URL urls}.
     */
    public interface ConnectionFactory {
        /**
         * Open an {@link HttpURLConnection} for the specified {@link URL}.
         *
         * @throws IOException
         */
        HttpURLConnection create( URL url ) throws IOException;

        /**
         * Open an {@link HttpURLConnection} for the specified {@link URL} and {@link Proxy}.
         *
         * @throws IOException
         */
        HttpURLConnection create( URL url, Proxy proxy ) throws IOException;

        /**
         * A {@link ConnectionFactory} which uses the built-in {@link URL#openConnection()}
         */
        ConnectionFactory DEFAULT = new ConnectionFactory() {
            public HttpURLConnection create( URL url ) throws IOException {
                return (HttpURLConnection) url.openConnection();
            }

            public HttpURLConnection create( URL url, Proxy proxy ) throws IOException {
                return (HttpURLConnection) url.openConnection(proxy);
            }
        };
    }

    private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;

    /**
     * Operation that handles executing a callback once complete and handling
     * nested exceptions
     *
     * @param <V>
     */
    private abstract static class Operation<V> implements Callable<V> {

        /**
         * Run operation
         *
         * @return result
         * @throws KieServerHttpRequestException
         * @throws IOException
         */
        protected abstract V run() throws KieServerHttpRequestException, IOException;

        /**
         * Operation complete callback
         *
         * @throws IOException
         */
        protected abstract void done() throws IOException;

        public V call() throws KieServerHttpRequestException {
            boolean thrown = false;
            try {
                return run();
            } catch( KieServerHttpRequestException e ) {
                thrown = true;
                throw e;
            } catch( IOException ioe ) {
                thrown = true;
                throw new KieServerHttpRequestException("Unable to do " + this.getClass().getSimpleName(), ioe);
            } finally {
                try {
                    done();
                } catch( IOException ioe ) {
                    if( !thrown )
                        throw new KieServerHttpRequestException("Exception thrown when finishing "
                                + this.getClass().getSimpleName(), ioe);
                }
            }
        }
    }

    /**
     * Class that ensures a {@link Closeable} gets closed with proper exception
     * handling.
     *
     * @param <V>
     */
    private abstract static class CloseOperation<V> extends Operation<V> {

        private final Closeable closeable;

        private final boolean ignoreCloseExceptions;

        /**
         * Create closer for operation
         *
         * @param closeable
         * @param ignoreCloseExceptions
         */
        protected CloseOperation(final Closeable closeable, final boolean ignoreCloseExceptions) {
            this.closeable = closeable;
            this.ignoreCloseExceptions = ignoreCloseExceptions;
        }

        @Override
        protected void done() throws IOException {
            if( closeable instanceof Flushable )
                ((Flushable) closeable).flush();
            if( ignoreCloseExceptions )
                try {
                    closeable.close();
                } catch( IOException ioe ) {
                    // Ignored
                }
            else
                closeable.close();
        }
    }

    /**
     * Class that and ensures a {@link Flushable} gets flushed with proper
     * exception handling.
     *
     * @param <V>
     */
    private abstract static class FlushOperation<V> extends Operation<V> {

        private final Flushable flushable;

        /**
         * Create flush operation
         *
         * @param flushable
         */
        protected FlushOperation(final Flushable flushable) {
            this.flushable = flushable;
        }

        @Override
        protected void done() throws IOException {
            flushable.flush();
        }
    }

    /**
     * Request output stream
     */
    public static class RequestOutputStream extends BufferedOutputStream {

        private final CharsetEncoder encoder;

        /**
         * Create request output stream
         *
         * @param stream
         * @param charset
         * @param bufferSize
         */
        public RequestOutputStream(final OutputStream stream, final String charset, final int bufferSize) {
            super(stream, bufferSize);

            encoder = Charset.forName(getValidCharset(charset)).newEncoder();
        }

        /**
         * Write string to stream
         *
         * @param value
         * @return this stream
         * @throws IOException
         */
        public RequestOutputStream write( final String value ) throws IOException {
            final ByteBuffer bytes = encoder.encode(CharBuffer.wrap(value));

            super.write(bytes.array(), 0, bytes.limit());

            return this;
        }
    }

    private static String getValidCharset( final String charset ) {
        if( charset != null && charset.length() > 0 )
            return charset;
        else
            return CHARSET_UTF8;
    }

    private static StringBuilder addPathSeparator( final String baseUrl, final StringBuilder result ) {
        // Add trailing slash if the base URL doesn't have any path segments.
        //
        // The following test is checking for the last slash not being part of
        // the protocol to host separator: '://'.
        if( baseUrl.indexOf(':') + 2 == baseUrl.lastIndexOf('/') )
            result.append('/');
        return result;
    }

    private static StringBuilder addParamPrefix( final String baseUrl, final StringBuilder result ) {
        // Add '?' if missing and add '&' if params already exist in base url
        final int queryStart = baseUrl.indexOf('?');
        final int lastChar = result.length() - 1;
        if( queryStart == -1 )
            result.append('?');
        else if( queryStart < lastChar && baseUrl.charAt(lastChar) != '&' )
            result.append('&');
        return result;
    }

    /**
     * Encode the given URL as an ASCII {@link String}
     * <p>
     * This method ensures the path and query segments of the URL are properly encoded such as ' ' characters being encoded to '%20'
     * or any UTF-8 characters that are non-ASCII. No encoding of URLs is done by default by the {@link KieServerHttpRequest}
     * constructors and so if URL encoding is needed this method should be called before calling the {@link KieServerHttpRequest}
     * constructor.
     *
     * @param url
     * @return encoded URL
     * @throws KieServerHttpRequestException
     */
    static String encodeUrlToUTF8( final CharSequence url ) throws KieServerHttpRequestException {
        URL parsed;
        try {
            parsed = new URL(url.toString());
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Unable to encode url '" + url.toString() + "'", ioe);
        }

        String host = parsed.getHost();
        int port = parsed.getPort();
        if( port != -1 )
            host = host + ':' + Integer.toString(port);

        try {
            String encoded = new URI(parsed.getProtocol(), host, parsed.getPath(), parsed.getQuery(), null).toASCIIString();
            int paramsStart = encoded.indexOf('?');
            if( paramsStart > 0 && paramsStart + 1 < encoded.length() )
                encoded = encoded.substring(0, paramsStart + 1) + encoded.substring(paramsStart + 1).replace("+", "%2B");
            return encoded;
        } catch( URISyntaxException e ) {
            KieServerHttpRequestException krhre = new KieServerHttpRequestException("Unable to parse parse URI", e);
            throw krhre;
        }
    }

    /**
     * Append given map as query parameters to the base URL
     * <p>
     * Each map entry's key will be a parameter name and the value's {@link Object#toString()} will be the parameter value.
     *
     * @param url
     * @param params
     * @return URL with appended query params
     */
    static String appendQueryParameters( final CharSequence url, final Map<?, ?> params ) {
        final String baseUrl = url.toString();
        if( params == null || params.isEmpty() )
            return baseUrl;

        final StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        Entry<?, ?> entry;
        Object value;
        Iterator<?> iterator = params.entrySet().iterator();
        entry = (Entry<?, ?>) iterator.next();
        result.append(entry.getKey().toString());
        result.append('=');
        value = entry.getValue();
        if( value != null )
            result.append(value);

        while( iterator.hasNext() ) {
            result.append('&');
            entry = (Entry<?, ?>) iterator.next();
            result.append(entry.getKey().toString());
            result.append('=');
            value = entry.getValue();
            if( value != null )
                result.append(value);
        }

        return result.toString();
    }

    /**
     * Append given name/value pairs as query parameters to the base URL
     * <p>
     * The params argument is interpreted as a sequence of name/value pairs so the given number of params must be divisible by 2.
     *
     * @param url
     * @param params
     * name/value pairs
     * @return URL with appended query params
     */
    static String appendQueryParameters( final CharSequence url, final Object... params ) {
        final String baseUrl = url.toString();
        if( params == null || params.length == 0 ) {
            return baseUrl;
        }

        if( params.length % 2 != 0 ) {
            throw new IllegalArgumentException("Must specify an even number of parameter names/values");
        }

        final StringBuilder result = new StringBuilder(baseUrl);

        addPathSeparator(baseUrl, result);
        addParamPrefix(baseUrl, result);

        Object value;
        result.append(params[0]);
        result.append('=');
        value = params[1];
        if( value != null )
            result.append(value);

        for( int i = 2; i < params.length; i += 2 ) {
            result.append('&');
            result.append(params[i]);
            result.append('=');
            value = params[i + 1];
            if( value != null ) {
                result.append(value);
            }
        }

        return result.toString();
    }

    public static void setKeepAlive( final boolean keepAlive ) {
        setProperty("http.keepAlive", Boolean.toString(keepAlive));
    }

    public static void setMaxConnections( final int maxConnections ) {
        setProperty("http.maxConnections", Integer.toString(maxConnections));
    }

    private static String setProperty( final String name, final String value ) {
        final PrivilegedAction<String> action;
        if( value != null )
            action = new PrivilegedAction<String>() {

                public String run() {
                    return System.setProperty(name, value);
                }
            };
        else
            action = new PrivilegedAction<String>() {

                public String run() {
                    return System.clearProperty(name);
                }
            };
        return AccessController.doPrivileged(action);
    }

    // Factory methods ------------------------------------------------------------------------------------------------------------

    public static KieServerHttpRequest deleteRequest(final URL url ) throws KieServerHttpRequestException {
        KieServerHttpRequest request = new KieServerHttpRequest(url);
        request.getRequestInfo().requestMethod = DELETE;
        return request;
    }

    public static KieServerHttpRequest putRequest(final URL url ) throws KieServerHttpRequestException {
        KieServerHttpRequest request = new KieServerHttpRequest(url);
        request.getRequestInfo().requestMethod = PUT;
        return request;
    }

    public static KieServerHttpRequest getRequest(final String urlString ) throws KieServerHttpRequestException {
        KieServerHttpRequest request = new KieServerHttpRequest(urlString);
        request.getRequestInfo().requestMethod = GET;
        return request;
    }

    public static KieServerHttpRequest getRequest(final URL url ) throws KieServerHttpRequestException {
        KieServerHttpRequest request = new KieServerHttpRequest(url);
        request.getRequestInfo().requestMethod = GET;
        return request;
    }

    public static KieServerHttpRequest postRequest(final URL url ) throws KieServerHttpRequestException {
        KieServerHttpRequest request = new KieServerHttpRequest(url);
        request.getRequestInfo().requestMethod = POST;
        return request;
    }

    public static KieServerHttpRequest newRequest(final String url ) throws KieServerHttpRequestException {
        return new KieServerHttpRequest(url);
    }

    public static KieServerHttpRequest newRequest(final URL url ) throws KieServerHttpRequestException {
        return new KieServerHttpRequest(url);
    }

    public static KieServerHttpRequest newRequest(final String url, String username, String password )
            throws KieServerHttpRequestException {
        return new KieServerHttpRequest(url, username, password);
    }

    public static KieServerHttpRequest newRequest(final URL url, String username, String password )
            throws KieServerHttpRequestException {
        return new KieServerHttpRequest(url, username, password);
    }

    private KieServerHttpRequest(final URL url) throws KieServerHttpRequestException {
        getRequestInfo().baseUrl = url;
    }

    private KieServerHttpRequest(final String urlString) throws KieServerHttpRequestException {
        getRequestInfo().baseUrl = convertStringToUrl(urlString);
    }

    private static URL convertStringToUrl( final String urlString ) throws KieServerHttpRequestException {
        try {
            return new URL(urlString);
        } catch( MalformedURLException e ) {
            throw new KieServerHttpRequestException("Unable to create request with url '" + urlString + "'", e);
        }

    }

    // Constructors --------------------------------------------------------------------------------------------------------------

    private KieServerHttpRequest(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    private KieServerHttpRequest(URL stringUrl, String username, String password) {
        RequestInfo requestInfo = getRequestInfo();
        requestInfo.baseUrl = stringUrl;
        requestInfo.user = username;
        requestInfo.password = password;
    }

    private KieServerHttpRequest(String stringUrl, String username, String password) {
        this(stringUrl);
        RequestInfo requestInfo = getRequestInfo();
        requestInfo.user = username;
        requestInfo.password = password;
    }

    // HTTP methods --------------------------------------------------------------------------------------------------------------

    public KieServerHttpRequest get(final String relativeUrl ) throws KieServerHttpRequestException {
        relativeRequest(relativeUrl, GET);
        responseCode();
        return this;
    }

    public KieServerHttpRequest get() throws KieServerHttpRequestException {
        getRequestInfo().requestMethod = GET;
        responseCode();
        return this;
    }

    public KieServerHttpRequest post(final String relativeUrl ) throws KieServerHttpRequestException {
        relativeRequest(relativeUrl, POST);
        responseCode();
        return this;
    }

    public KieServerHttpRequest post() throws KieServerHttpRequestException {
        getRequestInfo().requestMethod = POST;
        responseCode();
        return this;
    }

    public KieServerHttpRequest put(final String relativeUrl ) throws KieServerHttpRequestException {
        relativeRequest(relativeUrl, PUT);
        responseCode();
        return this;
    }

    public KieServerHttpRequest put() throws KieServerHttpRequestException {
        getRequestInfo().requestMethod = PUT;
        responseCode();
        return this;
    }

    public KieServerHttpRequest delete(final String relativeUrl ) throws KieServerHttpRequestException {
        relativeRequest(relativeUrl, DELETE);
        responseCode();
        return this;
    }

    public KieServerHttpRequest delete() throws KieServerHttpRequestException {
        getRequestInfo().requestMethod = DELETE;
        responseCode();
        return this;
    }

    // General Input/Output helper methods ----------------------------------------------------------------------------------------

    /**
     * Copy from input stream to output stream
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    private KieServerHttpRequest copy(final InputStream input, final OutputStream output ) throws IOException {
        return new CloseOperation<KieServerHttpRequest>(input, ignoreCloseExceptions) {

            @Override
            public KieServerHttpRequest run() throws IOException {
                final byte[] buffer = new byte[bufferSize];
                int read;
                while( (read = input.read(buffer)) != -1 ) {
                    output.write(buffer, 0, read);
                }
                return KieServerHttpRequest.this;
            }
        }.call();
    }

    // Fluent setter's/ property getter's -----------------------------------------------------------------------------------------

    public KieServerHttpRequest ignoreCloseExceptions(final boolean ignore ) {
        ignoreCloseExceptions = ignore;
        return this;
    }

    public boolean ignoreCloseExceptions() {
        return ignoreCloseExceptions;
    }

    public KieServerHttpRequest bufferSize(final int size ) {
        if( size < 1 )
            throw new IllegalArgumentException("Size must be greater than zero");
        bufferSize = size;
        return this;
    }

    public int bufferSize() {
        return bufferSize;
    }

    /**
     * Set whether or not the response body should be automatically uncompressed when read from.
     * <p>
     * This will only affect requests that have the 'Content-Encoding' response header set to 'gzip'.
     * <p>
     * This causes all receive methods to use a {@link GZIPInputStream} when applicable so that higher level streams and readers can
     * read the data uncompressed.
     * <p>
     * Setting this option does not cause any request headers to be set automatically so {@link #acceptGzipEncoding()} should be
     * used in conjunction with this setting to tell the server to gzip the response.
     *
     * @param uncompress
     * @return this request
     */
    public KieServerHttpRequest setUncompress(final boolean uncompress ) {
        this.uncompress = uncompress;
        return this;
    }

    public KieServerHttpRequest followRedirects(final boolean followRedirects ) {
        this.followRedirects = followRedirects;
        return this;
    }

    public URI getUri() {
        try {
            return getRequestInfo().getRequestUrl().toURI();
        } catch( URISyntaxException urise ) {
            throw new KieServerHttpRequestException("Invalid request URL", urise);
        }
    }

    public KieServerHttpRequest timeout(final long timeoutInMilliseconds ) {
        if( connection != null ) {
            connection.setReadTimeout((int) timeoutInMilliseconds);
        } else {
            getRequestInfo().timeoutInMilliSecs = (int) timeoutInMilliseconds;
        }
        return this;
    }

    private void setRequestUrl( String urlString ) {
        getRequestInfo().setRequestUrl(urlString);
    }

    // Connection methods --------------------------------------------------------------------------------------------------------

    private HttpURLConnection createConnection() {
        String urlString = getRequestInfo().getRequestUrl().toString();
        if( getRequestInfo().requestMethod == null ) {
            throw new KieServerHttpRequestException("Please specify (and execute?) a HTTP method first.");
        }
        try {
            final HttpURLConnection connection;
            if( this.httpProxyHost != null ) {
                Proxy proxy = new Proxy(HTTP, new InetSocketAddress(this.httpProxyHost, this.httpProxyPort));
                connection = CONNECTION_FACTORY.create(getRequestInfo().getRequestUrl(), proxy);
            } else {
                connection = CONNECTION_FACTORY.create(getRequestInfo().getRequestUrl());
            }
            // support for localhost and https
            if (getRequestInfo().getRequestUrl().getProtocol().equalsIgnoreCase("https") && getRequestInfo().getRequestUrl().getHost().equalsIgnoreCase("localhost")) {
                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier(){

                    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equalsIgnoreCase("localhost")) {
                            return true;
                        }
                        return false;
                    }
                });
            }

            connection.setRequestMethod(getRequestInfo().requestMethod);
            return connection;
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Unable to create (" + getRequestInfo().requestMethod + ") connection to '"
                    + urlString + "'", ioe);
        }
    }

    HttpURLConnection getConnection() {
        if( getRequestInfo().requestMethod == null ) {
            throw new KieServerHttpRequestException("Please set HTTP request method before opening a connection.");
        }
        initializeConnection();
        return connection;
    }

    private void initializeConnection() {
        if( connection == null ) {
            addQueryParametersToUrl();
            connection = createConnection();
            // timeout
            connection.setReadTimeout(getRequestInfo().timeoutInMilliSecs);
            connection.setConnectTimeout(getRequestInfo().timeoutInMilliSecs);

            // various
            RequestInfo requestInfo = getRequestInfo();
            int contentLength = 0;
            if( requestInfo.body != null ) {
                contentLength = requestInfo.body.toString().getBytes(Charset.forName("UTF-8")).length;
                connection.setFixedLengthStreamingMode(contentLength);
                List<String> contentTypeList = requestInfo.getHeader(ACCEPT);
                if( contentTypeList != null && ! contentTypeList.isEmpty() ) {
                   requestInfo.setHeader(CONTENT_TYPE, contentTypeList.get(0));
                }
            }
            requestInfo.setHeader(CONTENT_LENGTH, contentLength);
            connection.setInstanceFollowRedirects(followRedirects);

            // auth
            if( requestInfo.user != null && requestInfo.password != null ) {
                basicAuthorization(requestInfo.user, requestInfo.password);
            }

            // headers
            if( requestInfo.headers != null ) {
                for( Entry<String, List<String>> entry : requestInfo.headers.entrySet() ) {
                    List<String> headerVals = entry.getValue();
                    for( String val : headerVals ) {
                        connection.setRequestProperty(entry.getKey(), val);
                    }
                }
            }

            // output: form parameters, body
            addFormParametersToConnection();
            if( requestInfo.body != null ) {
                try {
                    openOutput();
                    output.write(requestInfo.body.toString());
                } catch( IOException ioe ) {
                    throw new KieServerHttpRequestException("Unable to add char sequence to request body", ioe);
                }
            }
        }
    }

    // relative request methods ---------------------------------------------------------------------------------------------------

    public KieServerHttpRequest relativeRequest(String relativeUrlString, String httpMethod ) {
        relativeRequest(relativeUrlString);
        getRequestInfo().requestMethod = httpMethod;
        return this;
    }

    public KieServerHttpRequest relativeRequest(String relativeUrlString ) {
        String baseUrlString = getRequestInfo().baseUrl.toExternalForm();
        boolean urlSlash = baseUrlString.endsWith("/");
        boolean postfixSlash = relativeUrlString.startsWith("/");
        String separator = "";
        if( !urlSlash && !postfixSlash ) {
            separator = "/";
        } else if( urlSlash && postfixSlash ) {
            relativeUrlString = relativeUrlString.substring(1);
        }

        setRequestUrl(baseUrlString + separator + relativeUrlString);
        return this;
    }

    // Fluent connection manipulation methods -------------------------------------------------------------------------------------

    public KieServerHttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    public KieServerHttpRequest resetStream() throws IOException {
        getConnection().getInputStream().reset();
        return this;
    }

    // Connection related getter methods -----------------------------------------------------------------------------------------

    public KieServerHttpRequest followRedirets(final boolean followRedirects ) {
        this.followRedirects = followRedirects;
        return this;
    }

    public URL getUrl() {
        return getRequestInfo().getRequestUrl();
    }

    public String getMethod() {
        return getRequestInfo().requestMethod;
    }

    // Request header related methods -------------------------------------------------------------------------------------------

    public String getHeader( final String name ) {
        List<String> headerVals = getRequestInfo().getHeader(name);
        if( headerVals != null && !headerVals.isEmpty() ) {
            return headerVals.get(0);
        }
        return null;
    }

    public KieServerHttpRequest header(final String name, final Object value ) {
        getRequestInfo().setHeader(name, value);
        return this;
    }

    public KieServerHttpRequest headers(final Map<String, String> headers ) {
        if( !headers.isEmpty() ) {
            for( Entry<String, String> header : headers.entrySet() ) {
                header(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    public List<String> getRequestHeader( String headerName ) {
        return getRequestInfo().getHeader(headerName);
        // return getConnection().getRequestProperties().get(headerName);
    }

    /**
     * Set the 'Accept-Encoding' header to given value
     *
     * @param acceptEncoding
     * @return this request
     */
    public KieServerHttpRequest acceptEncoding(final String acceptEncoding ) {
        return header(ACCEPT_ENCODING, acceptEncoding);
    }

    /**
     * Set the 'Accept-Charset' header to given value
     *
     * @param acceptCharset
     * @return this request
     */
    public KieServerHttpRequest acceptCharset(final String acceptCharset ) {
        return header(ACCEPT_CHARSET, acceptCharset);
    }

    /**
     * Set the 'Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public KieServerHttpRequest basicAuthorization(final String name, final String password ) {
        return header(AUTHORIZATION, "Basic " + org.kie.server.common.rest.Base64Util.encode(name + ':' + password));
    }

    /**
     * Set the 'Authorization' header to given values in Bearer/Token authentication
     * format
     *
     * @param token
     * @return this request
     */
    public KieServerHttpRequest tokenAuthorization(final String token ) {
        return header(AUTHORIZATION, "Bearer " + token);
    }

    /**
     * Set the 'Content-Type' request header to the given value
     *
     * @param contentType
     * @return this request
     */
    public KieServerHttpRequest contentType(final String contentType ) {
        return contentType(contentType, null);
    }

    /**
     * Set the 'Content-Type' request header to the given value and charset
     *
     * @param contentType
     * @param charset
     * @return this request
     */
    public KieServerHttpRequest contentType(final String contentType, final String charset ) {
        if( charset != null && charset.length() > 0 ) {
            final String separator = "; " + PARAM_CHARSET + '=';
            return header(CONTENT_TYPE, contentType + separator + charset);
        } else
            return header(CONTENT_TYPE, contentType);
    }

    /**
     * Set the 'Accept' header to given value
     *
     * @param accept
     * @return this request
     */
    public KieServerHttpRequest accept(final String accept ) {
        RequestInfo requestInfo = getRequestInfo();
        if( requestInfo.getHeader(ACCEPT).isEmpty() ) {
           requestInfo.setHeader(ACCEPT, new ArrayList<String>());
        }
        requestInfo.headers.get(ACCEPT).set(0, accept);
        return this;
    }

    // Request/Output management methods
    // --------------------------------------------------------------------------------------------------

    /**
     * Open output stream
     *
     * @return this request
     * @throws IOException
     */
    private KieServerHttpRequest openOutput() throws IOException {
        if( output != null ) {
            return this;
        }
        getConnection().setDoOutput(true);
        final String charset = getHeaderParam(getConnection().getRequestProperty(CONTENT_TYPE), PARAM_CHARSET);
        output = new RequestOutputStream(getConnection().getOutputStream(), charset, bufferSize);
        return this;
    }

    /**
     * Close output stream
     *
     * @return this request
     * @throws KieServerHttpRequestException
     * @throws IOException
     */
    private KieServerHttpRequest closeOutput() throws IOException {
        if( connection == null ) {
            throw new KieServerHttpRequestException("Please execute a HTTP method first on the request.");
        }
        if( output == null ) {
            return this;
        }
        if( ignoreCloseExceptions ) {
            try {
                output.close();
            } catch( IOException ignored ) {
                // Ignored
            }
        } else {
            output.close();
        }
        output = null;
        return this;
    }

    /**
     * Call {@link #closeOutput()} and re-throw a caught {@link IOException}s as
     * an {@link KieServerHttpRequestException}
     *
     * @return this request
     * @throws KieServerHttpRequestException
     */
    private KieServerHttpRequest closeOutputQuietly() throws KieServerHttpRequestException {
        try {
            return closeOutput();
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Unable to close output from response", ioe);
        }
    }

    // Request/Input helper methods -----------------------------------------------------------------------------------------------

    public KieServerHttpRequest body(final CharSequence value ) throws KieServerHttpRequestException {
        getRequestInfo().addToBody(value);
        return this;
    }

    public OutputStreamWriter writer() throws KieServerHttpRequestException {
        try {
            openOutput();
            return new OutputStreamWriter(output, output.encoder.charset());
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Unable to create writer to request output stream", ioe);
        }
    }

    // query parameter methods ----------------------------------------------------------------------------------------------------

    public KieServerHttpRequest query(final Object name, final Object value ) throws KieServerHttpRequestException {
        getRequestInfo().setQueryParameter(name.toString(), value != null ? value.toString() : null);
        return this;
    }

    public KieServerHttpRequest query(final Map<?, ?> values ) throws KieServerHttpRequestException {
        if( !values.isEmpty() ) {
            for( Entry<?, ?> entry : values.entrySet() ) {
                query(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    private void addQueryParametersToUrl() {
        RequestInfo requestInfo = getRequestInfo();
        Object[] paramList = null;
        if( requestInfo.queryParameters != null ) {
            List<String> queryParamList = new ArrayList<String>();
            for( Entry<String, List<String>> paramListEntry : requestInfo.queryParameters.entrySet() ) {
                String name = paramListEntry.getKey();
                for( String val : paramListEntry.getValue() ) {
                    queryParamList.add(name);
                    queryParamList.add(val);
                }
            }
            paramList = queryParamList.toArray();
        }
        String unencodedUrlString = appendQueryParameters(requestInfo.getRequestUrl().toString(), paramList);
        String urlString = encodeUrlToUTF8(unencodedUrlString);
        requestInfo.setRequestUrl(urlString);
    }

    // Form parameter methods -----------------------------------------------------------------------------------------------------

    public KieServerHttpRequest form(final Object name, final Object value, String charset ) throws KieServerHttpRequestException {
        if( !getRequestInfo().form ) {
            contentType(APPLICATION_FORM_URLENCODED, charset);
            getRequestInfo().form = true;
        }
        charset = getValidCharset(charset);

        RequestInfo requestInfo = getRequestInfo();
        requestInfo.form = true;
        requestInfo.charset = charset;
        requestInfo.setFormParameter(name.toString(), value);

        return this;
    }

    public KieServerHttpRequest form(final Object name, final Object value ) throws KieServerHttpRequestException {
        return form(name, value, CHARSET_UTF8);
    }

    public KieServerHttpRequest form(final Map<?, ?> values, final String charset ) throws KieServerHttpRequestException {
        if( !values.isEmpty() ) {
            for( Entry<?, ?> entry : values.entrySet() ) {
                form(entry.getKey(), entry.getValue(), charset);
            }
        }
        return this;
    }

    public KieServerHttpRequest form(final Map<?, ?> values ) throws KieServerHttpRequestException {
        return form(values, CHARSET_UTF8);
    }

    private void addFormParametersToConnection() {
        RequestInfo requestInfo = getRequestInfo();
        if( requestInfo.form && requestInfo.formParameters != null ) {

            String name = null;
            String value = null;
            try {
                openOutput();
                boolean first = true;

                for( Entry<String, List<String>> entry : requestInfo.formParameters.entrySet() ) {
                    name = entry.getKey();
                    for( String formValue : entry.getValue() ) {
                        value = formValue;
                        if( !first ) {
                            output.write('&');
                        }
                        first = false;
                        output.write(URLEncoder.encode(name.toString(), requestInfo.charset));
                        output.write('=');
                        if( value != null ) {
                            output.write(URLEncoder.encode(value.toString(), requestInfo.charset));
                        }
                    }
                }
            } catch( IOException ioe ) {
                throw new KieServerHttpRequestException(
                        "Unable to add form parameter (" + name + "/" + value + ") to request body", ioe);
            }
        }
    }

    // Response related methods --------------------------------------------------------------------------------------------------

    public KieServerHttpResponse response() {
        if( this.response == null ) {
            this.response = new KieServerHttpResponse() {

                private String body = null;

                // @formatter:off
                @Override
                public InputStream stream() throws KieServerHttpRequestException { return responseStream(); }
                @Override
                public String message() throws KieServerHttpRequestException { return responseMessage(); }
                @Override
                public int intHeader( String name ) throws KieServerHttpRequestException { return intResponseHeader(name); }
                @Override
                public String[] headers( String name ) { return responseHeaders(name); }
                @Override
                public Map<String, List<String>> headers() throws KieServerHttpRequestException { return responseHeaders(); }
                @Override
                public Map<String, String> headerParameters( String headerName ) { return responseHeaderParameters(headerName); }
                @Override
                public String headerParameter( String headerName, String paramName ) { return responseHeaderParameter(headerName, paramName); }
                @Override
                public String header( String name ) throws KieServerHttpRequestException { return responseHeader(name); }
                @Override
                public String contentType() { return responseContentType(); }
                @Override
                public int contentLength() { return responseContentLength(); }
                @Override
                public String contentEncoding() { return responseContentEncoding(); }
                @Override
                public int code() throws KieServerHttpRequestException { return responseCode(); }
                @Override
                public String charset() { return responseCharset(); }
                @Override
                public byte[] bytes() throws KieServerHttpRequestException { return responseBytes(); }
                @Override
                public BufferedInputStream buffer() throws KieServerHttpRequestException { return responseBuffer(); }
                @Override
                public String body() throws KieServerHttpRequestException {
                    if( body == null ) {
                        body = responseBody();
                    }
                    return body;
                }
                // @formatter:on
            };
        }
        return this.response;
    }

    private int responseCode() throws KieServerHttpRequestException {
        initializeConnection();
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Error occurred when trying to retrieve response code", ioe);
        }
    }

    private String responseMessage() throws KieServerHttpRequestException {
        initializeConnection();
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Error occurred when trying to retrieve response message", ioe);
        }
    }

    private String responseBody() throws KieServerHttpRequestException {
        String charset = responseCharset();
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(responseBuffer(), output);
            return output.toString(getValidCharset(charset));
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Error occurred when retrieving response body", ioe);
        }
    }

    private byte[] responseBytes() throws KieServerHttpRequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(responseBuffer(), output);
        } catch( IOException ioe ) {
            throw new KieServerHttpRequestException("Error occurred when retrieving byte content of response", ioe);
        }
        return output.toByteArray();
    }

    private ByteArrayOutputStream byteStream() {
        final int size = responseContentLength();
        if( size > 0 )
            return new ByteArrayOutputStream(size);
        else
            return new ByteArrayOutputStream();
    }

    private InputStream responseStream() throws KieServerHttpRequestException {
        InputStream stream;
        if( responseCode() < HTTP_BAD_REQUEST ) {
            try {
                stream = getConnection().getInputStream();
            } catch( IOException ioe ) {
                throw new KieServerHttpRequestException("Unable to retrieve input stream of response", ioe);
            }
        } else {
            stream = getConnection().getErrorStream();
            if( stream == null )
                try {
                    stream = getConnection().getInputStream();
                } catch( IOException ioe ) {
                    if( responseContentLength() > 0 )
                        throw new KieServerHttpRequestException("Unable to retrieve input stream of response", ioe);
                    else
                        stream = new ByteArrayInputStream(new byte[0]);
                }
        }

        if( !uncompress || !"gzip".equals(responseContentEncoding()) ) {
            return stream;
        } else {
            try {
                return new GZIPInputStream(stream);
            } catch( IOException e ) {
                throw new KieServerHttpRequestException("Unable to decompress gzipped stream", e);
            }
        }
    }

    private BufferedInputStream responseBuffer() throws KieServerHttpRequestException {
        return new BufferedInputStream(responseStream(), bufferSize);
    }

    // Response header related methods -------------------------------------------------------------------------------------------

    private String responseHeader( final String name ) throws KieServerHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    private int intResponseHeader( final String name ) throws KieServerHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, -1);
    }

    private Map<String, List<String>> responseHeaders() throws KieServerHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFields();
    }

    /**
     * Get all values of the given header from the response
     *
     * @param name
     * @return non-null but possibly empty array of {@link String} header values
     */
    private String[] responseHeaders( final String name ) {
        final Map<String, List<String>> headers = responseHeaders();
        if( headers == null || headers.isEmpty() )
            return EMPTY_STRINGS;

        final List<String> values = headers.get(name);
        if( values != null && !values.isEmpty() )
            return values.toArray(new String[values.size()]);
        else
            return EMPTY_STRINGS;
    }

    /**
     * Get parameter with given name from header value in response
     *
     * @param headerName
     * @param paramName
     * @return parameter value or null if missing
     */
    private String responseHeaderParameter( final String headerName, final String paramName ) {
        return getHeaderParam(responseHeader(headerName), paramName);
    }

    /**
     * Get all parameters from header value in response
     * <p>
     * This will be all key=value pairs after the first ';' that are separated by a ';'
     *
     * @param headerName
     * @return non-null but possibly empty map of parameter headers
     */
    private Map<String, String> responseHeaderParameters( final String headerName ) {
        return getHeaderParams(responseHeader(headerName));
    }

    /**
     * Get parameter values from header value
     *
     * @param header
     * @return parameter value or null if none
     */
    private static Map<String, String> getHeaderParams( final String header ) {
        if( header == null || header.length() == 0 )
            return Collections.emptyMap();

        final int headerLength = header.length();
        int start = header.indexOf(';') + 1;
        if( start == 0 || start == headerLength )
            return Collections.emptyMap();

        int end = header.indexOf(';', start);
        if( end == -1 )
            end = headerLength;

        Map<String, String> params = new LinkedHashMap<String, String>();
        while( start < end ) {
            int nameEnd = header.indexOf('=', start);
            if( nameEnd != -1 && nameEnd < end ) {
                String name = header.substring(start, nameEnd).trim();
                if( name.length() > 0 ) {
                    String value = header.substring(nameEnd + 1, end).trim();
                    int length = value.length();
                    if( length != 0 )
                        if( length > 2 && '"' == value.charAt(0) && '"' == value.charAt(length - 1) )
                            params.put(name, value.substring(1, length - 1));
                        else
                            params.put(name, value);
                }
            }

            start = end + 1;
            end = header.indexOf(';', start);
            if( end == -1 )
                end = headerLength;
        }

        return params;
    }

    /**
     * Get parameter value from header value
     *
     * @param value
     * @param paramName
     * @return parameter value or null if none
     */
    private static String getHeaderParam( final String value, final String paramName ) {
        if( value == null || value.length() == 0 )
            return null;

        final int length = value.length();
        int start = value.indexOf(';') + 1;
        if( start == 0 || start == length )
            return null;

        int end = value.indexOf(';', start);
        if( end == -1 )
            end = length;

        while( start < end ) {
            int nameEnd = value.indexOf('=', start);
            if( nameEnd != -1 && nameEnd < end && paramName.equals(value.substring(start, nameEnd).trim()) ) {
                String paramValue = value.substring(nameEnd + 1, end).trim();
                int valueLength = paramValue.length();
                if( valueLength != 0 )
                    if( valueLength > 2 && '"' == paramValue.charAt(0) && '"' == paramValue.charAt(valueLength - 1) )
                        return paramValue.substring(1, valueLength - 1);
                    else
                        return paramValue;
            }

            start = end + 1;
            end = value.indexOf(';', start);
            if( end == -1 )
                end = length;
        }

        return null;
    }

    private String responseContentEncoding() {
        return responseHeader(CONTENT_ENCODING);
    }

    private String responseContentType() {
        return responseHeader(CONTENT_TYPE);
    }

    private int responseContentLength() {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(CONTENT_LENGTH, -1);
    }

    private String responseCharset() {
        return responseHeaderParameter(CONTENT_TYPE, PARAM_CHARSET);
    }

    // SSL / Certification methods -----------------------------------------------------------------------------------------------

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;

    private static SSLSocketFactory getTrustedFactory() throws KieServerHttpRequestException {
        if( TRUSTED_FACTORY == null ) {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted( X509Certificate[] chain, String authType ) {
                    // Intentionally left blank
                }

                public void checkServerTrusted( X509Certificate[] chain, String authType ) {
                    // Intentionally left blank
                }
            } };
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch( GeneralSecurityException e ) {
                throw new KieServerHttpRequestException("Security exception configuring SSL context", e);
            }
        }

        return TRUSTED_FACTORY;
    }

    private static HostnameVerifier getTrustedVerifier() {
        if( TRUSTED_VERIFIER == null )
            TRUSTED_VERIFIER = new HostnameVerifier() {

                public boolean verify( String hostname, SSLSession session ) {
                    return true;
                }
            };

        return TRUSTED_VERIFIER;
    }

    /**
     * Configure HTTPS connection to trust all certificates
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     * @throws KieServerHttpRequestException
     */
    public KieServerHttpRequest trustAllCerts() throws KieServerHttpRequestException {
        final HttpURLConnection connection = getConnection();
        if( connection instanceof HttpsURLConnection )
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustedFactory());
        return this;
    }

    /**
     * Configure HTTPS connection to trust all hosts using a custom {@link HostnameVerifier} that always returns <code>true</code>
     * for each
     * host verified
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     */
    public KieServerHttpRequest trustAllHosts() {
        final HttpURLConnection connection = getConnection();
        if( connection instanceof HttpsURLConnection )
            ((HttpsURLConnection) connection).setHostnameVerifier(getTrustedVerifier());
        return this;
    }

    // Proxy methods --------------------------------------------------------------------------------------------------------------

    public static void setProxyHost( final String host ) {
        setProperty("http.proxyHost", host);
        setProperty("https.proxyHost", host);
    }

    public static void setProxyPort( final int port ) {
        final String portValue = Integer.toString(port);
        setProperty("http.proxyPort", portValue);
        setProperty("https.proxyPort", portValue);
    }

    public static void setNonProxyHosts( final String... hosts ) {
        if( hosts != null && hosts.length > 0 ) {
            StringBuilder separated = new StringBuilder();
            int last = hosts.length - 1;
            for( int i = 0; i < last; i++ )
                separated.append(hosts[i]).append('|');
            separated.append(hosts[last]);
            setProperty("http.nonProxyHosts", separated.toString());
        } else
            setProperty("http.nonProxyHosts", null);
    }

    /**
     * Configure an HTTP proxy on this connection. Use {{@link #proxyBasic(String, String)} if
     * this proxy requires basic authentication.
     *
     * @param proxyHost
     * @param proxyPort
     * @return this request
     */
    public KieServerHttpRequest useProxy(final String proxyHost, final int proxyPort ) {
        if( connection != null ) {
            throw new IllegalStateException(
                    "The connection has already been created. This method must be called before reading or writing to the request.");
        }

        this.httpProxyHost = proxyHost;
        this.httpProxyPort = proxyPort;
        return this;
    }

    /**
     * Set the 'Proxy-Authorization' header to given value
     *
     * @param proxyAuthorization
     * @return this request
     */
    public KieServerHttpRequest proxyAuthorization(final String proxyAuthorization ) {
        return header(HEADER_PROXY_AUTHORIZATION, proxyAuthorization);
    }

    /**
     * Set the 'Proxy-Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public KieServerHttpRequest proxyBasic(final String name, final String password ) {
        return proxyAuthorization("Basic " + org.kie.server.common.rest.Base64Util.encode(name + ':' + password));
    }

    // OTHER ----------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return getMethod() + ' ' + getUrl();
    }

    @Override
    public KieServerHttpRequest clone() {
        if( connection != null ) {
            throw new KieServerHttpRequestException("Unable to clone request with open or completed connection.");
        }
        return new KieServerHttpRequest(getRequestInfo().clone());
    }

}
