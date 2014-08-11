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
package org.kie.services.client.api.rest;

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
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
import java.io.Reader;
import java.io.Writer;
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

import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.JsonSerializationProvider;

/**
 * A fluid interface for making HTTP requests using an underlying {@link HttpURLConnection} (or sub-class).
 * <p>
 * Each instance supports making a single request and cannot be reused for further requests.
 * 
 * This code was originally copied from Kevin Sawicki's HttpRequest project (https://github.com/kevinsawicki/http-request).
 * </p>
 * However, it has been extensively modified and rewritten.
 */
public class KieRemoteHttpRequest {

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String ENCODING_GZIP = "gzip";
    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_SERVER = "Server";
    public static final String PARAM_CHARSET = "charset";
    private static final String[] EMPTY_STRINGS = new String[0];

    private static JsonSerializationProvider JSON_SERIALIZER = new JsonSerializationProvider();
    private static JaxbSerializationProvider XML_SERIALIZER = new JaxbSerializationProvider();

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
    protected static abstract class Operation<V> implements Callable<V> {

        /**
         * Run operation
         *
         * @return result
         * @throws KieRemoteHttpRequestException
         * @throws IOException
         */
        protected abstract V run() throws KieRemoteHttpRequestException, IOException;

        /**
         * Operation complete callback
         *
         * @throws IOException
         */
        protected abstract void done() throws IOException;

