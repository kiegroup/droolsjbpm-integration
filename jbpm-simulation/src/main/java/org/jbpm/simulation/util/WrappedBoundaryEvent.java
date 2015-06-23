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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Auditing;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CategoryValue;
import org.eclipse.bpmn2.ConversationLink;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.ExtensionDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.Monitoring;
import org.eclipse.bpmn2.OutputSet;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.FeatureMap;

public class WrappedBoundaryEvent implements BoundaryEvent {
    
    private BoundaryEvent delegate;
    
    public WrappedBoundaryEvent(BoundaryEvent delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<ExtensionAttributeValue> getExtensionValues() {
        return delegate.getExtensionValues();
    }

    @Override
    public List<Documentation> getDocumentation() {
        return delegate.getDocumentation();
    }

    @Override
    public List<ExtensionDefinition> getExtensionDefinitions() {
        return delegate.getExtensionDefinitions();
    }

    @Override
    public String getId() {
        return "$reverseprops$"+delegate.getId();
    }

    @Override
    public void setId(String s) {
        delegate.setId(s);
    }

    @Override
    public FeatureMap getAnyAttribute() {
        return delegate.getAnyAttribute();
    }

    @Override
    public Activity getAttachedToRef() {
        return delegate.getAttachedToRef();
    }

    @Override
    public void setAttachedToRef(Activity activity) {
        delegate.setAttachedToRef(activity);
    }

    @Override
    public boolean isCancelActivity() {
        return delegate.isCancelActivity();
    }

    @Override
    public void setCancelActivity(boolean b) {
        delegate.setCancelActivity(b);
    }

    @Override
    public List<DataOutput> getDataOutputs() {
        return delegate.getDataOutputs();
    }

    @Override
    public List<DataOutputAssociation> getDataOutputAssociation() {
        return delegate.getDataOutputAssociation();
    }

    @Override
    public OutputSet getOutputSet() {
        return delegate.getOutputSet();
    }

    @Override
    public void setOutputSet(OutputSet outputSet) {
        delegate.setOutputSet(outputSet);
    }

    @Override
    public List<EventDefinition> getEventDefinitions() {
        return delegate.getEventDefinitions();
    }

    @Override
    public List<EventDefinition> getEventDefinitionRefs() {
        return delegate.getEventDefinitionRefs();
    }

    @Override
    public boolean isParallelMultiple() {
        return delegate.isParallelMultiple();
    }

    @Override
    public void setParallelMultiple(boolean b) {
        delegate.setParallelMultiple(b);
    }

    @Override
    public List<Property> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public List<SequenceFlow> getIncoming() {
        return delegate.getIncoming();
    }

    @Override
    public List<Lane> getLanes() {
        return delegate.getLanes();
    }

    @Override
    public List<SequenceFlow> getOutgoing() {
        return delegate.getOutgoing();
    }

    @Override
    public Auditing getAuditing() {
        return delegate.getAuditing();
    }

    @Override
    public void setAuditing(Auditing auditing) {
        delegate.setAuditing(auditing);
    }

    @Override
    public Monitoring getMonitoring() {
        return delegate.getMonitoring();
    }

    @Override
    public void setMonitoring(Monitoring monitoring) {
        delegate.setMonitoring(monitoring);
    }

    @Override
    public List<CategoryValue> getCategoryValueRef() {
        return delegate.getCategoryValueRef();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(String s) {
        delegate.setName(s);
    }

    @Override
    public List<ConversationLink> getIncomingConversationLinks() {
        return delegate.getIncomingConversationLinks();
    }

    @Override
    public List<ConversationLink> getOutgoingConversationLinks() {
        return delegate.getOutgoingConversationLinks();
    }

    @Override
    public EClass eClass() {
        return delegate.eClass();
    }

    @Override
    public Resource eResource() {
        return delegate.eResource();
    }

    @Override public EObject eContainer() {
        return delegate.eContainer();
    }

    @Override
    public EStructuralFeature eContainingFeature() {
        return delegate.eContainingFeature();
    }

    @Override
    public EReference eContainmentFeature() {
        return delegate.eContainmentFeature();
    }

    @Override
    public EList<EObject> eContents() {
        return delegate.eContents();
    }

    @Override
    public TreeIterator<EObject> eAllContents() {
        return delegate.eAllContents();
    }

    @Override
    public boolean eIsProxy() {
        return delegate.eIsProxy();
    }

    @Override
    public EList<EObject> eCrossReferences() {
        return delegate.eCrossReferences();
    }

    @Override
    public Object eGet(EStructuralFeature feature) {
        return delegate.eGet(feature);
    }

    @Override public Object eGet(EStructuralFeature feature, boolean resolve) {
        return delegate.eGet(feature, resolve);
    }

    @Override
    public void eSet(EStructuralFeature feature, Object newValue) {
        delegate.eSet(feature, newValue);
    }

    @Override
    public boolean eIsSet(EStructuralFeature feature) {
        return delegate.eIsSet(feature);
    }

    @Override
    public void eUnset(EStructuralFeature feature) {
          delegate.eUnset(feature);
    }

    @Override
    public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
        return delegate.eInvoke(operation, arguments);
    }

    @Override
    public EList<Adapter> eAdapters() {
        return delegate.eAdapters();
    }

    @Override
    public boolean eDeliver() {
        return delegate.eDeliver();
    }

    @Override
    public void eSetDeliver(boolean deliver) {
        delegate.eSetDeliver(deliver);
    }

    @Override
    public void eNotify(Notification notification) {
         delegate.eNotify(notification);
    }
}
