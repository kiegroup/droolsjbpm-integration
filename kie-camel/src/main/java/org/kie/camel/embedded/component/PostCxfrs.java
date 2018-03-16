/*
 * Copyright 2010 JBoss Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.embedded.component;

import java.io.InputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.drools.core.util.StringUtils;

public class PostCxfrs implements Processor {
    public void process(Exchange exchange) throws Exception {
        Object object = exchange.getIn().getBody();
        if (object instanceof Response) {
            Response res = (Response)object;
            if (res.getStatus() == Status.OK.getStatusCode()) {
                exchange.getIn().setBody(StringUtils.toString((InputStream)((Response)object).getEntity()));
            }
        }
    }
}
