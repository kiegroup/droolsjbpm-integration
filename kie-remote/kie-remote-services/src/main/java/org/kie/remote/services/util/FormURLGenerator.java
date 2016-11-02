/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.util;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.util.StringUtils;

public class FormURLGenerator {

    public static final String KIE_WB_JSP = "kie-wb.jsp";
    public static final String FORM_PERSPECTIVE = "FormDisplayPerspective";

    public String generateFormProcessURL( String baseURL, String processId, String deploymentId, String opener ) {
        if ( StringUtils.isEmpty( baseURL ) || StringUtils.isEmpty( processId ) || StringUtils.isEmpty( deploymentId ) || StringUtils.isEmpty(
                opener ) ) return "";

        Map urlParams = new HashMap();

        urlParams.put( "processId", processId );
        urlParams.put( "domainId", deploymentId );
        urlParams.put( "opener", opener );

        return generateURL( baseURL, urlParams );
    }


    public String generateFormTaskURL( String baseURL, long taskId, String opener ) {
        if ( StringUtils.isEmpty( baseURL ) || taskId < 0 || StringUtils.isEmpty( opener ) ) return "";

        Map<String, String> urlParams = new HashMap<String, String>();

        urlParams.put( "taskId", String.valueOf( taskId ) );
        urlParams.put( "opener", opener );

        return generateURL( baseURL, urlParams );
    }

    protected String generateURL( String baseURL, Map<String, String> params ) {
        StringBuilder resultURL = new StringBuilder();
        if ( baseURL.contains( "/rest/" ) ) {
            baseURL = baseURL.substring( 0, baseURL.indexOf( "/rest/" ) );
        }

        resultURL.append( baseURL );

        if ( !baseURL.endsWith( "/" ) ) {
            resultURL.append( "/" );
        }

        resultURL.append( KIE_WB_JSP ).append( "?perspective=" ).append( FORM_PERSPECTIVE ).append( "&standalone=true" );

        if ( params != null && !params.isEmpty() ) {
            for ( Map.Entry<String, String> entry : params.entrySet() ) {
                resultURL.append( "&" );
                resultURL.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
            }
        }
        return resultURL.toString();
    }
}
