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

package org.jbpm.simulation.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.util.Bpmn2ResourceImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;

public class JBPMBpmn2ResourceImpl extends Bpmn2ResourceImpl {

	public JBPMBpmn2ResourceImpl(URI uri) {
		super(uri);

        // Switch off DTD external entity processing
        Map parserFeatures = new HashMap();
        parserFeatures.put("http://xml.org/sax/features/external-general-entities", false);
        parserFeatures.put("http://xml.org/sax/features/external-parameter-entities", false);
        this.getDefaultLoadOptions().put(XMLResource.OPTION_PARSER_FEATURES, parserFeatures);

    }
	
	@Override
    protected XMLSave createXMLSave() {
        prepareSave();
        return new JBPMXMLSaveImpl(createXMLHelper()) {
            @Override
            protected boolean shouldSaveFeature(EObject o, EStructuralFeature f) {
                if (Bpmn2Package.eINSTANCE.getDocumentation_Text().equals(f))
                    return false;
                if (Bpmn2Package.eINSTANCE.getFormalExpression_Body().equals(f))
                    return false;
                return super.shouldSaveFeature(o, f);
            }
        };
    }
}
