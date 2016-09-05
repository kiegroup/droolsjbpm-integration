/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.objects;

public class AnotherMessage {

    private String another_name;

    public AnotherMessage(String name) {
        this.another_name = name;
    }

    public String getAnother_name() {
        return another_name;
    }

    public void setAnother_name(String another_name) {
        this.another_name = another_name;
    }
}
