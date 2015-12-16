/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates
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
package org.kie.remote.common.rest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface that exposes response related functionality of a {@link KieRemoteHttpRequest} instance.
 */
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
