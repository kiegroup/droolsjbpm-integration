package org.jbpm.simulation.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.Process;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMap;

public class BPMN2Utils {

    public static Definitions getDefinitions(InputStream is) {

        try {
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet
                    .getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                            new JBPMBpmn2ResourceFactoryImpl());
            resourceSet.getPackageRegistry().put(
                    "http://www.omg.org/spec/BPMN/20100524/MODEL",
                    Bpmn2Package.eINSTANCE);
            JBPMBpmn2ResourceImpl resource = (JBPMBpmn2ResourceImpl) resourceSet
                    .createResource(URI
                            .createURI("inputStream://dummyUriWithValidSuffix.xml"));
            resource.getDefaultLoadOptions().put(
                    JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
            resource.setEncoding("UTF-8");
            Map<String, Object> options = new HashMap<String, Object>();
            options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");

            resource.load(is, options);

            EList<Diagnostic> warnings = resource.getWarnings();

            if (warnings != null && !warnings.isEmpty()) {
                for (Diagnostic diagnostic : warnings) {
                    System.out.println("Warning: " + diagnostic.getMessage());
                }
            }

            EList<Diagnostic> errors = resource.getErrors();
            if (errors != null && !errors.isEmpty()) {
                for (Diagnostic diagnostic : errors) {
                    System.out.println("Error: " + diagnostic.getMessage());
                }
                throw new IllegalStateException(
                        "Error parsing process definition");
            }

            return ((DocumentRoot) resource.getContents().get(0))
                    .getDefinitions();
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

    }
    
    public static boolean isAdHoc(FlowElement element) {
        if (element.eContainer() instanceof Process) {
            
            Process process = (Process) element.eContainer();
            Iterator<FeatureMap.Entry> iter = process.getAnyAttribute().iterator();
            while(iter.hasNext()) {
                FeatureMap.Entry entry = iter.next();
                if(entry.getEStructuralFeature().getName().equals("adHoc")) {
                    return Boolean.parseBoolean(((String)entry.getValue()).trim());
                }
            }
        } else if (element instanceof AdHocSubProcess) {
            return true;
        }
        return false;
    }
    
    public static boolean isContainerAdHoc(FlowElementsContainer container) {
        if (container instanceof Process) {
            
            Process process = (Process) container;
            Iterator<FeatureMap.Entry> iter = process.getAnyAttribute().iterator();
            while(iter.hasNext()) {
                FeatureMap.Entry entry = iter.next();
                if(entry.getEStructuralFeature().getName().equals("adHoc")) {
                    return Boolean.parseBoolean(((String)entry.getValue()).trim());
                }
            }
        } else if (container instanceof AdHocSubProcess) {
            return true;
        }
        return false;
    }
}
