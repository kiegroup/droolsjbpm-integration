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
package org.kie.remote.client.rest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface KieRemoteHttpResponse {

    public int code() throws KieRemoteHttpRequestException;
    public String message() throws KieRemoteHttpRequestException;
    
    /**
     * Get response as {@link String} using character set returned from {@link #charset()}
     *
     * @return The content of the response
     * @throws KieRemoteHttpRequestException
     */
    public String body() throws KieRemoteHttpRequestException;
    
    /**
     * Get response as byte array
     *
     * @return the content of the response in a byte array.
     * @throws KieRemoteHttpRequestException
     */
    public byte[] bytes() throws KieRemoteHttpRequestException;
    
    /**
     * Get stream to response body
     *
     * @return The {@link InputStream} containing the response body content
     * @throws KieRemoteHttpRequestException
     */
    public InputStream stream() throws KieRemoteHttpRequestException;
    public BufferedInputStream buffer() throws KieRemoteHttpRequestException;
    /**
     * Get the response header
     * @param name The name of the response header
     * @return The value of the requested header
     * @throws KieRemoteHttpRequestException
     */
    public String header( final String name ) throws KieRemoteHttpRequestException;
    int intHeader( final String name ) throws KieRemoteHttpRequestException;
    public Map<String, List<String>> headers() throws KieRemoteHttpRequestException;
    public String[] headers( final String name );
    public String headerParameter( final String headerName, final String paramName );
    public Map<String, String> headerParameters( final String headerName );
    
    /**
     * Get the 'Content-Encoding' header from the response
     *
     * @return this request
     */
    public String contentEncoding();
    
    /**
     * Get the 'Content-Type' header from the response
     *
     * @return response header value
     */
    public String contentType();
    
    /**
     * Get the 'Content-Length' header from the response
     *    
     * @return response header value
     */
    public int contentLength();
    
    /**
     * Get 'charset' parameter from 'Content-Type' response header
     *
     * @return charset or null if none
     */
    public String charset();
    
}
