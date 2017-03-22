/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ExchangeHelper;

import static org.kie.camel.KieCamelConstants.KIE_HEADERS_PREFIX;

public class KieCamelUtils {

    public static Message getResultMessage( Exchange exchange ) {
        return ExchangeHelper.isOutCapable( exchange ) ? exchange.getOut() : exchange.getIn();
    }

    public static String asCamelKieName( String name ) {
        return KIE_HEADERS_PREFIX + ucFirst( name );
    }

    public static String ucFirst( String name ) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