        public V call() throws KieRemoteHttpRequestException {
            boolean thrown = false;
            try {
                return run();
            } catch( KieRemoteHttpRequestException e ) {
                thrown = true;
                throw e;
            } catch( IOException ioe ) {
                thrown = true;
                throw new KieRemoteHttpRequestException("Unable to do " + this.getClass().getSimpleName(), ioe);
            } finally {
                try {
                    done();
                } catch( IOException ioe ) {
                    if( !thrown )
                        throw new KieRemoteHttpRequestException("Exception thrown when finishing "
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
    protected static abstract class CloseOperation<V> extends Operation<V> {

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
    protected static abstract class FlushOperation<V> extends Operation<V> {

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
     * or any UTF-8 characters that are non-ASCII. No encoding of URLs is done by default by the {@link KieRemoteHttpRequest}
     * constructors and so if URL encoding is needed this method should be called before calling the {@link KieRemoteHttpRequest}
     * constructor.
     *
     * @param url
     * @return encoded URL
     * @throws KieRemoteHttpRequestException
     */
    static String encode( final CharSequence url ) throws KieRemoteHttpRequestException {
        URL parsed;
        try {
            parsed = new URL(url.toString());
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to encode url '" + url.toString() + "'", ioe);
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
            KieRemoteHttpRequestException krhre = new KieRemoteHttpRequestException("Unable to parse parse URI", e);
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
    static String append( final CharSequence url, final Map<?, ?> params ) {
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
    static String append( final CharSequence url, final Object... params ) {
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

    public static KieRemoteHttpRequest getRequest( final String url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, GET);
    }

    public static KieRemoteHttpRequest getRequest( final URL url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, GET);
    }

    public static KieRemoteHttpRequest getRequest( final String baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return getRequest(encode ? encode(url) : url);
    }

    public static KieRemoteHttpRequest getRequest( final String baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return getRequest(encode ? encode(url) : url);
    }

    public static KieRemoteHttpRequest postRequest( final String url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, POST);
    }

    public static KieRemoteHttpRequest postRequest( final URL url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, POST);
    }

    public static KieRemoteHttpRequest postRequest( final String baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return postRequest(encode ? encode(url) : url);
    }

    public static KieRemoteHttpRequest postRequest( final String baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return postRequest(encode ? encode(url) : url);
    }

    public static KieRemoteHttpRequest deleteRequest( final String url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, DELETE);
    }

    public static KieRemoteHttpRequest deleteRequest( final URL url ) throws KieRemoteHttpRequestException {
        return new KieRemoteHttpRequest(url, DELETE);
    }

    public static KieRemoteHttpRequest deleteRequest( final CharSequence baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return deleteRequest(encode ? encode(url) : url);
    }

    public static KieRemoteHttpRequest deleteRequest( final CharSequence baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return deleteRequest(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest(final String urlString) throws KieRemoteHttpRequestException {
        try {
            this.baseUrl = new URL(urlString);
        } catch( MalformedURLException e ) {
            throw new KieRemoteHttpRequestException("Unable to create request with url '" + urlString + "'", e);
        }
    }

    // Constructors --------------------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest(URL baseRestUrl, String username, String password, int timeoutInSeconds) {
        this.baseUrl = baseRestUrl;
        this.user = username;
        this.password = password;
        this.timeout = timeout * 1000;
    }

    public KieRemoteHttpRequest(final URL url, final String method) throws KieRemoteHttpRequestException {
        this.baseUrl = url;
        this.requestMethod = method;
    }

    public KieRemoteHttpRequest(final String urlString, final String method) throws KieRemoteHttpRequestException {
        this(urlString);
        this.requestMethod = method;
    }

    // HTTP methods --------------------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest get( final CharSequence baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest get( final CharSequence baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest get( final String relativeUrl ) throws KieRemoteHttpRequestException {
        relativeRequest(relativeUrl, GET);
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest get() throws KieRemoteHttpRequestException {
        this.requestMethod = GET;
        openConnectionAndDoHttpMethod();
        return this;
    }
    
    public KieRemoteHttpRequest post( final CharSequence baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return post(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest post( final String relativeUrl, final boolean encode, final Object... params ) {
        String url = append(relativeUrl, params);
        return post(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest post( final String relativeUrl ) throws KieRemoteHttpRequestException {
        relativeRequest(relativeUrl, POST);
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest post() throws KieRemoteHttpRequestException {
        this.requestMethod = POST;
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest put( final String relativeUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(relativeUrl, params);
        return put(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest put( final CharSequence baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return put(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest put( final String relativeUrl ) throws KieRemoteHttpRequestException {
        relativeRequest(relativeUrl, PUT);
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest put() throws KieRemoteHttpRequestException {
        this.requestMethod = PUT;
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest delete( final CharSequence baseUrl, final Map<?, ?> params, final boolean encode ) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest delete( final CharSequence baseUrl, final boolean encode, final Object... params ) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    public KieRemoteHttpRequest delete( final String relativeUrl ) throws KieRemoteHttpRequestException {
        relativeRequest(relativeUrl, DELETE);
        openConnectionAndDoHttpMethod();
        return this;
    }

    public KieRemoteHttpRequest delete() throws KieRemoteHttpRequestException {
        this.requestMethod = DELETE;
        openConnectionAndDoHttpMethod();
        return this;
    }
    
    private void openConnectionAndDoHttpMethod() {
        if( user != null && password != null ) {
            basic(user, password);
        }
        setTimeout();
        code();
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
    protected KieRemoteHttpRequest copy( final InputStream input, final OutputStream output ) throws IOException {
        return new CloseOperation<KieRemoteHttpRequest>(input, ignoreCloseExceptions) {

            @Override
            public KieRemoteHttpRequest run() throws IOException {
                final byte[] buffer = new byte[bufferSize];
                int read;
                while( (read = input.read(buffer)) != -1 ) {
                    output.write(buffer, 0, read);
                }
                return KieRemoteHttpRequest.this;
            }
        }.call();
    }

    /**
     * Copy from reader to writer
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    protected KieRemoteHttpRequest copy( final Reader input, final Writer output ) throws IOException {
        return new CloseOperation<KieRemoteHttpRequest>(input, ignoreCloseExceptions) {

            @Override
            public KieRemoteHttpRequest run() throws IOException {
                final char[] buffer = new char[bufferSize];
                int read;
                while( (read = input.read(buffer)) != -1 ) {
                    output.write(buffer, 0, read);
                }
                return KieRemoteHttpRequest.this;
            }
        }.call();
    }

    private final URL baseUrl;
    private URL requestUrl;
    private String user;
    private String password;
    private Integer timeout;
    private static final int DEFAULT_TIMEOUT_SECS = 5;

    private HttpURLConnection connection = null;
    private RequestOutputStream output;

    private String requestMethod;

    private boolean form;
    private boolean ignoreCloseExceptions = true;

    private boolean uncompress = false;
    private int bufferSize = 8192;

    private String httpProxyHost;
    private int httpProxyPort;

    // Fluent setter's/ property getter's
    // -----------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest ignoreCloseExceptions( final boolean ignore ) {
        ignoreCloseExceptions = ignore;
        return this;
    }

    public boolean ignoreCloseExceptions() {
        return ignoreCloseExceptions;
    }

    public KieRemoteHttpRequest bufferSize( final int size ) {
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
    public KieRemoteHttpRequest setUncompress( final boolean uncompress ) {
        this.uncompress = uncompress;
        return this;
    }

    protected ByteArrayOutputStream byteStream() {
        final int size = contentLength();
        if( size > 0 )
            return new ByteArrayOutputStream(size);
        else
            return new ByteArrayOutputStream();
    }

    // Connection methods --------------------------------------------------------------------------------------------------------

    private HttpURLConnection createConnection() {
        if( requestUrl == null ) {
            requestUrl = baseUrl;
        }
        String urlString = requestUrl.toString();
        try {
            final HttpURLConnection connection;
            if( httpProxyHost != null ) {
                Proxy proxy = new Proxy(HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
                connection = CONNECTION_FACTORY.create(requestUrl, proxy);
            } else {
                connection = CONNECTION_FACTORY.create(requestUrl);
            }
            connection.setRequestMethod(requestMethod);
            return connection;
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to create (" + requestMethod + ") connection to '" + urlString + "'",
                    ioe);
        }
    }

    HttpURLConnection getConnection() {
        if( connection == null ) {
            connection = createConnection();
        }
        return connection;
    }

    // Fluent connection manipulation methods -------------------------------------------------------------------------------------

    public KieRemoteHttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    public KieRemoteHttpRequest chunk( final int size ) {
        getConnection().setChunkedStreamingMode(size);
        return this;
    }

    public KieRemoteHttpRequest readTimeout( final int timeout ) {
        getConnection().setReadTimeout(timeout);
        return this;
    }

    public KieRemoteHttpRequest connectTimeout( final int timeout ) {
        getConnection().setConnectTimeout(timeout);
        return this;
    }

    private void setTimeout() {
        if( timeout == null ) {
            timeout = DEFAULT_TIMEOUT_SECS * 1000;
        }
        HttpURLConnection connection = getConnection();
        connection.setReadTimeout(timeout);
        connection.setConnectTimeout(timeout);
    }

    // Connection related getter methods -----------------------------------------------------------------------------------------

    /**
     * Set value of {@link HttpURLConnection#setUseCaches(boolean)}
     *
     * @param useCaches
     * @return this request
     */
    public KieRemoteHttpRequest useCaches( final boolean useCaches ) {
        getConnection().setUseCaches(useCaches);
        return this;
    }

    /**
     * Set the 'If-Modified-Since' request header to the given value
     *
     * @param ifModifiedSince
     * @return this request
     */
    public KieRemoteHttpRequest ifModifiedSince( final long ifModifiedSince ) {
        getConnection().setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public KieRemoteHttpRequest contentLength( final int contentLength ) {
        getConnection().setFixedLengthStreamingMode(contentLength);
        return this;
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public KieRemoteHttpRequest contentLength( final String contentLength ) {
        return contentLength(Integer.parseInt(contentLength));
    }

    public URL getUrl() {
        return getConnection().getURL();
    }

    public String getMethod() {
        return getConnection().getRequestMethod();
    }

    // Request header related methods -------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest header( final String name, final Object value ) {
        getConnection().setRequestProperty(name, value != null ? value.toString() : null);
        return this;
    }

    public KieRemoteHttpRequest headers( final Map<String, String> headers ) {
        if( !headers.isEmpty() ) {
            for( Entry<String, String> header : headers.entrySet() ) {
                header(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Set the 'Accept-Encoding' header to given value
     *
     * @param acceptEncoding
     * @return this request
     */
    public KieRemoteHttpRequest acceptEncoding( final String acceptEncoding ) {
        return header(ACCEPT_ENCODING, acceptEncoding);
    }

    /**
     * Set the 'Accept-Encoding' header to 'gzip'
     *
     * @see #setUncompress(boolean)
     * @return this request
     */
    public KieRemoteHttpRequest acceptGzipEncoding() {
        return acceptEncoding(ENCODING_GZIP);
    }

    /**
     * Set the 'Accept-Charset' header to given value
     *
     * @param acceptCharset
     * @return this request
     */
    public KieRemoteHttpRequest acceptCharset( final String acceptCharset ) {
        return header(ACCEPT_CHARSET, acceptCharset);
    }

    /**
     * Set the 'Authorization' header to given value
     *
     * @param authorization
     * @return this request
     */
    public KieRemoteHttpRequest authorization( final String authorization ) {
        return header(AUTHORIZATION, authorization);
    }

    /**
     * Set the 'Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public KieRemoteHttpRequest basic( final String name, final String password ) {
        return authorization("Basic " + Base64Util.encode(name + ':' + password));
    }

    /**
     * Set the 'Content-Type' request header to the given value
     *
     * @param contentType
     * @return this request
     */
    public KieRemoteHttpRequest contentType( final String contentType ) {
        return contentType(contentType, null);
    }

    /**
     * Set the 'Content-Type' request header to the given value and charset
     *
     * @param contentType
     * @param charset
     * @return this request
     */
    public KieRemoteHttpRequest contentType( final String contentType, final String charset ) {
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
    public KieRemoteHttpRequest accept( final String accept ) {
        return header(ACCEPT, accept);
    }

    /**
     * Set the 'Accept' header to 'application/json'
     *
     * @return this request
     */
    public KieRemoteHttpRequest acceptJson() {
        return accept(APPLICATION_JSON);
    }

    // Request/Output management methods
    // --------------------------------------------------------------------------------------------------

    /**
     * Open output stream
     *
     * @return this request
     * @throws IOException
     */
    protected KieRemoteHttpRequest openOutput() throws IOException {
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
     * @throws KieRemoteHttpRequestException
     * @throws IOException
     */
    protected KieRemoteHttpRequest closeOutput() throws IOException {
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
     * an {@link KieRemoteHttpRequestException}
     *
     * @return this request
     * @throws KieRemoteHttpRequestException
     */
    protected KieRemoteHttpRequest closeOutputQuietly() throws KieRemoteHttpRequestException {
        try {
            return closeOutput();
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to close output from response", ioe);
        }
    }

    // Request/Input helper methods
    // -------------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest send( final byte[] input ) throws KieRemoteHttpRequestException {
        return send(new ByteArrayInputStream(input));
    }

    public KieRemoteHttpRequest send( final InputStream input ) throws KieRemoteHttpRequestException {
        try {
            openOutput();
            copy(input, output);
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to write stream to request body", ioe);
        }
        return this;
    }

    public KieRemoteHttpRequest send( final Reader input ) throws KieRemoteHttpRequestException {
        try {
            openOutput();
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to add reader content to request body", ioe);
        }
        final Writer writer = new OutputStreamWriter(output, output.encoder.charset());
        return new FlushOperation<KieRemoteHttpRequest>(writer) {

            @Override
            protected KieRemoteHttpRequest run() throws IOException {
                return copy(input, writer);
            }
        }.call();
    }

    public KieRemoteHttpRequest send( final CharSequence value ) throws KieRemoteHttpRequestException {
        try {
            openOutput();
            output.write(value.toString());
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to add char sequence to request body", ioe);
        }
        return this;
    }

    public OutputStreamWriter writer() throws KieRemoteHttpRequestException {
        try {
            openOutput();
            return new OutputStreamWriter(output, output.encoder.charset());
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to create writer to request output stream", ioe);
        }
    }

    // Form parameter methods -----------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest form( final Object name, final Object value, String charset ) throws KieRemoteHttpRequestException {
        final boolean first = !form;
        if( first ) {
            contentType(APPLICATION_FORM_URLENCODED, charset);
            form = true;
        }
        charset = getValidCharset(charset);
        try {
            openOutput();
            if( !first )
                output.write('&');
            output.write(URLEncoder.encode(name.toString(), charset));
            output.write('=');
            if( value != null )
                output.write(URLEncoder.encode(value.toString(), charset));
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Unable to add form parameter (" + name + "/" + value + ") to request body",
                    ioe);
        }
        return this;
    }

    public KieRemoteHttpRequest form( final Object name, final Object value ) throws KieRemoteHttpRequestException {
        return form(name, value, CHARSET_UTF8);
    }

    public KieRemoteHttpRequest form( final Map<?, ?> values, final String charset ) throws KieRemoteHttpRequestException {
        if( !values.isEmpty() ) {
            for( Entry<?, ?> entry : values.entrySet() ) {
                form(entry.getKey(), entry.getValue(), charset);
            }
        }
        return this;
    }

    public KieRemoteHttpRequest form( final Map<?, ?> values ) throws KieRemoteHttpRequestException {
        return form(values, CHARSET_UTF8);
    }

    // Response related methods --------------------------------------------------------------------------------------------------

    public int code() throws KieRemoteHttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Error occured when trying to retrieve response code", ioe);
        }
    }

    public String message() throws KieRemoteHttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Error occurred when trying to retrieve response message", ioe);
        }
    }

    // Response content related methods -------------------------------------------------------------------------------------------

    /**
     * Get response as {@link String} in given character set
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset is null
     *
     * @param charset
     * @return string
     * @throws KieRemoteHttpRequestException
     */
    public String body( final String charset ) throws KieRemoteHttpRequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
            return output.toString(getValidCharset(charset));
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Error occurred when retrieving response body", ioe);
        }
    }

    /**
     * Get response as {@link String} using character set returned from {@link #charset()}
     *
     * @return string
     * @throws KieRemoteHttpRequestException
     */
    public String body() throws KieRemoteHttpRequestException {
        return body(charset());
    }

    /**
     * Get response as byte array
     *
     * @return byte array
     * @throws KieRemoteHttpRequestException
     */
    public byte[] bytes() throws KieRemoteHttpRequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
        } catch( IOException ioe ) {
            throw new KieRemoteHttpRequestException("Error occurred when retrieving byte content of response", ioe);
        }
        return output.toByteArray();
    }

    /**
     * Get stream to response body
     *
     * @return stream
     * @throws KieRemoteHttpRequestException
     */
    public InputStream stream() throws KieRemoteHttpRequestException {
        InputStream stream;
        if( code() < HTTP_BAD_REQUEST ) {
            try {
                stream = getConnection().getInputStream();
            } catch( IOException ioe ) {
                throw new KieRemoteHttpRequestException("Unable to retrieve input stream of response", ioe);
            }
        } else {
            stream = getConnection().getErrorStream();
            if( stream == null )
                try {
                    stream = getConnection().getInputStream();
                } catch( IOException ioe ) {
                    if( contentLength() > 0 )
                        throw new KieRemoteHttpRequestException("Unable to retrieve input stream of response", ioe);
                    else
                        stream = new ByteArrayInputStream(new byte[0]);
                }
        }

        if( !uncompress || !ENCODING_GZIP.equals(contentEncoding()) ) {
            return stream;
        } else {
            try {
                return new GZIPInputStream(stream);
            } catch( IOException e ) {
                throw new KieRemoteHttpRequestException("Unable to decompress gzipped stream", e);
            }
        }
    }

    /**
     * Get response in a buffered stream
     *
     * @see #bufferSize(int)
     * @return stream
     * @throws KieRemoteHttpRequestException
     */
    public BufferedInputStream buffer() throws KieRemoteHttpRequestException {
        return new BufferedInputStream(stream(), bufferSize);
    }

    // Response header related methods -------------------------------------------------------------------------------------------

    public String responseHeader( final String name ) throws KieRemoteHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    public Map<String, List<String>> responseHeaders() throws KieRemoteHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFields();
    }

    public long dateResponseHeader( final String name, final long defaultValue ) throws KieRemoteHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldDate(name, defaultValue);
    }

    public int intResponseHeader( final String name ) throws KieRemoteHttpRequestException {
        return intResponseHeader(name, -1);
    }

    public int intResponseHeader( final String name, final int defaultValue ) throws KieRemoteHttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }

    /**
     * Get all values of the given header from the response
     *
     * @param name
     * @return non-null but possibly empty array of {@link String} header values
     */
    public String[] responseHeaders( final String name ) {
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
    public String responseHeaderParameter( final String headerName, final String paramName ) {
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
    public Map<String, String> responseHeaderParameters( final String headerName ) {
        return getHeaderParams(responseHeader(headerName));
    }

    /**
     * Get parameter values from header value
     *
     * @param header
     * @return parameter value or null if none
     */
    protected Map<String, String> getHeaderParams( final String header ) {
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
    protected String getHeaderParam( final String value, final String paramName ) {
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

    /**
     * Get the 'Content-Encoding' header from the response
     *
     * @return this request
     */
    public String contentEncoding() {
        return responseHeader(CONTENT_ENCODING);
    }

    /**
     * Get the 'Content-Type' header from the response
     *
     * @return response header value
     */
    public String contentType() {
        return responseHeader(CONTENT_TYPE);
    }

    /**
     * Get the 'Content-Length' header from the response
     *
     * @return response header value
     */
    public int contentLength() {
        return intResponseHeader(CONTENT_LENGTH);
    }

    /**
     * Get 'charset' parameter from 'Content-Type' response header
     *
     * @return charset or null if none
     */
    public String charset() {
        return responseHeaderParameter(CONTENT_TYPE, PARAM_CHARSET);
    }

    // SSL / Certification methods -----------------------------------------------------------------------------------------------

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;

    private static SSLSocketFactory getTrustedFactory() throws KieRemoteHttpRequestException {
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
                throw new KieRemoteHttpRequestException("Security exception configuring SSL context", e);
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
     * @throws KieRemoteHttpRequestException
     */
    public KieRemoteHttpRequest trustAllCerts() throws KieRemoteHttpRequestException {
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
    public KieRemoteHttpRequest trustAllHosts() {
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
    public KieRemoteHttpRequest useProxy( final String proxyHost, final int proxyPort ) {
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
    public KieRemoteHttpRequest proxyAuthorization( final String proxyAuthorization ) {
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
    public KieRemoteHttpRequest proxyBasic( final String name, final String password ) {
        return proxyAuthorization("Basic " + Base64Util.encode(name + ':' + password));
    }

    // OTHER / CLEAN UP -----------------------------------------------------------------------------------------------------------

    public KieRemoteHttpRequest relativeRequest( String relativeUrlString, String httpMethod ) {
        relativeRequest(relativeUrlString);
        this.requestMethod = httpMethod;
        return this;
    }

    public KieRemoteHttpRequest relativeRequest( String relativeUrlString ) {
        String baseUrlString = baseUrl.toExternalForm();
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

    private void setRequestUrl( String urlString ) {
        try {
            this.requestUrl = new URL(urlString);
        } catch( MalformedURLException e ) {
            throw new KieRemoteHttpRequestException("Unable to create request with url'" + urlString + "'", e);
        }
    }

    public URI getUri() {
        try {

            return this.requestUrl.toURI();
        } catch( URISyntaxException urise ) {
            throw new KieRemoteHttpRequestException("Invalid request URL", urise);
        }
    }

    public <T> T getEntity( Class<T> entityClass ) {
        String output = body();
        String acceptHeader = responseHeader(CONTENT_TYPE);
        if( acceptHeader.equals(MediaType.APPLICATION_JSON) ) {
            return JSON_SERIALIZER.deserialize(output, entityClass);
        } else if( acceptHeader.equals(MediaType.APPLICATION_JSON) ) {
            return (T) XML_SERIALIZER.deserialize(output);
        } else {
            throw new KieRemoteHttpRequestException("Unknown " + ACCEPT + " header in response: " + acceptHeader);
        }
    }

    @Override
    public String toString() {
        return getMethod() + ' ' + getUrl();
    }

}
