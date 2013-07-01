package org.jbpm.simulation.helper;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.core.impl.EnvironmentFactory;
import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.PathContext;
import org.jbpm.simulation.impl.SimulationNodeInstanceFactoryRegistry;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class TestUtils {

    public static boolean matchExpected(List<PathContext> paths, List<String>... expectedIds) {
        
        for (PathContext context : paths) {
            List<FlowElement> elements = removeDuplicates(context.getPathElements());
            boolean match = false;
            for (int i = 0; i < expectedIds.length; i++) {
                List<String> expected = expectedIds[i];
                
                if (expected != null && elements.size() == expected.size()) {
                    
                    for (FlowElement fe : elements) {
                        if (!expected.contains(fe.getId())) {
                            System.err.println("Following element not matched: " + fe.getId() + " " + fe.getName());
                            match = false;
                            break;
                        } 
                        match = true;
                    }
                    if (match) {
                        expectedIds[i] = null;
                        break;
                    }
                }
            }
            
            if (!match) {
                return false;
            }
        }
        
        return true;
    }
    
    public static void printOutPaths(List<PathContext> paths, String name) {
        if (!"true".equalsIgnoreCase(System.getProperty("test.debug.off"))) {
            System.out.println("###################" + name + "###################");
            for (PathContext context : paths) {
                System.out.println("PATH: " + context.getId());
                System.out.println("AS TEXT:");
                for (FlowElement fe : context.getPathElements()) {
                    System.out.println(fe.getName() + "  - " + fe.eClass().getName());
                }
            }
            System.out.println("#####################################################");
        }
    }
    
    public static void printOutPaths(List<PathContext> paths, JSONObject jsonPaths, String name) {
        if (!"true".equalsIgnoreCase(System.getProperty("test.debug.off"))) {
            System.out.println("###################" + name + "###################");
            for (PathContext context : paths) {
                System.out.println("$$$$$$$$ PATH: " + context.getId() + " " + context.getType());
                System.out.println("$$$ AS TEXT:");
                for (FlowElement fe : context.getPathElements()) {
                    System.out.println(fe.getName() + "  - " + fe.eClass().getName());
                }
            }
            if (jsonPaths != null) {
                System.out.println("$$$ AS JSON:");
                System.out.println(jsonPaths.toString());
                System.out.println("$$$$$$$$");
            }
            System.out.println("#####################################################");
        }
    }
    
    public static StatefulKnowledgeSession createSession(String process) {
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        builder.add(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2);
        
        KnowledgeBase kbase = builder.newKnowledgeBase();
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo") );
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());
        session.getEnvironment().set("NodeInstanceFactoryRegistry", SimulationNodeInstanceFactoryRegistry.getInstance());
        
        return session;
    }
    
    public static List<FlowElement> removeDuplicates(Set<FlowElement> orig) {
        
        Set<String> uniqueIds = new HashSet<String>();
        List<FlowElement> unique = new ArrayList<FlowElement>();
        
        for (FlowElement fElement : orig) {
            if (!uniqueIds.contains(fElement.getId())) {
                uniqueIds.add(fElement.getId());
                unique.add(fElement);
            }
        }
        System.out.println("Size of flow elements after removing duplicates " + unique.size());
        return unique;
    }
    
    public static ReleaseId createKJarWithMultipleResources(String id, String[] resourceFiles, ResourceType[] types) throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieModuleModel kproj = ks.newKieModuleModel();
        KieFileSystem kfs = ks.newKieFileSystem();

        for (int i = 0; i < resourceFiles.length; i++) {            
            Resource resource = ResourceFactory.newClassPathResource(resourceFiles[i]);
            String res = readResourceContent(resource);
            String type = types[i].getDefaultExtension();

            kfs.write("src/main/resources/" + id.replaceAll("\\.", "/")
                    + "/org/test/res" + i + "." + type, res);
        }

        KieBaseModel kBase1 = kproj.newKieBaseModel(id)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM);

        KieSessionModel ksession1 = kBase1
                .newKieSessionModel(id + ".KSession1")
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("pseudo"));

        kfs.writeKModuleXML(kproj.toXML());

        KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
        assertTrue(kieBuilder.getResults().getMessages().isEmpty());

        KieModule kieModule = kieBuilder.getKieModule();
        return kieModule.getReleaseId();
    }
    
    protected static String readResourceContent(Resource resource) {
        StringBuilder contents = new StringBuilder();
        BufferedReader reader = null;
 
        try {
            reader = new BufferedReader(resource.getReader());
            String text = null;
 
            // repeat until all lines is read
            while ((text = reader.readLine()) != null) {
                contents.append(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return contents.toString();
    }
}
