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

package org.kie.server.common.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Variant;

import org.kie.server.common.rest.variant.ServerDrivenNegotiation;

/**
 * This utility compensates for RESTEASY-960: 
 * https://issues.jboss.org/browse/RESTEASY-960
 */
public class RestEasy960Util {

    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_CHARSET = "Accept-Charset";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ACCEPT_LANGUAGE = "Accept-Language";
   
    public static List<Variant> variants 
        = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE, MediaType.APPLICATION_JSON_TYPE).add().build();
    public static Variant defaultVariant 
        = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE).add().build().get(0);
    public static final Variant jsonVariant 
        = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    
    public static Variant getVariant(HttpHeaders headers) { 
        // copied (except for the acceptHeaders fix) from RestEasy's RequestImpl class
        ServerDrivenNegotiation negotiation = new ServerDrivenNegotiation();
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        List<String> acceptHeaders = requestHeaders.get(ACCEPT);
        // Fix
        if( acceptHeaders != null && ! acceptHeaders.isEmpty() ) { 
            List<String> fixedAcceptHeaders = new ArrayList<String>();
            for(String header : acceptHeaders ) { 
                fixedAcceptHeaders.add(header.replaceAll("q=\\.", "q=0.")); 
            }
            acceptHeaders = fixedAcceptHeaders;
            negotiation.setAcceptHeaders(acceptHeaders);
            negotiation.setAcceptCharsetHeaders(requestHeaders.get(ACCEPT_CHARSET));
            negotiation.setAcceptEncodingHeaders(requestHeaders.get(ACCEPT_ENCODING));
            negotiation.setAcceptLanguageHeaders(requestHeaders.get(ACCEPT_LANGUAGE));

            return negotiation.getBestMatch(variants);
            // ** use below instead of above when RESTEASY-960 is fixed **
            // return restRequest.selectVariant(variants);
        }
        return null;
    }
}
