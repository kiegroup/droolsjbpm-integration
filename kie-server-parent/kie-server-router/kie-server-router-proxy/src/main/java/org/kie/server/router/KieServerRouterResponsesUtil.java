/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.router;

public final class KieServerRouterResponsesUtil {

    private KieServerRouterResponsesUtil() {
        // do nothing
    }

    public static String buildServerInfo (KieServerRouterEnvironment environment) {
        return "{\n" +
                "      \"version\" : \"LATEST\",\n" +
                "      \"name\" : \"" + environment.getRouterName() + "\",\n" +
                "      \"location\" : \"" + environment.getRouterExternalUrl() + "\",\n" +
                "      \"capabilities\" : [ \"KieServer\", \"BRM\", \"BPM\", \"CaseMgmt\", \"BPM-UI\" ],\n" +
                "      \"id\" : \"" + environment.getRouterId() + "\"\n" +
                "}";
    }
    
    public static String buildJAXBServerInfoReponse(KieServerRouterEnvironment environment) {
        return "<response type=\"SUCCESS\" msg=\"Kie Server info\">\n"+
               "<kie-server-info>\n"+
               "    <capabilities>KieServer</capabilities>\n"+
               "    <capabilities>BRM</capabilities>\n"+
               "    <capabilities>BPM</capabilities>\n"+
               "    <capabilities>CaseMgmt</capabilities>\n"+
               "    <capabilities>BPM-UI</capabilities>\n"+
               "    <location>" + environment.getRouterExternalUrl() + "</location>\n"+
               "    <messages/>\n"+            
               "    <name>" + environment.getRouterName() + "</name>\n"+
               "    <id>" + environment.getRouterId() + "</id>\n"+
               "    <version>LATEST</version>\n"+
               "</kie-server-info>\n"+
            "</response>";
    }
    
    public static String buildJSONServerInfoReponse(KieServerRouterEnvironment environment) {
        return  "{\n"+
                "  \"type\" : \"SUCCESS\",\n"+
                "  \"msg\" : \"Kie Server info\",\n"+
                "  \"result\" : {\n"+
                "    \"kie-server-info\" : {\n"+
                "      \"version\" : \"LATEST\",\n"+
                "      \"name\" : \"" + environment.getRouterName() + "\",\n"+
                "      \"location\" : \"" + environment.getRouterExternalUrl() + "\",\n"+
                "      \"capabilities\" : [ \"KieServer\", \"BRM\", \"BPM\", \"CaseMgmt\", \"BPM-UI\" ],\n"+
                "      \"id\" : \"" + environment.getRouterId() + "\"\n"+
                "    }\n"+
                "  }\n"+
                "}";
    }

    public static String buildXSTREAMServerInfoReponse(KieServerRouterEnvironment environment) {
        return  "<org.kie.server.api.model.ServiceResponse>"+
                "    <type>SUCCESS</type>\n"+
                "    <msg>Kie Server info</msg>\n"+
                "    <result class=\"kie-server-info\">\n"+
                "    <serverId>" + environment.getRouterId() + "</serverId>\n"+
                "    <version>LATEST</version>\n"+
                "    <name>" + environment.getRouterName() + "</name>\n"+
                "    <location>" + environment.getRouterExternalUrl() + "</location>\n"+
                "    <capabilities>\n"+
                "        <string>KieServer</string>\n"+
                "        <string>BRM</string>\n"+
                "        <string>BPM</string>\n"+
                "        <string>CaseMgmt</string>\n"+
                "        <string>BPM-UI</string>\n"+
                "    </capabilities>\n" +
                "    </result>\n"+
                "</org.kie.server.api.model.ServiceResponse>"; 
    }

}
