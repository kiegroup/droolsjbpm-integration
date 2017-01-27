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

package org.kie.server.services.jbpm.ui.form;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.form.provider.AbstractFormProvider;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.kie.server.services.jbpm.ui.FormServiceBase;
import org.kie.server.services.jbpm.ui.api.UIFormProvider;

public class RemoteKieFormsProvider extends AbstractFormProvider implements UIFormProvider {

    public static final String SUBFORM = "SubForm";

    public static final String SUBFORM_FORM_PROPERTY = "nestedForm";

    public static final String MULTIPLE_SUBFORM = "MultipleSubForm";

    public static final String MULTIPLE_SUBFORM_CREATION_FORM_PROPERTY = "creationForm";
    public static final String MULTIPLE_SUBFORM_EDITION_FORM_PROPERTY = "editionForm";

    protected JsonParser parser = new JsonParser();

    protected Gson gson = new Gson();

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getType() {
        return FormServiceBase.FormType.FORM_TYPE.getName();
    }

    @Override
    public void configure( FormManagerService formManagerService ) {
        this.formManagerService = formManagerService;
    }

    @Override
    public String render( String name, ProcessDefinition process, Map<String, Object> renderContext ) {
        if (!(process instanceof ProcessAssetDesc )) {
            return null;
        }

        return render( process.getId() + getFormSuffix(), process.getDeploymentId() );
    }

    @Override
    public String render( String name, Task task, ProcessDefinition process, Map<String, Object> renderContext ) {
        String lookupName = getTaskFormName( task );

        if ( lookupName == null || lookupName.isEmpty()) return null;

        return render( lookupName, task.getTaskData().getDeploymentId() );
    }

    protected String render( String formName, String deploymentId ) {
        String formContent = formManagerService.getFormByKey( deploymentId, formName);

        if ( !StringUtils.isEmpty( formContent ) ) {
            return generateRenderingContextString( formContent, formManagerService.getAllFormsByDeployment( deploymentId ) );
        }
        return null;
    }

    protected String generateRenderingContextString( String formContent, Map<String, String> availableForms ) {
        Map<String, Object> contextForms = new HashMap<>();

        Collection<String> result = availableForms.entrySet().stream().filter( entry -> entry.getKey().endsWith( getFormExtension() ) ).collect( Collectors.toMap( p -> p.getKey(), p -> p.getValue()) ).values();

        parseFormContent( parser.parse( formContent ).getAsJsonObject(), contextForms, result );

        return gson.toJson( contextForms.values() );
    }


    protected void parseFormContent( JsonObject jsonForm, Map<String, Object> contextForms, Collection<String> availableForms ) {
        String id = jsonForm.get( "id" ).getAsString();

        if ( !contextForms.containsKey( id ) ) {
            contextForms.put( id,  jsonForm );
        }

        JsonArray fields = jsonForm.get( "fields" ).getAsJsonArray();

        fields.forEach( jsonElement -> {
            JsonObject object = jsonElement.getAsJsonObject();
            if ( object != null ) {
                String code = object.get( "code" ).getAsString();
                if( SUBFORM.equals( code ) ) {
                    String id1 = object.get( SUBFORM_FORM_PROPERTY ).getAsString();
                    parseFormContent( id1, contextForms, availableForms );
                } else if ( MULTIPLE_SUBFORM.equals( code ) ) {
                    String id1 = object.get( MULTIPLE_SUBFORM_CREATION_FORM_PROPERTY ).getAsString();
                    parseFormContent( id1, contextForms, availableForms );
                    id1 = object.get( MULTIPLE_SUBFORM_EDITION_FORM_PROPERTY ).getAsString();
                    parseFormContent( id1, contextForms, availableForms );
                }
            }
        } );
    }

    protected void parseFormContent( String formId, Map<String, Object> contextForms, Collection<String> availableForms ) {
        if ( !StringUtils.isEmpty( formId ) && !contextForms.containsKey( formId )) {
            JsonObject jsonForm = findForm( formId, availableForms );
            if ( jsonForm != null ) {
                parseFormContent( jsonForm, contextForms, availableForms );
            }
        }
    }

    protected JsonObject findForm( String id, Collection<String> availableForms  ) {
        for ( Iterator<String> it = availableForms.iterator(); it.hasNext(); ) {
            String formContent = it.next();
            JsonObject jsonForm = parser.parse( formContent ).getAsJsonObject();

            if ( id.equals( jsonForm.get( "id" ).getAsString() ) ) {
                it.remove();
                return jsonForm;
            }
        }
        return null;
    }

    @Override
    protected String getFormExtension() {
        return ".frm";
    }
}
