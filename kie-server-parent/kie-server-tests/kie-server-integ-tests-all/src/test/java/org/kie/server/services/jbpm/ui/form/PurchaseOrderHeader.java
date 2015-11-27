/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.services.jbpm.ui.form;

@org.kie.api.definition.type.Label(value = "Purchase Order Header")
public class PurchaseOrderHeader  implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    @org.kie.api.definition.type.Label(value = "Project")
    private java.lang.String project;

    @org.kie.api.definition.type.Label(value = "Creation Date")
    private java.util.Date creationDate;

    @org.kie.api.definition.type.Label(value = "Customer Name")
    private java.lang.String customer;

    public PurchaseOrderHeader() {

    }

    public java.lang.String getProject() {
        return this.project;
    }

    public void setProject(  java.lang.String project ) {
        this.project = project;
    }

    public java.util.Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(  java.util.Date creationDate ) {
        this.creationDate = creationDate;
    }

    public java.lang.String getCustomer() {
        return this.customer;
    }

    public void setCustomer(  java.lang.String customer ) {
        this.customer = customer;
    }

}
